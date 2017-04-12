package pl.sdadas.fsbrowser.exception;

/**
 * @author Sławomir Dadas
 */
public class FsException extends Exception {

    public FsException() {
    }

    public FsException(String message) {
        super(message);
    }

    public FsException(String message, Throwable cause) {
        super(message, cause);
    }

    public FsException(Throwable cause) {
        super(cause);
    }

    public FsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
