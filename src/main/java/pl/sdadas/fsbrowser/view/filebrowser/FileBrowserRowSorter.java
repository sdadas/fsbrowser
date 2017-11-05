package pl.sdadas.fsbrowser.view.filebrowser;

import javax.swing.table.TableRowSorter;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class FileBrowserRowSorter extends TableRowSorter<FileSystemTableModel> {

    private Runnable beforeSort;

    public FileBrowserRowSorter(FileSystemTableModel model) {
        super(model);
    }

    @Override
    public boolean isSortable(int column) {
        return getModel().getColumn(column).isSortable();
    }

    @Override
    public void setSortable(int column, boolean sortable) {
    }

    public void setBeforeSort(Runnable beforeSort) {
        this.beforeSort = beforeSort;
    }

    @Override
    public void setSortKeys(List<? extends SortKey> sortKeys) {
        if(this.beforeSort != null && !sortKeys.isEmpty()) this.beforeSort.run();
        super.setSortKeys(sortKeys);
    }
}
