package pl.sdadas.fsbrowser.view.mainwindow;

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
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.sdadas.fsbrowser.Version;
import pl.sdadas.fsbrowser.app.BeanFactory;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.fs.connection.ConnectionConfig;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.common.messages.MessageLevel;
import pl.sdadas.fsbrowser.view.connections.ConnectionsDialog;
import pl.sdadas.fsbrowser.view.filesystempanel.FileSystemPanel;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sławomir Dadas
 */
public class MainPanel extends WebPanel {

    private final WebDocumentPane<DocumentData> pane;

    private final WebStatusBar statusBar;

    private final AppConfigProvider configProvider;

    private final ClipboardHelper clipboard;

    private final ListeningExecutorService executor;

    private ConnectionsDialog connectionsDialog;

    public MainPanel(AppConfigProvider configProvider, ClipboardHelper clipboard, ListeningExecutorService executor) {
        super(new BorderLayout());
        this.executor = executor;
        this.configProvider = configProvider;
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
        result.add(this.createButton("Connect", "connection", (event) -> showConnectionsDialog()));
        result.add(this.createButton("About", "about", (event) -> showAboutDialog()));
        return result;
    }

    private WebButton createButton(String text, String icon, ActionListener listener) {
        WebButton button = new WebButton(text, IconFactory.getIcon(icon));
        button.setFontSize(11);
        button.addActionListener(listener);
        button.setMinimumWidth(80);
        return button;
    }

    void showConnectionsDialog() {
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

    private void doShowAboutDialog() throws IOException {
        Window window = SwingUtils.getWindowAncestor(this);
        Resource resource = new ClassPathResource("about.html");
        String text = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        params.put("fullVersionString", Version.getFullVersionString());
        ViewUtils.message(window, MessageLevel.Info, "", StrSubstitutor.replace(text, params));
    }

    private void onConnect(AppConnection connection) {
        openFileSystemTab(BeanFactory.connection(connection), connection.getName());
    }

    public void openFileSystemTab(FsConnection connection, String name) {
        FileSystemPanel fspanel = new FileSystemPanel(connection, this.clipboard, this.executor);
        String id = RandomStringUtils.randomAlphabetic(32);
        DocumentData document = new DocumentData(id, IconFactory.getIcon("disk"), name, fspanel);
        this.pane.openDocument(document);
    }

    private WebMenuBar createMenuBar() {
        WebMenuBar menu = new WebMenuBar();
        //menu.setUndecorated(true);
        menu.setMenuBarStyle(MenuBarStyle.attached);
        WebMenu conn = new WebMenu("Connections", IconFactory.getIcon("connection"));
        WebMenuItem connectAction = new WebMenuItem("Connect", IconFactory.getIcon("add-connection"));

        conn.add(connectAction);
        menu.add(conn);
        return menu;
    }
}
