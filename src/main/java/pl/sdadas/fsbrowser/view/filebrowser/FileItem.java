package pl.sdadas.fsbrowser.view.filebrowser;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.fs.FsUtils;
import pl.sdadas.fsbrowser.utils.FileSystemUtils;
import pl.sdadas.fsbrowser.utils.IconFactory;

import javax.swing.*;

/**
 * @author Sławomir Dadas
 */
public class FileItem {

    private final FileStatus status;

    private String name;

    public static FileItem parent() {
        FileItem res = new FileItem();
        res.name = "..";
        return res;
    }

    public FileItem(FileStatus status) {
        this.status = status;
    }

    private FileItem() {
        this.status = null;
    }

    public FileStatus getStatus() {
        return status;
    }

    public String getName() {
        return status != null ? status.getPath().getName() : name;
    }

    public String getSize() {
        return status != null && status.isFile() ? FileSystemUtils.formatByteCount(status.getLen()) : null;
    }

    public String getOwner() {
        return status != null ? status.getOwner() : null;
    }

    public String getGroup() {
        return status != null ? status.getGroup() : null;
    }

    public Long getBlockSize() {
        return status != null ? status.getBlockSize() : null;
    }

    public String getModificationTime() {
        return status != null ? DateFormatUtils.format(status.getModificationTime(), "dd-MM-yyyy HH:mm:ss") : null;
    }

    public String getAccessTime() {
        return status != null ? DateFormatUtils.format(status.getAccessTime(), "dd-MM-yyyy HH:mm:ss") : null;
    }

    public String getPermissions() {
        return status != null ? status.getPermission().toString() : null;
    }

    public Icon getIcon() {
        return IconFactory.getIcon(getIconName());
    }

    public boolean isFile() {
        return status != null && status.isFile();
    }

    public Path getPath() {
        return status != null ? status.getPath() : null;
    }

    public boolean isDirectory() {
        return status != null && status.isDirectory();
    }

    private String getIconName() {
        if(status == null) return "folder-open";
        return status.isFile() ? "file" : "folder";
    }
}
