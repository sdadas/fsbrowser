package pl.sdadas.fsbrowser.view.mainwindow;

import com.alee.extended.layout.ToolbarLayout;
import com.alee.extended.statusbar.WebMemoryBar;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.extended.tab.DocumentAdapter;
import com.alee.extended.tab.DocumentData;
import com.alee.extended.tab.PaneData;
import com.alee.extended.tab.WebDocumentPane;
import com.alee.laf.button.WebButton;
import com.alee.laf.menu.*;
import com.alee.laf.panel.WebPanel;
import com.alee.utils.SwingUtils;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.sdadas.fsbrowser.Version;
import pl.sdadas.fsbrowser.app.BeanFactory;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.common.messages.MessageLevel;
import pl.sdadas.fsbrowser.view.connections.ConnectionsDialog;
import pl.sdadas.fsbrowser.view.filesystempanel.FileSystemPanel;
import pl.sdadas.fsbrowser.view.locations.StatusBarHelper;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Sławomir Dadas
 */
public class MainPanel extends WebPanel {

    private final WebDocumentPane<DocumentData> pane;

    private final WebStatusBar statusBar;

    private final AppConfigProvider configProvider;

    private final ClipboardHelper clipboard;

    private final ListeningExecutorService executor;

    private final StatusBarHelper statusBarHelper;

    private ConnectionsDialog connectionsDialog;

    public MainPanel(AppConfigProvider configProvider, ClipboardHelper clipboard,
                     ListeningExecutorService executor, StatusBarHelper statusBarHelper) {
        super(new BorderLayout());
        this.executor = executor;
        this.configProvider = configProvider;
        this.statusBarHelper = statusBarHelper;
        this.pane = createDocumentPane();
        this.statusBar = createStatusBar();
        this.clipboard = clipboard;
        initView();
    }

    private void initView() {
        add(this.pane, BorderLayout.CENTER);
        add(this.statusBar, BorderLayout.PAGE_END);
    }

    private WebDocumentPane<DocumentData> createDocumentPane() {
        WebDocumentPane<DocumentData> result = new WebDocumentPane<>();
        result.setTabMenuEnabled(true);
        result.addDocumentListener(new DocumentAdapter<DocumentData>() {
            @Override
            public void closed(DocumentData document, PaneData<DocumentData> pane, int index) {
                FileSystemPanel comp = (FileSystemPanel) document.getComponent();
                IOUtils.closeQuietly(comp);
            }
        });
        return result;
    }

    private WebStatusBar createStatusBar() {
        WebStatusBar result = new WebStatusBar();
        result.add(this.createButton("Connect", "connection", this::showConnections));
        result.add(this.createButton("Locations", "locations", this::showLocations));
        result.add(this.createButton("About", "about", (event) -> showAboutDialog()));

        WebMemoryBar memoryBar = new WebMemoryBar ();
        memoryBar.setPreferredWidth(memoryBar.getPreferredSize().width + 20);
        memoryBar.setFontSize(11);
        result.add(memoryBar, ToolbarLayout.END);
        return result;
    }

    private WebButton createButton(String text, String icon, ActionListener listener) {
        WebButton button = new WebButton(text, IconFactory.getIcon(icon));
        button.setFontSize(11);
        button.addActionListener(listener);
        button.setMinimumWidth(80);
        return button;
    }

    public void showConnectionsDialog() {
        if(this.connectionsDialog == null) {
            Window window = SwingUtils.getWindowAncestor(this);
            this.connectionsDialog = new ConnectionsDialog(this.configProvider, window);
            this.connectionsDialog.addConnectListener(this::onConnect);
        }
        this.connectionsDialog.setVisible(true);
    }

    private void showAboutDialog() {
        try {
            doShowAboutDialog();
        } catch (IOException e) {
            ViewUtils.error(this, e.getMessage());
        }
    }

    private void showPopup(ActionEvent event, Supplier<WebPopupMenu> supplier) {
        WebButton button = (WebButton) event.getSource();
        WebPopupMenu popup = supplier.get();
        popup.setPopupMenuWay(PopupMenuWay.aboveStart);
        popup.show(button, button.getX() - button.getWidth(), button.getY());
    }

    void showLocations(ActionEvent event) {
        showPopup(event, () -> this.statusBarHelper.createLocationsPopup(this));
    }

    void showConnections(ActionEvent event) {
        showPopup(event, () -> this.statusBarHelper.createConnectionsPopup(this));
    }

    private void doShowAboutDialog() throws IOException {
        Window window = SwingUtils.getWindowAncestor(this);
        Resource resource = new ClassPathResource("about.html");
        String text = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        params.put("fullVersionString", Version.getFullVersionString());
        ViewUtils.message(window, MessageLevel.Info, "", StrSubstitutor.replace(text, params));
    }

    private void onConnect(AppConnection connection) {
        openFileSystemTab(connection);
    }

    public void openFileSystemTab(AppConnection connection) {
        openFileSystemTab(BeanFactory.connection(connection), connection.getName());
    }

    public void openFileSystemTab(FsConnection connection, String name) {
        openFileSystemTab(connection, name, null);
    }

    public void openFileSystemTab(FsConnection connection, String name, String workingDirectory) {
        try {
            FileSystemPanel fspanel = new FileSystemPanel(connection, this.clipboard, this.executor);
            if(StringUtils.isNotBlank(workingDirectory)) {
                fspanel.setCurrentPath(workingDirectory);
            }
            String id = RandomStringUtils.randomAlphabetic(32);
            DocumentData document = new DocumentData(id, IconFactory.getIcon("disk"), name, fspanel);
            this.pane.openDocument(document);
        } catch (RuntimeException ex) {
            ViewUtils.error(this, "Error opening HDFS connection:\n" + ex.getMessage());
        }
    }

    public DocumentData getActiveDocument() {
        return this.pane.getSelectedDocument();
    }

    public List<DocumentData> getAllDocuments() {
        return this.pane.getDocuments();
    }

    public void setActiveDocument(DocumentData data) {
        this.pane.setSelected(data);
    }

    public boolean hasConnections() {
        return !this.configProvider.getConfig().getConnections().isEmpty();
    }

    public WebButton getStatusBarButton(String text) {
        Component[] components = statusBar.getComponents();
        for (Component component : components) {
            if(component instanceof WebButton) {
                if(StringUtils.equalsIgnoreCase(((WebButton) component).getText(), text)) {
                    return (WebButton) component;
                }
            }
        }
        return null;
    }
}
