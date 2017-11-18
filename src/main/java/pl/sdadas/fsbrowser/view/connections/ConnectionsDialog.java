package pl.sdadas.fsbrowser.view.connections;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.button.WebButton;
import com.alee.laf.list.WebList;
import com.alee.laf.list.WebListModel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.scroll.WebScrollPane;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.utils.ViewUtils;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class ConnectionsDialog extends WebDialog {

    private final AppConfigProvider configProvider;

    private WebListModel<AppConnection> model;

    private WebList list;

    private List<ConnectListener> connectListeners;

    public ConnectionsDialog(AppConfigProvider configProvider, Window owner) {
        super(owner);
        this.configProvider = configProvider;
        this.connectListeners = new ArrayList<>();
        initView();
        initListeners();
    }

    private void initView() {
        this.model = createListModel();
        this.list = createList();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(400, 200));
        setTitle("Connections");
        setResizable(false);
        setModal(false);

        WebPanel buttonsPanel = createButtonsPanel();
        buttonsPanel.setMargin(10, 0, 0, 0);
        WebScrollPane scroll = new WebScrollPane(this.list);
        scroll.setDrawFocus(false);

        WebPanel panel = new WebPanel(new VerticalFlowLayout());
        panel.setMargin(10);
        panel.add(scroll);
        panel.add(buttonsPanel);

        add(panel);
        ViewUtils.setupDialogWindow(this);
    }

    private void initListeners() {
        this.list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if(event.getClickCount() != 2) return;
                int idx = list.locationToIndex(event.getPoint());
                if(idx < 0) return;
                connect(idx);
            }
        });
        this.list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    int idx = list.getSelectedIndex();
                    if(idx < 0) return;
                    connect(idx);
                }
            }
        });
    }

    private WebPanel createButtonsPanel() {
        List<WebButton> buttons = new LinkedList<>();
        buttons.add(createButton("Edit", "edit-connection", this::editConnection));
        buttons.add(createButton("Remove", "remove-connection", this::removeConnection));
        buttons.add(createButton("Add", "add-connection", this::addConnection));
        buttons.add(createButton("Connect", "connection", this::connect));
        return ViewUtils.rightLeftPanel(85, buttons.toArray(new WebButton[buttons.size()]));
    }

    private void connect() {
        int idx = list.getSelectedIndex();
        connect(idx);
    }

    private void connect(int idx) {
        if(idx < 0) return;
        AppConnection connection = configProvider.getConfig().getConnections().get(idx);
        setVisible(false);
        fireOnConnect(connection);
    }

    private WebButton createButton(String text, String icon, Runnable actionListener) {
        WebButton result = new WebButton(text, IconFactory.getIcon(icon));
        result.addActionListener((event) -> actionListener.run());
        result.setPreferredWidth(85);
        return result;
    }

    private void removeConnection() {
        int idx = list.getSelectedIndex();
        if(idx < 0) return;
        configProvider.getConfig().getConnections().remove(idx);
        configProvider.writeConfig();
        refreshModel();
    }

    private void addConnection() {
        editConnection(null);
    }

    private void editConnection() {
        int idx = list.getSelectedIndex();
        if(idx < 0) return;
        AppConnection object = configProvider.getConfig().getConnections().get(idx);
        editConnection(object);
    }

    private void editConnection(AppConnection connection) {
        boolean adding = connection == null;
        AppConnection object = adding ? new AppConnection("hdfs", "hdfs@cluster") : connection;
        EditConnectionDialog dialog = new EditConnectionDialog(object, this);
        boolean result = dialog.showDialog();
        if(result) {
            if(adding) configProvider.getConfig().getConnections().add(object);
            configProvider.writeConfig();
        }
        refreshModel();
    }

    private void refreshModel() {
        this.model.setElements(configProvider.getConfig().getConnections());
    }

    private WebListModel<AppConnection> createListModel() {
        return new WebListModel<>(this.configProvider.getConfig().getConnections());
    }

    private WebList createList() {
        return new WebList(this.model);
    }

    public void addConnectListener(ConnectListener listener) {
        this.connectListeners.add(listener);
    }

    public void fireOnConnect(AppConnection connection) {
        for (ConnectListener listener : connectListeners) {
            listener.onConnect(connection);
        }
    }
}
