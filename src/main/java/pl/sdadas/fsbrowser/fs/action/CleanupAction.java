package pl.sdadas.fsbrowser.fs.action;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.data.hadoop.fs.FsShell;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class CleanupAction implements FsAction<Void> {

    private final List<Path> paths;

    private final Date olderThan;

    public CleanupAction(List<Path> paths, Date olderThan) {
        this.paths = paths;
        this.olderThan = olderThan;
    }

    @Override
    public Void execute(FsShell shell, FileSystem fs) throws Exception {
        for (Path path : paths) {
            cleanupPath(shell, fs, path);
        }
        return null;
    }

    private void cleanupPath(FsShell shell, FileSystem fs, Path path) throws IOException {
        FileStatus[] statuses = fs.listStatus(path);
        for (FileStatus status : statuses) {
            long timestamp = status.getModificationTime();
            Date date = new Date(timestamp);
            if (olderThan.after(date)) {
                shell.rmr(true, status.getPath().toUri().getPath());
            }
        }
    }
}
