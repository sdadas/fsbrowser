package pl.sdadas.fsbrowser.view.filecontent;

import com.alee.extended.statusbar.WebStatusBar;
import com.alee.laf.label.WebLabel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextArea;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import pl.sdadas.fsbrowser.utils.ViewUtils;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static pl.sdadas.fsbrowser.utils.FileSystemUtils.formatByteCount;

/**
 * @author Sławomir Dadas
 */
public class FileContentDialog extends WebDialog {

    private final CountingInputStream cstream;

    private final BufferedReader reader;

    private WebTextArea text;

    private WebStatusBar statusBar;

    private WebLabel statusLabel;

    private char[] buffer = new char[10000];

    private boolean hasMore = true;

    private final long bytesTotal;

    public FileContentDialog(InputStream stream, long bytesTotal, Window owner) {
        super(owner);
        this.cstream = new CountingInputStream(stream);
        this.reader = createReader(cstream);
        this.bytesTotal = bytesTotal;
        initView();
    }

    private void initView() {
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 600));
        setLayout(new BorderLayout());
        setTitle("File content");
        setResizable(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                IOUtils.closeQuietly(reader);
            }
        });

        this.text = new WebTextArea();
        this.text.setText("");
        this.text.setLineWrap(true);
        this.text.setWrapStyleWord(false);
        this.text.setEditable(false);
        this.text.setFont(new Font("Consolas", Font.PLAIN, 12));
        this.statusLabel = new WebLabel("");
        this.statusBar = new WebStatusBar();
        this.statusBar.add(this.statusLabel);

        WebScrollPane scroll = new WebScrollPane(this.text);
        scroll.getVerticalScrollBar().addAdjustmentListener(this::onScrollBottom);
        scroll.getVerticalScrollBar().setUnitIncrement(100);

        this.add(scroll, BorderLayout.CENTER);
        this.add(this.statusBar, BorderLayout.PAGE_END);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void readChunk() {
        if(!hasMore) return;
        try {
            int read = reader.read(this.buffer, 0, 10000);
            if(read <= 0) {
                hasMore = false;
                return;
            }
            String more = new String(this.buffer, 0, read);
            this.text.append(more);
            this.statusLabel.setText(getByteInfo());
        } catch (IOException e) {
            ViewUtils.error(this.text, e.getMessage());
        }
    }

    private String getByteInfo() {
        return String.format("%s / %s read", formatByteCount(cstream.getByteCount()), formatByteCount(bytesTotal));
    }

    private BufferedReader createReader(InputStream stream) {
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        return new BufferedReader(isr);
    }

    public void showDialog() {
        setVisible(true);
        readChunk();
    }

    private void onScrollBottom(AdjustmentEvent event) {
        if(!event.getValueIsAdjusting()) {
            Adjustable adjustable = event.getAdjustable();
            int position = event.getValue() + adjustable.getVisibleAmount();
            int bottom = adjustable.getMaximum() - 5;
            if(position >= bottom) {
                readChunk();
            }
        }
    }

}
