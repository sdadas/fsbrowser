package pl.sdadas.fsbrowser.view.filebrowser;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

/**
 * @author Sławomir Dadas
 */
public class FileBrowserFilter extends RowFilter<FileSystemTableModel, Integer> {

    private final String text;

    private Integer nameIndex;

    public FileBrowserFilter(String text) {
        this.text = text;
    }

    @Override
    public boolean include(Entry<? extends FileSystemTableModel, ? extends Integer> entry) {
        if(nameIndex == null) {
            this.nameIndex = entry.getModel().getColumnIndex("name");
        }

        if(StringUtils.isBlank(text)) {
            return true;
        } else {
            String name = entry.getStringValue(nameIndex);
            return StringUtils.containsIgnoreCase(name, this.text) || "..".equals(name);
        }
    }
}
