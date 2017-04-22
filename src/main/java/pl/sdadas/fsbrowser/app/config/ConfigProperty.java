package pl.sdadas.fsbrowser.app.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Sławomir Dadas
 */
@XmlRootElement(name = "property")
public class ConfigProperty {

    private String name;

    private String value;

    public String getName() {
        return name;
    }

    @XmlElement(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    @XmlElement(name = "value")
    public void setValue(String value) {
        this.value = value;
    }
}
