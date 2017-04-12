package pl.sdadas.fsbrowser.app.config;

import pl.sdadas.fsbrowser.Application;
import pl.sdadas.fsbrowser.utils.JaxbUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;

/**
 * @author Sławomir Dadas
 */
public class AppConfigProvider {

    private AppConfig config;

    public AppConfigProvider() {
        this.config = readConfig();
    }

    private AppConfig readConfig() {
        File file = getConfigLocation();
        if(!file.exists()) return new AppConfig();
        try {
            return JaxbUtils.unmarshall(file, AppConfig.class);
        } catch(IllegalStateException ex) {
            return new AppConfig();
        }
    }

    public void writeConfig() {
        File file = getConfigLocation();
        JaxbUtils.marshall(config, file);
    }

    private File getConfigLocation() {
        try {
            File dir = getJarLocation();
            return new File(dir, "config.xml");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private File getJarLocation() throws URISyntaxException {
        CodeSource codeSource = Application.class.getProtectionDomain().getCodeSource();
        File jarFile = new File(codeSource.getLocation().toURI().getPath());
        return jarFile.getParentFile();
    }

    public AppConfig getConfig() {
        return config;
    }
}
