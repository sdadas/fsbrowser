package pl.sdadas.fsbrowser.view.connections;

import com.alee.extended.filechooser.WebFileChooserField;
import com.alee.extended.layout.FormLayout;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;
import com.alee.laf.text.WebTextField;
import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.app.config.ConfigProperty;
import pl.sdadas.fsbrowser.app.config.SourceConfig;
import pl.sdadas.fsbrowser.common.PropertyTableModel;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.utils.JaxbUtils;
import pl.sdadas.fsbrowser.utils.ViewUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Sławomir Dadas
 */
public class EditConnectionDialog extends WebDialog {

    private final PropertyTableModel propertiesModel;

    private final AppConfigProvider configProvider;

    private WebTable propertiesTable;

    private WebTextField name;

    private WebTextField user;

    private WebFileChooserField file;

    private WebPanel panel;

    private final AppConnection value;

    private boolean success;

    public EditConnectionDialog(AppConnection value, ConnectionsDialog owner) {
        super(owner);
        this.value = value;
        this.propertiesModel = new PropertyTableModel(value.getPropertiesMap());
        this.configProvider = owner.getConfigProvider();
        initView();
        initListeners();
        initValues();
    }

    private void initView() {
        setDefaultCloseOperation(WebDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(350, 350));
        setTitle("Connection");
        setModal(true);

        this.name = new WebTextField();
        this.user = new WebTextField();
        this.file = new WebFileChooserField(getOwner());
        this.file.setFilesDropEnabled(true);
        this.file.setMultiSelectionEnabled(false);
        this.file.setShowFileExtensions(true);
        this.file.setMinimumWidth(200);
        WebLabel fileLabel = new WebLabel("Load configuration file", IconFactory.getIcon("question"));
        TooltipManager.setTooltip(fileLabel, getFileTooltipText(), TooltipWay.down, 0);
        WebPanel filePanel = ViewUtils.leftRightPanel(fileLabel, this.file);
        this.propertiesTable = new WebTable(this.propertiesModel);
        WebScrollPane scroll = new WebScrollPane(propertiesTable);
        scroll.getVerticalScrollBar().setUnitIncrement(50);
        scroll.setDrawFocus(false);
        scroll.setPreferredHeight(300);
        WebPanel buttonsPanel = ViewUtils.rightLeftPanel(50,
                new WebButton("Cancel", this::cancel),
                new WebButton("Save", this::save));
        buttonsPanel.setMargin(10, 0, 0, 0);

        FormLayout layout = new FormLayout(5, 5);
        WebPanel formPanel = new WebPanel(layout);
        formPanel.setMargin(0, 0, 20, 0);
        formPanel.add(new WebLabel("Connection name"));
        formPanel.add(this.name);
        formPanel.add(new WebLabel("Connect as user"));
        formPanel.add(this.user);

        this.panel = new WebPanel(new VerticalFlowLayout());
        this.panel.setMargin(10);
        this.panel.add(formPanel, filePanel, scroll, buttonsPanel);
        add(this.panel);
        ViewUtils.setupDialogWindow(this);
    }

    private String getFileTooltipText() {
        return "<html><b>core-site.xml</b> and <b>hdfs-site.xml</b> hadoop configuration<br/>" +
                "files need to be provided. They can either be loaded separately<br/>" +
                "or as a zip archive. If you use <b>Cloudera Hadoop</b>, you can<br/>" +
                "download configuration from Cloudera Manager:<br/>" +
                "<b>HDFS Service</b> -> <b>Actions</b> -> <b>Download client configuration<b/><br/></html>";
    }

    private void initListeners() {
        this.file.addSelectedFilesListener(files -> {
            if(files.size() == 0) return;
            List<String> errors = new ArrayList<>();
            List<ConfigProperty> properties = readConfigurationFile(files.get(0), errors);
            if(!errors.isEmpty()) {
                ViewUtils.error(this.panel, errors.stream().collect(Collectors.joining("\n")));
            } else {
                this.value.getProperties().addAll(properties);
                this.propertiesModel.setProperties(this.value.getPropertiesMap());
            }
        });
    }

    private void initValues() {
        this.name.setText(this.value.getName());
        this.user.setText(this.value.getUser());
    }

    private void save(ActionEvent event) {
        List<String> errors = new ArrayList<>();
        String nameValue = this.name.getText();
        String userValue = this.user.getText();

        if(StringUtils.isBlank(nameValue)) errors.add("Connection name cannot be empty.");
        if(StringUtils.isBlank(userValue)) errors.add("User name cannot be empty.");
        if(this.value.getProperties() == null || this.value.getProperties().isEmpty()) {
            errors.add("No hadoop configuration files provided.");
        }

        Optional<AppConnection> matchesName = configProvider.getConfig().getConnections().stream()
                .filter(conn -> StringUtils.equalsIgnoreCase(conn.getName(), nameValue)).findAny();
        if(matchesName.isPresent()) {
            errors.add(String.format("Connection %s already exists.", nameValue));
        }

        if(!errors.isEmpty()) {
            ViewUtils.error(this.panel, errors.stream().collect(Collectors.joining("\n")));
        } else {
            value.setName(nameValue);
            value.setUser(userValue);
            this.success = true;
            setVisible(false);
        }
    }

    public boolean showDialog() {
        this.setVisible(true);
        return this.success;
    }

    private void cancel(ActionEvent event) {
        this.success = false;
        setVisible(false);
    }

    private List<ConfigProperty> readConfigurationFile(File file, List<String> errors) {
        try {
            String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
            List<ConfigProperty> res = new ArrayList<>();
            switch (ext) {
                case "xml": res = parseFile(new FileSystemResource(file));  break;
                case "zip": res = readArchive(file); break;
                default: errors.add("File should be either hadoop xml config or zip file containing configuration.");
            }
            this.file.setSelectedFile(null);
            return res;
        } catch (IOException e) {
            errors.add(String.format("Problem reading file: %s is not a valid configuration.", file.getAbsolutePath()));
            return Collections.emptyList();
        }
    }

    private List<ConfigProperty> readArchive(File file) throws IOException {
        List<ConfigProperty> results = new ArrayList<>();
        ZipFile zip = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = getEntryName(entry);
            if(StringUtils.equalsAny(entryName, "core-site.xml", "hdfs-site.xml")) {
                InputStream is = zip.getInputStream(entry);
                List<ConfigProperty> properties = parseFile(new InputStreamResource(is));
                results.addAll(properties);
            }
        }
        return results;
    }

    private List<ConfigProperty> parseFile(Resource resource) {
        SourceConfig config = JaxbUtils.unmarshall(resource, SourceConfig.class);
        return config.getProperties();
    }

    private String getEntryName(ZipEntry entry) {
        String path = entry.getName();
        return path.contains("/") ? StringUtils.substringAfterLast(path, "/") : path;
    }
}
