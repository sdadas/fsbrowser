package pl.sdadas.fsbrowser.view.common.loading;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.panel.VerticalPanel;
import com.alee.extended.panel.WebOverlay;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.progressbar.WebProgressBar;
import com.alee.utils.SwingUtils;
import com.alee.utils.swing.EmptyMouseAdapter;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import org.apache.commons.lang3.StringUtils;
import pl.sdadas.fsbrowser.utils.ViewUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author Sławomir Dadas
 */
public class LoadingOverlay extends WebOverlay {

    private final static String DEFAULT_PROGRESS = "Please wait...";

    private final BackgroundOverlay background;

    private final WebLabel label;

    private final WebProgressBar progressBar;

    private final WebPanel loading;

    private final ListeningExecutorService executor;

    public LoadingOverlay(ListeningExecutorService executor) {
        this.progressBar = createProgressBar();
        this.label = createLabel();
        this.loading = createPanel();
        this.background = new BackgroundOverlay();
        this.background.setVisible(false);
        this.executor = executor;
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
        asyncAction(runnable, null);
    }

    public void asyncAction(Runnable runnable, Progress progress) {
        busy(true);
        ListenableFuture<?> future = executor.submit(runnable);
        future.addListener(() -> busy(false), executor);
        if(progress != null) {
            executor.submit(() -> this.trackProgress(future, progress));
        }
    }

    private void trackProgress(ListenableFuture<?> future, Progress progress) {
        while(!future.isDone()) {
            try {
                Thread.sleep(1000L);
                String val = progress.getValue();
                if(StringUtils.isNotBlank(val)) {
                    setProgress(val);
                }
            } catch (InterruptedException e) {
                setProgress(DEFAULT_PROGRESS);
                return;
            }
        }
        setProgress(DEFAULT_PROGRESS);
    }

    private void setProgress(String text) {
        SwingUtils.invokeLater(() -> this.label.setText(text));
    }

    private WebProgressBar createProgressBar() {
        WebProgressBar progress = new WebProgressBar();
        progress.setIndeterminate(true);
        progress.setStringPainted(false);
        progress.setPreferredSize(new Dimension(200, 20));
        return progress;
    }

    private WebLabel createLabel() {
        return new WebLabel(DEFAULT_PROGRESS, SwingConstants.CENTER);
    }

    private WebPanel createPanel() {
        VerticalPanel vp = new VerticalPanel(this.label, this.progressBar);
        vp.setMargin(5);
        WebPanel res = new WebPanel(true, vp);
        res.setVisible(false);
        return res;
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
