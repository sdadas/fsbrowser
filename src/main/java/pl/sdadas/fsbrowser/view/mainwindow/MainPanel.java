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
import pl.sdadas.fsbrowser.app.BeanFactory;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.fs.connection.ConnectionConfig;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.view.connections.ConnectionsDialog;
import pl.sdadas.fsbrowser.view.filesystempanel.FileSystemPanel;

import java.awt.*;

/**
 * @author Sławomir Dadas
 */
public class MainPanel extends WebPanel {

    private final WebDocumentPane<DocumentData> pane;

    private final WebStatusBar statusBar;

    private final AppConfigProvider configProvider;

    private final ClipboardHelper clipboard;

    private final ListeningExecutorService executor;

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
        WebButton connectionsButton = new WebButton(IconFactory.getIcon("connection"));
        connectionsButton.addActionListener((event) -> showConnectionsDialog());
        result.add(connectionsButton);
        return result;
    }

    public void showConnectionsDialog() {
        Window window = SwingUtils.getWindowAncestor(this);
        ConnectionsDialog dialog = new ConnectionsDialog(this.configProvider, window);
        dialog.addConnectListener(this::onConnect);
        dialog.setVisible(true);
    }

    private void onConnect(AppConnection connection) {
        FileSystemPanel fspanel = new FileSystemPanel(BeanFactory.connection(connection), this.clipboard, this.executor);
        String id = RandomStringUtils.randomAlphabetic(32);
        DocumentData document = new DocumentData(id, IconFactory.getIcon("disk"), connection.getName(), fspanel);
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
