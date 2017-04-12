package pl.sdadas.fsbrowser.fs.connection;

import org.apache.hadoop.conf.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;

/**
 * @author Sławomir Dadas
 */
public class ConnectionConfig {

    private final String user;

    private final Configuration configuration;

    private final Iterable<Resource> resources;

    public ConnectionConfig(String user, Iterable<Resource> resources) {
        this.user = user;
        this.resources = resources;
        try {
            this.configuration = createConfiguration();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Configuration createConfiguration() throws IOException {
        Configuration conf = new Configuration();
        for (Resource resource : resources) {
            conf.addResource(resource.getInputStream());
        }
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        conf.set("fs.default.name", conf.get("fs.defaultFS"));
        conf.set("hadoop.job.ugi", user);
        return conf;
    }

    public String getUser() {
        return user;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
