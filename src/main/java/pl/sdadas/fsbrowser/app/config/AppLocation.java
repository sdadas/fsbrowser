package pl.sdadas.fsbrowser.app.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

/**
 * @author Sławomir Dadas
 */
@XmlRootElement(name = "location")
public class AppLocation implements Serializable {

    private String connectionId;

    private String path;

    public AppLocation() {
    }

    public AppLocation(String connectionId, String path) {
        this.connectionId = connectionId;
        this.path = path;
    }

    public String getConnectionId() {
        return connectionId;
    }

    @XmlAttribute(name = "connectionId")
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getPath() {
        return path;
    }

    @XmlValue
    public void setPath(String path) {
        this.path = path;
    }

    @XmlTransient
    public String getLocationString() {
        return StringUtils.abbreviateMiddle(path, "...", 50) + " @ " + connectionId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AppLocation that = (AppLocation) other;
        return new EqualsBuilder()
                .append(connectionId, that.connectionId)
                .append(path, that.path)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(connectionId)
                .append(path)
                .toHashCode();
    }
}
