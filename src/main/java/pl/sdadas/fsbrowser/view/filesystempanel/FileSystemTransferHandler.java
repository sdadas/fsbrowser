package pl.sdadas.fsbrowser.view.filesystempanel;

import com.alee.utils.SwingUtils;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardAction;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.filebrowser.FileItem;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Sławomir Dadas
 */
public class FileSystemTransferHandler extends TransferHandler {

    private static DataFlavor pathsDataFlavor = createFileItemsDataFlavor();

    private final FileSystemPanel parent;

    public static DataFlavor createFileItemsDataFlavor() {
        try {
            return new DataFlavor("application/x-java-clipboard-paths");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public FileSystemTransferHandler(FileSystemPanel parent) {
        this.parent = parent;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if(!support.isDrop()) return false;
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                || support.isDataFlavorSupported(pathsDataFlavor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        try {
            Transferable transferable = support.getTransferable();
            if(transferable.isDataFlavorSupported(pathsDataFlavor)) {
                ClipboardHelper.Paths paths = (ClipboardHelper.Paths) transferable.getTransferData(pathsDataFlavor);
                parent.asyncAction(() -> moveFiles(paths));
            } else {
                List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                parent.asyncAction(() -> importFiles(files));
            }
        } catch (IOException | UnsupportedFlavorException ex) {
            SwingUtils.invokeLater(() -> ViewUtils.error(parent, ex.getMessage()));
        }
        return true;
    }

    private void moveFiles(ClipboardHelper.Paths paths) {
        ViewUtils.handleErrors(parent, () -> {
            parent.getActions().doPaste(paths);
        });
    }

    private void importFiles(List<File> files) {
        ViewUtils.handleErrors(parent, () -> {
            Path dest = parent.getModel().getCurrentPath();
            parent.getConnection().copyFromLocal(files.toArray(new File[files.size()]), dest);
            parent.getModel().reloadView();
        });
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
        super.exportDone(source, data, action);
    }

    @Override
    protected Transferable createTransferable(JComponent component) {
        List<FileItem> items = parent.getBrowser().selectedItems();
        return new ClipboardPathsTransferable(items, parent);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    private class ClipboardPathsTransferable implements Transferable {

        private ClipboardHelper.Paths data;

        public ClipboardPathsTransferable(List<FileItem> files, FileSystemPanel source) {
            List<Path> paths = files.stream()
                    .map(FileItem::getPath)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            this.data = new ClipboardHelper.Paths(source, paths, ClipboardAction.CUT);
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{pathsDataFlavor};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(pathsDataFlavor);
        }

        public ClipboardHelper.Paths getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return data;
        }
    }
}
