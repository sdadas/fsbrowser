package pl.sdadas.fsbrowser.utils;

import com.alee.extended.layout.HorizontalFlowLayout;
import com.alee.extended.window.PopOverDirection;
import com.alee.extended.window.PopOverLocation;
import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.utils.SizeUtils;
import com.alee.utils.SwingUtils;
import pl.sdadas.fsbrowser.common.UnsafeRunnable;
import pl.sdadas.fsbrowser.exception.FsAccessException;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.view.common.messages.MessageLevel;
import pl.sdadas.fsbrowser.view.common.messages.MessagePopup;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sławomir Dadas
 */
public final class ViewUtils {

    public static void setupDialogWindow(JDialog dialog) {
        dialog.pack();
        dialog.setMinimumSize(dialog.getSize());
        dialog.setLocationRelativeTo(dialog.getOwner());
    }

    public static WebPanel rightLeftPanel(JComponent... comps) {
        return horizontalPanel(ComponentOrientation.RIGHT_TO_LEFT, null, comps);
    }

    public static WebPanel leftRightPanel(JComponent... comps) {
        return horizontalPanel(ComponentOrientation.LEFT_TO_RIGHT, null, comps);
    }

    public static WebPanel rightLeftPanel(Integer minWidth, JComponent... comps) {
        return horizontalPanel(ComponentOrientation.RIGHT_TO_LEFT, minWidth, comps);
    }

    public static WebPanel leftRightPanel(Integer minWidth, JComponent... comps) {
        return horizontalPanel(ComponentOrientation.LEFT_TO_RIGHT, minWidth, comps);
    }

    public static WebPanel horizontalPanel(ComponentOrientation orientation, Integer minWidth, JComponent... comps) {
        HorizontalFlowLayout layout = new HorizontalFlowLayout();
        WebPanel panel = new WebPanel(layout);
        panel.setComponentOrientation(orientation);
        for (JComponent button : comps) {
            if(minWidth != null) SizeUtils.setMinimumWidth(button, minWidth);
            panel.add(button);
        }
        return panel;
    }

    public static MessagePopup error(Component comp, String message) {
        return message(comp, MessageLevel.Error, "Error", message);
    }

    public static MessagePopup warning(Component comp, String message) {
        return message(comp, MessageLevel.Warning, "Warning", message);
    }

    public static MessagePopup info(Component comp, String message) {
        return message(comp, MessageLevel.Info, "Information", message);
    }

    public static MessagePopup message(Component comp, MessageLevel level, String title, String message) {
        MessagePopup result = new MessagePopup(SwingUtils.getWindowAncestor(comp));
        result.setLevel(level);
        result.setTitle(title);
        result.setText(message);
        result.show(PopOverLocation.center);
        return result;
    }

    public static void handleErrors(Component comp, UnsafeRunnable runnable) {
        try {
            runnable.run();
        } catch(FsAccessException ex) {
            SwingUtils.invokeLater(() -> error(comp, "Access denied"));
        } catch(FsException ex) {
            ex.printStackTrace();
            SwingUtils.invokeLater(() -> error(comp, ex.getMessage()));
        }
    }

    public static void ignoreErrors(UnsafeRunnable runnable) {
        try {
            runnable.run();
        } catch (FsException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean requireNativeLibraries(JComponent comp) {
        if(!FileSystemUtils.checkNativeLibraries()) {
            String message = "This action requires hadoop native libraries.\n" +
                    "Please install them, set HADOOP_HOME environment variable and restart application.\n";
            warning(comp, message);
            return false;
        }
        return true;
    }

    private ViewUtils() {
    }
}
