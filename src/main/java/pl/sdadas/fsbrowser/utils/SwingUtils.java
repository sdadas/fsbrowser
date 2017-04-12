package pl.sdadas.fsbrowser.utils;

import org.apache.commons.io.IOUtils;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author Sławomir Dadas
 */
public final class SwingUtils {

    public static Image getImageResource(String classpath) {
        InputStream is = SwingUtils.class.getClassLoader().getResourceAsStream(classpath);
        try {
            byte[] bytes = IOUtils.toByteArray(is);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            return toolkit.createImage(bytes);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void openUrl(String uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(uri));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private SwingUtils() {
    }
}
