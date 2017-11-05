package pl.sdadas.fsbrowser.view.filebrowser;

import com.google.common.primitives.Longs;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
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

    public FileSize getSize() {
        return status != null && status.isFile() ? new FileSize(status.getLen()) : null;
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

    public FileTimestamp getModificationTime() {
        return status != null ? new FileTimestamp(status.getModificationTime()) : null;
    }

    public FileTimestamp getAccessTime() {
        return status != null ? new FileTimestamp(status.getAccessTime()) : null;
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

    public static class FileTimestamp implements Comparable<FileTimestamp> {

        private final long timestamp;

        public FileTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return DateFormatUtils.format(timestamp, "dd-MM-yyyy HH:mm:ss");
        }

        @Override
        public int compareTo(FileTimestamp other) {
            return Longs.compare(this.timestamp, other.timestamp);
        }
    }

    public static class FileSize implements Comparable<FileSize> {

        private final long size;

        public FileSize(long size) {
            this.size = size;
        }

        public long getSize() {
            return size;
        }

        @Override
        public String toString() {
            return FileSystemUtils.formatByteCount(size);
        }

        @Override
        public int compareTo(FileSize other) {
            return Longs.compare(this.size, other.size);
        }
    }
}
