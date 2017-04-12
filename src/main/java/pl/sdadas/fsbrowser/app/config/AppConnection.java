package pl.sdadas.fsbrowser.app.config;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
@XmlRootElement(name = "connection")
public class AppConnection implements Serializable {

    private String user;

    private String name;

    private List<String> resources = Lists.newArrayList();

    public AppConnection() {
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

    public List<String> getResources() {
        return resources;
    }

    @XmlElementWrapper(name = "resources")
    @XmlElement(name = "resource")
    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return name;
    }
}
