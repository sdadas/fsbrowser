package pl.sdadas.fsbrowser.view.locations;

import com.alee.extended.tab.DocumentData;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.menu.WebPopupMenu;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.app.BeanFactory;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.app.config.AppLocation;
import pl.sdadas.fsbrowser.fs.connection.HarFsConnection;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.filesystempanel.FileSystemPanel;
import pl.sdadas.fsbrowser.view.mainwindow.MainPanel;

import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class StatusBarHelper {

    private final AppConfigProvider provider;

    public StatusBarHelper(AppConfigProvider provider) {
        this.provider = provider;
    }

    public void addLocation(AppLocation location) {
        provider.getConfig().getLocations().add(location);
        provider.writeConfig();
    }

    public WebPopupMenu createConnectionsPopup(MainPanel parent) {
        WebPopupMenu result = new WebPopupMenu();
        List<AppConnection> connections = provider.getConfig().getConnections();
        for (AppConnection connection : connections) {
            result.add(createConnectionItem(connection, parent));
        }
        if(!connections.isEmpty()) {
            result.addSeparator();
        }
        result.add(createEditConnectionsItem(parent));
        return result;
    }

    private WebMenuItem createConnectionItem(AppConnection connection, MainPanel parent) {
        WebMenuItem result = new WebMenuItem(connection.getName());
        result.addActionListener((event) -> parent.openFileSystemTab(connection));
        return result;
    }

    private WebMenuItem createEditConnectionsItem(MainPanel parent) {
        WebMenuItem result = new WebMenuItem("Edit connections", IconFactory.getIcon("connection"));
        result.addActionListener((event) -> parent.showConnectionsDialog());
        return result;
    }

    public WebPopupMenu createLocationsPopup(MainPanel parent) {
        WebPopupMenu result = new WebPopupMenu();
        List<AppLocation> locations = provider.getConfig().getLocations();
        for (AppLocation location : locations) {
            result.add(createLocationItem(location, parent));
        }
        if(!locations.isEmpty()) {
            result.addSeparator();
            result.add(createClearLocationsItem());
        }
        if(parent.getActiveDocument() != null) {
            result.add(createAddLocationItem(parent));
        } else if(locations.isEmpty()) {
            result.add(new WebMenuItem("No saved locations"));
        }

        return result;
    }

    private WebMenuItem createLocationItem(AppLocation location, MainPanel parent) {
        WebMenuItem result = new WebMenuItem(location.getLocationString());
        result.addActionListener((event) -> openLocation(location, parent));
        return result;
    }

    private WebMenuItem createAddLocationItem(MainPanel parent) {
        WebMenuItem result = new WebMenuItem("Add current directory", IconFactory.getIcon("locations"));
        result.addActionListener((event) -> addLocation(parent));
        return result;
    }

    private WebMenuItem createClearLocationsItem() {
        WebMenuItem result = new WebMenuItem("Clear locations", IconFactory.getIcon("locations-clear"));
        result.addActionListener((event) -> clearLocations());
        return result;
    }

    private void openLocation(AppLocation location, MainPanel parent) {
        DocumentData doc = parent.getActiveDocument();
        if(findMatchingPanel(location, doc)) {
            parent.setActiveDocument(doc);
            return;
        }

        List<DocumentData> documents = parent.getAllDocuments();
        for (DocumentData document : documents) {
            if(findMatchingPanel(location, document)) {
                parent.setActiveDocument(doc);
                return;
            }
        }

        openNewConnection(location, parent);
    }

    private void openNewConnection(AppLocation location, MainPanel parent) {
        List<AppConnection> connections = this.provider.getConfig().getConnections();
        AppConnection found = null;
        for (AppConnection connection : connections) {
            if(connection.getName().equals(location.getConnectionId())) {
                found = connection;
                break;
            }
        }

        if(found == null) {
            ViewUtils.error(parent, "No matching connection found for selected location.");
        } else {
            parent.openFileSystemTab(BeanFactory.connection(found), found.getName(), location.getPath());
        }
    }

    private boolean findMatchingPanel(AppLocation location, DocumentData data) {
        if(data == null) return false;

        if(StringUtils.equals(data.getTitle(), location.getConnectionId())) {
            FileSystemPanel panel = (FileSystemPanel) data.getComponent();
            panel.setCurrentPath(location.getPath());
            return true;
        }
        return false;
    }

    private void addLocation(MainPanel parent) {
        DocumentData data = parent.getActiveDocument();
        if(data == null) return;

        FileSystemPanel panel = (FileSystemPanel) data.getComponent();
        if(panel == null || panel.getConnection() instanceof HarFsConnection) return;

        String connectionId = data.getTitle();
        Path path = panel.getModel().getCurrentPath();
        tryAddLocation(new AppLocation(connectionId, path.toUri().getPath()));
    }

    private void tryAddLocation(AppLocation location) {
        List<AppLocation> locations = this.provider.getConfig().getLocations();
        if(locations.contains(location)) return;

        locations.add(location);
        this.provider.writeConfig();
    }

    private void clearLocations() {
        this.provider.getConfig().getLocations().clear();
        this.provider.writeConfig();
    }
}
