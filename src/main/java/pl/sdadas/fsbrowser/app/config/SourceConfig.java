package pl.sdadas.fsbrowser.app.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
@XmlRootElement(name = "configuration")
public class SourceConfig {

    private List<ConfigProperty> properties;

    public List<ConfigProperty> getProperties() {
        return properties;
    }

    @XmlElement(name = "property")
    public void setProperties(List<ConfigProperty> properties) {
        this.properties = properties;
    }
}
