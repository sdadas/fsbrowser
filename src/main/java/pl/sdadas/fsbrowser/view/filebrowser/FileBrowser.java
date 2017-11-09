package pl.sdadas.fsbrowser.view.filebrowser;

import com.alee.laf.table.WebTable;
import com.google.common.collect.Lists;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class FileBrowser extends WebTable {

    private final FileSystemTableModel model;

    private final FileBrowserRowSorter sorter;

    public FileBrowser(FileSystemTableModel model) {
        super(model);
        this.model = model;
        this.sorter = this.createSorter();
        this.init();
        this.initColumns();
    }

    private FileBrowserRowSorter createSorter() {
        return new FileBrowserRowSorter(this.model);
    }

    private void init() {
        setFont(new Font("Consolas", Font.PLAIN, 14));
        setShowGrid(false);
        setDragEnabled(true);
        setDropMode(DropMode.INSERT);
        setFillsViewportHeight(true);
        setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
        setRowSorter(this.sorter);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "startEditing");
    }

    private void initColumns() {
        TableColumnModel columnModel = getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn tc = columnModel.getColumn(i);
            FileBrowserColumn col = this.model.getColumn(i);
            if(col.getMinWidth() != null) tc.setMinWidth(col.getMinWidth());
            if(col.getMaxWidth() != null) tc.setMaxWidth(col.getMaxWidth());
            if(col.getWidth() != null) tc.setWidth(col.getWidth());
        }
    }

    public FileItem selectedItem() {
        int rowIdx = getSelectedRow();
        if(rowIdx < 0) return null;
        return this.model.getRow(convertRowIndexToModel(rowIdx));
    }

    public List<FileItem> selectedItems() {
        int rowIdx[] = Arrays.stream(getSelectedRows()).map(this::convertRowIndexToModel).toArray();
        List<FileItem> selection = Lists.newArrayList();
        for (int idx : rowIdx) {
            selection.add(this.model.getRow(idx));
        }
        return selection;
    }

    public void filter(String text) {
        this.sorter.setRowFilter(new FileBrowserFilter(text));
    }

    public void clearSort() {
        this.sorter.setSortKeys(new ArrayList<>());
    }

    public void setBeforeSort(Runnable run) {
        this.sorter.setBeforeSort(run);
    }
}
