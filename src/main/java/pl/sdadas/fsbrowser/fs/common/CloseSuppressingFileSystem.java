package pl.sdadas.fsbrowser.fs.common;

import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

/**
 * @author Sławomir Dadas
 */
public class CloseSuppressingFileSystem extends DelegatingFileSystem {

    public CloseSuppressingFileSystem(FileSystem fs) {
        super(fs);
    }

    @Override
    public void close() throws IOException {
        /* DO NOTHING */
    }
}
