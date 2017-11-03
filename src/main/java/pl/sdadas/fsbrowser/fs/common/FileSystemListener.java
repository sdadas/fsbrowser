package pl.sdadas.fsbrowser.fs.common;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;

/**
 * @author Sławomir Dadas
 */
public interface FileSystemListener {

    void fileOpened(Path path, FSDataInputStream is);
}
