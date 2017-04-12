package pl.sdadas.fsbrowser.view.mainwindow;

import com.alee.laf.rootpane.WebFrame;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sławomir Dadas
 */
public class MainWindow extends WebFrame {

    private final MainPanel panel;

    public MainWindow(MainPanel panel) {
        this.panel = panel;
        initView();
    }

    private void initView() {
        setMinimumSize(new Dimension(800, 600));
        setContentPane(this.panel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("FSBrowser");
        setLocationRelativeTo(null);
        pack();
    }

    public void run() {
        setVisible(true);
        this.panel.showConnectionsDialog();
    }
}
