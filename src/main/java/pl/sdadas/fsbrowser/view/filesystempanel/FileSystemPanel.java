package pl.sdadas.fsbrowser.view.filesystempanel;

import com.alee.extended.breadcrumb.WebBreadcrumb;
import com.alee.extended.breadcrumb.WebBreadcrumbButton;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.toolbar.ToolbarStyle;
import com.alee.laf.toolbar.WebToolBar;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.app.BeanFactory;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.common.loading.LoadingOverlay;
import pl.sdadas.fsbrowser.view.filebrowser.FileBrowser;
import pl.sdadas.fsbrowser.view.filebrowser.FileItem;
import pl.sdadas.fsbrowser.view.filebrowser.FileSystemTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;

/**
 * @author Sławomir Dadas
 */
public class FileSystemPanel extends LoadingOverlay implements Closeable {

    private final FsConnection connection;

    private final FileSystemTableModel model;

    private WebBreadcrumb breadcrumb;

    private WebToolBar toolbar;

    private FileBrowser browser;

    private FileSystemActions actions;

    private ClipboardHelper clipboard;

    private boolean closed = false;

    public FileSystemPanel(FsConnection connection, ClipboardHelper clipboard) {
        this.connection = connection;
        this.model = BeanFactory.tableModel(connection);
        this.actions = new FileSystemActions(this);
        this.clipboard = clipboard;
        initView();
        initListeners();
    }

    private void initListeners() {
        onPathChanged(this.model.getCurrentPath());
        this.model.addPathListener(this::onPathChanged);
        this.browser.setTransferHandler(new FileSystemTransferHandler(this));
    }

    private void initView() {
        this.breadcrumb = createBreadcrumb();
        this.browser = createFileBrowser();
        this.toolbar = createToolBar();
        WebScrollPane browserScroll = new WebScrollPane(browser);
        browserScroll.setDrawFocus(false);
        browserScroll.getVerticalScrollBar().addAdjustmentListener(this::onScrollBottom);
        browserScroll.getVerticalScrollBar().setUnitIncrement(50);

        JPanel head = new JPanel(new VerticalFlowLayout());
        head.add(toolbar);
        head.add(breadcrumb);

        WebPanel panel = new WebPanel(new BorderLayout());
        panel.add(head, BorderLayout.PAGE_START);
        panel.add(browserScroll, BorderLayout.CENTER);
        setComponent(panel);
    }

    private void onPathChanged(Path value) {
        Path current = value;
        this.breadcrumb.removeAll();
        do {
            WebBreadcrumbButton button = createBreadcrumbButton(current);
            this.breadcrumb.add(button, 0);
            current = current.getParent();
        } while (current != null);
        this.breadcrumb.repaint();
    }

    private WebBreadcrumbButton createBreadcrumbButton(Path path) {
        String name = path.getName();
        if(StringUtils.isBlank(name)) {
            name = "/";
        }
        WebBreadcrumbButton button = new WebBreadcrumbButton(name);
        button.setIcon(IconFactory.getIcon("folder-small"));
        button.addActionListener(event -> {
            ViewUtils.handleErrors(this, () -> {
                this.model.onFileClicked(path);
            });
        });
        return button;
    }

    private void onScrollBottom(AdjustmentEvent event) {
        if(!event.getValueIsAdjusting()) {
            Adjustable adjustable = event.getAdjustable();
            int position = event.getValue() + adjustable.getVisibleAmount();
            int bottom = adjustable.getMaximum() - 5;
            if(position >= bottom) {
                this.model.loadMoreRows();
            }
        }
    }

    private WebBreadcrumb createBreadcrumb() {
        return new WebBreadcrumb(false);
    }

    private FileBrowser createFileBrowser() {
        FileBrowser browser = new FileBrowser(model);
        browser.addMouseListener(this.onClick());
        browser.addKeyListener(this.onEnter());
        return browser;
    }

    private KeyAdapter onEnter() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    int idx = browser.getSelectedRow();
                    if (idx >= 0) {
                        onFileAccessed(idx);
                    }
                }
            }
        };
    }

    private MouseAdapter onClick() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() != 2) return;
                Point point = event.getPoint();
                int idx = browser.rowAtPoint(point);
                onFileAccessed(idx);
            }
        };
    }

    private void onFileAccessed(int idx) {
        if (idx < 0) return;

        ViewUtils.handleErrors(this, () -> {
            FileItem item = model.getRow(idx);
            if(item == null) return;

            if(item.isFile()) {
                asyncAction(() -> this.actions.doPreviewFile(Collections.singletonList(item)));
            } else {
                model.onFileClicked(idx);
            }
        });
    }

    private WebToolBar createToolBar() {
        WebToolBar result = new WebToolBar();
        result.setToolbarStyle(ToolbarStyle.attached);
        result.setFloatable(false);
        result.add(createToolButton(this.actions.copyFromLocalAction()));
        result.add(createToolButton(this.actions.copyToLocalAction()));
        result.add(createToolButton(this.actions.cutAction()));
        result.add(createToolButton(this.actions.copyAction()));
        result.add(createToolButton(this.actions.pasteAction()));
        result.add(createToolButton(this.actions.gotoAction()));
        result.add(createToolButton(this.actions.mkdirAction()));
        result.add(createToolButton(this.actions.touchAction()));
        result.add(createToolButton(this.actions.moveToTrashAction()));
        result.add(createToolButton(this.actions.removeAction()));
        result.add(createToolButton(this.actions.previewFileAction()));
        result.addSeparator();
        result.add(createToolButton(this.actions.fsckAction()));
        return result;
    }

    private WebButton createToolButton(FileAction action) {
        WebButton result = new WebButton(action.getIcon());
        result.setRolloverDecoratedOnly(true);
        result.setDrawFocus(false);
        if(StringUtils.isNotBlank(action.getName())) result.setToolTipText(action.getName());
        result.addActionListener((event) -> asyncAction(() -> action.run(browser.selectedItems())));
        return result;
    }

    public FsConnection getConnection() {
        return connection;
    }

    public FileSystemTableModel getModel() {
        return model;
    }

    public WebBreadcrumb getBreadcrumb() {
        return breadcrumb;
    }

    public WebToolBar getToolbar() {
        return toolbar;
    }

    public FileBrowser getBrowser() {
        return browser;
    }

    public ClipboardHelper getClipboard() {
        return clipboard;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        IOUtils.closeQuietly(this.connection);
    }

    public boolean isClosed() {
        return closed;
    }
}
