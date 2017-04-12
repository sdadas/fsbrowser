package pl.sdadas.fsbrowser.utils;

import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sławomir Dadas
 */
public final class IconFactory {

    private final static Map<String, Icon> ICONS = new HashMap<String, Icon>();

    public static Icon getIcon(String name) {
        Icon icon = ICONS.get(name);
        if(icon == null) {
            Image image = getImageResource(String.format("icons/%s.png", name));
            icon = new ImageIcon(image);
            ICONS.put(name, icon);
        }
        return icon;
    }

    public static Image getImageResource(String classpath) {
        InputStream is = IconFactory.class.getClassLoader().getResourceAsStream(classpath);
        try {
            byte[] bytes = IOUtils.toByteArray(is);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            return toolkit.createImage(bytes);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private IconFactory() {
    }
}
