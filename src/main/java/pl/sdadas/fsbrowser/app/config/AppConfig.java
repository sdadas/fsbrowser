package pl.sdadas.fsbrowser.app.config;

import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
@XmlRootElement(name = "config")
public class AppConfig implements Serializable {

    private List<AppConnection> connections = Lists.newArrayList();

    private List<AppLocation> locations = Lists.newArrayList();

    public List<AppConnection> getConnections() {
        return connections;
    }

    @XmlElementWrapper(name = "connections")
    @XmlElement(name = "connection")
    public void setConnections(List<AppConnection> connections) {
        this.connections = connections;
    }

    public List<AppLocation> getLocations() {
        return locations;
    }

    @XmlElementWrapper(name = "locations")
    @XmlElement(name = "location")
    public void setLocations(List<AppLocation> locations) {
        this.locations = locations;
    }
}
