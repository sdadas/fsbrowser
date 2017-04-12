package pl.sdadas.fsbrowser.utils;

import com.alee.extended.window.PopOverDirection;
import com.alee.extended.window.PopOverLocation;
import com.alee.utils.SwingUtils;
import pl.sdadas.fsbrowser.common.UnsafeRunnable;
import pl.sdadas.fsbrowser.exception.FsAccessException;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.view.common.messages.MessageLevel;
import pl.sdadas.fsbrowser.view.common.messages.MessagePopup;

import javax.swing.*;

/**
 * @author Sławomir Dadas
 */
public final class ViewUtils {

    public static MessagePopup error(JComponent comp, String message) {
        return message(comp, MessageLevel.Error, "Error", message);
    }

    public static MessagePopup warning(JComponent comp, String message) {
        return message(comp, MessageLevel.Warning, "Warning", message);
    }

    public static MessagePopup info(JComponent comp, String message) {
        return message(comp, MessageLevel.Info, "Information", message);
    }

    public static MessagePopup message(JComponent comp, MessageLevel level, String title, String message) {
        MessagePopup result = new MessagePopup(SwingUtils.getWindowAncestor(comp));
        result.setLevel(level);
        result.setTitle(title);
        result.setText(message);
        result.show(PopOverLocation.center);
        return result;
    }

    public static void handleErrors(JComponent comp, UnsafeRunnable runnable) {
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

    private ViewUtils() {
    }
}
