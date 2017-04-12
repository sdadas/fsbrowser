package pl.sdadas.fsbrowser.common;

import pl.sdadas.fsbrowser.exception.FsException;

/**
 * @author Sławomir Dadas
 */
public interface UnsafeRunnable {

    void run() throws FsException;
}
