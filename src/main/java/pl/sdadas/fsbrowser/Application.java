package pl.sdadas.fsbrowser;

import com.alee.laf.WebLookAndFeel;
import pl.sdadas.fsbrowser.app.BeanFactory;
import pl.sdadas.fsbrowser.view.mainwindow.MainWindow;

import javax.swing.*;

/**
 * @author Sławomir Dadas
 */
public class Application {

    public static void main(String [] args) {
        SwingUtilities.invokeLater (() -> {
            WebLookAndFeel.install();
            BeanFactory.mainWindow().run();
        });
    }

}
