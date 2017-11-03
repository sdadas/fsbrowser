package pl.sdadas.fsbrowser.fs.action;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.data.hadoop.fs.FsShell;
import pl.sdadas.fsbrowser.fs.common.ListenableFileSystem;
import pl.sdadas.fsbrowser.fs.common.ProgressFileSystemListener;
import pl.sdadas.fsbrowser.view.common.loading.Progress;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Sławomir Dadas
 */
public class CopyFromLocalAction implements FsAction<Void> {

    private final File[] files;

    private final Path dest;

    private Progress progress;

    public CopyFromLocalAction(File[] files, Path dest) {
        this.files = files;
        this.dest = dest;
    }

    public CopyFromLocalAction(File[] files, Path dest, Progress progress) {
        this.files = files;
        this.dest = dest;
        this.progress = progress;
    }

    @Override
    public Void execute(FsShell shell, FileSystem fs) throws Exception {
        Path[] src = Arrays.stream(files).map(f -> new Path(f.getAbsolutePath())).toArray(Path[]::new);
        ListenableFileSystem lfs = createLocalFileSystem(fs);
        ProgressFileSystemListener listener = new ProgressFileSystemListener();
        lfs.addFileSystemListener(listener);
        if(progress != null) progress.setSupplier(listener::getProgress);
        FileUtil.copy(lfs, src, fs, dest, false, true, fs.getConf());
        return null;
    }

    private ListenableFileSystem createLocalFileSystem(FileSystem dest) throws IOException {
        Configuration conf = dest.getConf();
        LocalFileSystem local = FileSystem.getLocal(conf);
        return new ListenableFileSystem(local);
    }
}
