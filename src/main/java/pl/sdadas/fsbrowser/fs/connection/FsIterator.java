package pl.sdadas.fsbrowser.fs.connection;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.RemoteIterator;
import org.springframework.data.hadoop.fs.FsShell;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Sławomir Dadas
 */
public class FsIterator implements Iterator<FileStatus> {

    private final FsShell shell;

    private final FileSystem fs;

    private final RemoteIterator<LocatedFileStatus> inner;

    public FsIterator(FsShell shell, FileSystem fs, RemoteIterator<LocatedFileStatus> inner) {
        this.shell = shell;
        this.fs = fs;
        this.inner = inner;
    }

    @Override
    public boolean hasNext() {
        try {
            return inner.hasNext();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public FileStatus next() {
        try {
            return inner.next();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
