package pl.sdadas.fsbrowser.view.connections;

import com.alee.extended.filechooser.WebFileChooserField;
import com.alee.extended.layout.FormLayout;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.text.WebTextField;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.utils.ViewUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Sławomir Dadas
 */
public class EditConnectionDialog extends WebDialog {

    private WebTextField name;

    private WebTextField user;

    private WebFileChooserField file;

    private WebPanel panel;

    private final AppConnection value;

    private List<String> errors;

    private boolean success;

    public EditConnectionDialog(AppConnection value, Dialog owner) {
        super(owner);
        this.value = value;
        initView();
        initValues();
    }

    private void initView() {
        setDefaultCloseOperation(WebDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(350, 150));
        setResizable(false);
        setModal(true);
        setTitle("Connection");

        this.name = new WebTextField();
        this.user = new WebTextField();
        this.file = new WebFileChooserField(getOwner());
        this.file.setFilesDropEnabled(true);
        this.file.setMultiSelectionEnabled(false);
        this.file.setShowFileExtensions(true);

        FormLayout layout = new FormLayout(5, 5);
        this.panel = new WebPanel(layout);
        this.panel.setMargin(10);
        this.panel.setOpaque(false);
        this.panel.add(new WebLabel("Connection name"));
        this.panel.add(this.name);
        this.panel.add(new WebLabel("Connect as user"));
        this.panel.add(this.user);
        this.panel.add(new WebLabel("HDFS configuration"));
        this.panel.add(this.file);

        WebButtonGroup buttons = new WebButtonGroup();
        WebButton save = new WebButton("Save", (event) -> save());
        WebButton cancel = new WebButton("Cancel", (event) -> cancel());
        buttons.add(save);
        buttons.add(cancel);
        this.panel.add(new WebLabel());
        this.panel.add(buttons);

        add(this.panel);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void initValues() {
        this.name.setText(this.value.getName());
        this.user.setText(this.value.getUser());
    }

    private void save() {
        this.errors = new ArrayList<>();
        String nameValue = this.name.getText();
        String userValue = this.user.getText();
        List<File> filesValue = this.file.getSelectedFiles();

        if(StringUtils.isBlank(nameValue)) this.errors.add("Connection name cannot be empty.");
        if(StringUtils.isBlank(userValue)) this.errors.add("User name cannot be empty.");
        List<String> resources = readConfigurationFile(filesValue);

        if(!this.errors.isEmpty()) {
            ViewUtils.error(this.panel, this.errors.stream().collect(Collectors.joining("\n")));
        } else {
            value.setName(nameValue);
            value.setUser(userValue);
            value.setResources(resources);
            this.success = true;
            setVisible(false);
        }
    }

    public boolean showDialog() {
        this.setVisible(true);
        return this.success;
    }

    private void cancel() {
        this.success = false;
        setVisible(false);
    }

    private List<String> readConfigurationFile(List<File> filesValue) {
        if(filesValue.isEmpty() || filesValue.size() > 1) {
            errors.add("You need to select hdfs configuration file.");
            return Collections.emptyList();
        }
        File file = filesValue.get(0);
        try {
            return readConfigResources(file);
        } catch (IOException e) {
            errors.add(String.format("Problem reading file: %s is not a valid archive.", file.getAbsolutePath()));
            return Collections.emptyList();
        }
    }

    private List<String> readConfigResources(File file) throws IOException {
        List<String> results = new ArrayList<>();
        ZipFile zip = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = getEntryName(entry);
            if(StringUtils.equalsAny(entryName, "core-site.xml", "hdfs-site.xml")) {
                InputStream is = zip.getInputStream(entry);
                String content = IOUtils.toString(is, StandardCharsets.UTF_8);
                results.add(content);
            }
        }
        return results;
    }

    private String getEntryName(ZipEntry entry) {
        String path = entry.getName();
        return path.contains("/") ? StringUtils.substringAfterLast(path, "/") : path;
    }
}
