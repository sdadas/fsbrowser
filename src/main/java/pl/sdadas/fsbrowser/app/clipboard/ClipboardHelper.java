/*
 * To oprogramowanie jest własnością
 *
 * OPI - Ośrodek Przetwarzania Informacji,
 * Al. Niepodległości 188B, 00-608 Warszawa
 * Numer KRS: 0000127372
 * Sąd Rejonowy dla m. st. Warszawy w Warszawie XII Wydział
 * Gospodarczy KRS
 * REGON: 006746090
 * NIP: 525-000-91-40
 * Wszystkie prawa zastrzeżone. To oprogramowanie może być używane tylko
 * zgodnie z przeznaczeniem. OPI nie odpowiada za ewentualne wadliwe
 * działanie kodu.
 */
package pl.sdadas.fsbrowser.app.clipboard;

import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.view.filesystempanel.FileSystemPanel;

import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class ClipboardHelper {

    private Paths paths;

    public void copy(FileSystemPanel source, List<Path> paths) {
        this.paths = new Paths(source, paths, ClipboardAction.COPY);
    }

    public void cut(FileSystemPanel source, List<Path> paths) {
        this.paths = new Paths(source, paths, ClipboardAction.CUT);
    }

    public void clear() {
        this.paths = null;
    }

    public boolean isEmpty() {
        return paths == null;
    }

    public Paths getPaths() {
        return this.paths;
    }

    public static class Paths {

        private FileSystemPanel source;

        private List<Path> paths;

        private ClipboardAction action;

        public Paths(FileSystemPanel source, List<Path> paths, ClipboardAction action) {
            this.source = source;
            this.paths = paths;
            this.action = action;
        }

        public FsConnection getConnection() {
            return source.getConnection();
        }

        public FileSystemPanel getSource() {
            return source;
        }

        public List<Path> getPaths() {
            return paths;
        }

        public void setPaths(List<Path> paths) {
            this.paths = paths;
        }

        public ClipboardAction getAction() {
            return action;
        }
    }
}
