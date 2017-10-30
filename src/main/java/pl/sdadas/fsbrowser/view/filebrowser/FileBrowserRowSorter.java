package pl.sdadas.fsbrowser.view.filebrowser;

import javax.swing.table.TableRowSorter;

/**
 * @author Sławomir Dadas
 */
public class FileBrowserRowSorter extends TableRowSorter<FileSystemTableModel> {

    public FileBrowserRowSorter(FileSystemTableModel model) {
        super(model);
    }

    @Override
    public boolean isSortable(int column) {
        return false;
    }

    @Override
    public void setSortable(int column, boolean sortable) {
    }
}
