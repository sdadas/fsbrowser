package pl.sdadas.fsbrowser.view.common.loading;

import com.alee.extended.panel.WebOverlay;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;
import com.alee.utils.SwingUtils;
import com.alee.utils.swing.EmptyMouseAdapter;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sławomir Dadas
 */
public class LoadingOverlay extends WebOverlay {

    private final BackgroundOverlay background;

    private final WebPanel loading;

    public LoadingOverlay() {
        this.loading = createLoadinOverlay();
        this.loading.setVisible(false);
        this.background = new BackgroundOverlay();
        this.background.setVisible(false);
        addOverlay(background);
        addOverlay(loading, SwingConstants.CENTER, SwingConstants.CENTER);
    }

    public void busy(boolean value) {
        setFocusTraversalKeysEnabled(!value);
        background.setFocusTraversalKeysEnabled(!value);
        background.setVisible(value);
        loading.setVisible(value);
        if(value) {
            background.requestFocus();
        }
    }

    public void asyncAction(Runnable runnable) {
        busy(true);
        Thread thread = new Thread(() -> {
            try {
                runnable.run();
            } finally {
                busy(false);
            }
        });
        thread.start();
    }

    private WebPanel createLoadinOverlay() {
        WebProgressBar progress = new WebProgressBar();
        progress.setIndeterminate(true);
        progress.setStringPainted(true);
        progress.setPreferredSize(new Dimension(200, 20));
        progress.setString("Please wait...");
        return new WebPanel(true, progress);
    }

    private class BackgroundOverlay extends JPanel {

        public BackgroundOverlay() {
            super();
            this.setFocusable(true);
            SwingUtils.setOrientation(this);
            EmptyMouseAdapter.install(this);
            setBackground(new Color(128, 128, 128, 50));
        }

        @Override
        public boolean contains(final int x, final int y) {
            return super.contains(x, y);
        }
    }
}
