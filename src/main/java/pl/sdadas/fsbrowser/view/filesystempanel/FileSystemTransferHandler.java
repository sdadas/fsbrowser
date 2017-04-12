package pl.sdadas.fsbrowser.view.filesystempanel;

import com.alee.utils.SwingUtils;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.filebrowser.FileBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class FileSystemTransferHandler extends TransferHandler {

    private final FileSystemPanel parent;

    public FileSystemTransferHandler(FileSystemPanel parent) {
        this.parent = parent;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if(!support.isDrop()) return false;
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        try {
            Transferable transferable = support.getTransferable();
            List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            parent.asyncAction(() -> importFiles(files));
        } catch (IOException | UnsupportedFlavorException ex) {
            SwingUtils.invokeLater(() -> ViewUtils.error(parent, ex.getMessage()));
        }
        return true;
    }

    private void importFiles(List<File> files) {
        ViewUtils.handleErrors(parent, () -> {
            Path dest = parent.getModel().getCurrentPath();
            parent.getConnection().copyFromLocal(files.toArray(new File[files.size()]), dest);
            parent.getModel().reloadView();
        });
    }
}
