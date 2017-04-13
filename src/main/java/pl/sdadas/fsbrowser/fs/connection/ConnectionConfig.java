package pl.sdadas.fsbrowser.fs.connection;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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

    public ConnectionConfig(String user, Configuration... mergeConfigs) {
        this.user = user;
        this.configuration = mergeConfigurations(mergeConfigs);
        this.resources = Collections.emptyList();
    }

    private Configuration mergeConfigurations(Configuration... configs) {
        Configuration conf = new Configuration();
        List<String> nameServices = Lists.newArrayList();
        for (Configuration other : configs) {
            String ns = other.get("dfs.nameservices");
            if(StringUtils.isNotBlank(ns)) {
                nameServices.add(ns);
            }
            conf.addResource(other);
        }
        if(!nameServices.isEmpty()) {
            String ns = StringUtils.join(nameServices, ',');
            conf.set("dfs.nameservices", ns);
        }
        afterConfigCreated(conf);
        return conf;
    }

    private Configuration createConfiguration() throws IOException {
        Configuration conf = new Configuration();
        for (Resource resource : resources) {
            conf.addResource(resource.getInputStream());
        }
        afterConfigCreated(conf);
        return conf;
    }

    private void afterConfigCreated(Configuration conf) {
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        conf.set("fs.default.name", conf.get("fs.defaultFS"));
        conf.set("hadoop.job.ugi", user);
    }

    public String getUser() {
        return user;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
