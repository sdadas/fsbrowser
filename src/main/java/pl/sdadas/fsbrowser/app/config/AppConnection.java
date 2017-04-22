package pl.sdadas.fsbrowser.app.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.*;

/**
 * @author Sławomir Dadas
 */
@XmlRootElement(name = "connection")
public class AppConnection implements Serializable {

    private String user;

    private String name;

    private List<ConfigProperty> properties = new ArrayList<>();

    public AppConnection() {
    }

    public AppConnection(String user, String name) {
        this.user = user;
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    @XmlElement(name = "user")
    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    @XmlElement(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public List<ConfigProperty> getProperties() {
        return properties;
    }

    @XmlElementWrapper(name = "configuration")
    @XmlElement(name = "property")
    public void setProperties(List<ConfigProperty> properties) {
        this.properties = properties;
    }

    @XmlTransient
    public Map<String, String> getPropertiesMap() {
        Map<String, String> result = new LinkedHashMap<>();
        if(this.properties == null) return result;
        for (ConfigProperty property : properties) {
            result.put(property.getName(), property.getValue());
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
