package pl.sdadas.fsbrowser.exception;

/**
 * @author Sławomir Dadas
 */
public class FsAccessException extends FsException {

    public FsAccessException() {
    }

    public FsAccessException(String message) {
        super(message);
    }

    public FsAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public FsAccessException(Throwable cause) {
        super(cause);
    }

    public FsAccessException(String message, Throwable cause, boolean suppression, boolean writableStackTrace) {
        super(message, cause, suppression, writableStackTrace);
    }
}
