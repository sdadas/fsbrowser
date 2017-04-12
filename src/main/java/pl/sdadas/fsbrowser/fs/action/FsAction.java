package pl.sdadas.fsbrowser.fs.action;

import org.apache.hadoop.fs.FileSystem;
import org.springframework.data.hadoop.fs.FsShell;

/**
 * @author Sławomir Dadas
 */
public interface FsAction<T> {

    T execute(FsShell shell, FileSystem fs) throws Exception;
}
