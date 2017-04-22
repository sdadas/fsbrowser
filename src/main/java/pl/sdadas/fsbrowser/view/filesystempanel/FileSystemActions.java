package pl.sdadas.fsbrowser.view.filesystempanel;

import com.alee.extended.filechooser.WebDirectoryChooser;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.utils.SwingUtils;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardAction;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.fs.connection.ConnectionConfig;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.utils.FileSystemUtils;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.cleanup.CleanupDialog;
import pl.sdadas.fsbrowser.view.filebrowser.FileItem;
import pl.sdadas.fsbrowser.view.filecontent.FileContentDialog;
import pl.sdadas.fsbrowser.view.props.PropertiesDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Sławomir Dadas
 */
public class FileSystemActions {

    private final FileSystemPanel parent;

    public FileSystemActions(FileSystemPanel parent) {
        this.parent = parent;
    }

    public FileAction fsckAction() {
        return FileAction.builder(this::doFsck)
                .name("Check path (fsck)")
                .icon("properties")
                .predicates(this::singlePredicate)
                .get();
    }

    private void doFsck(List<FileItem> selection) {
        FileItem item = selection.get(0);
        FileStatus status = item.getStatus();
        if(status == null) return;

        ViewUtils.handleErrors(parent, () -> {
            Map<String, String> props = parent.getConnection().fsck(status.getPath());
            Window window = SwingUtils.getWindowAncestor(parent);
            PropertiesDialog dialog = new PropertiesDialog(props, window);
            dialog.setTitle("FSCK " + status.getPath().toUri().getPath());
            dialog.setVisible(true);
        });
    }

    public FileAction copyFromLocalAction() {
        return FileAction.builder(this::doCopyFromLocal)
                .name("Copy from local")
                .icon("copy-from-local")
                .get();
    }

    private void doCopyFromLocal(List<FileItem> selection) {
        File[] files = selectFiles();
        if(files.length == 0) return;

        ViewUtils.handleErrors(parent, () -> {
            parent.getConnection().copyFromLocal(files, parent.getModel().getCurrentPath());
            parent.getModel().reloadView();
        });
    }

    public FileAction copyToLocalAction() {
        return FileAction.builder(this::doCopyToLocal)
                .name("Copy to local")
                .icon("copy-to-local")
                .predicates(this::notEmptyPredicate)
                .get();
    }

    private void doCopyToLocal(List<FileItem> selection) {
        File dir = selectDirectory();
        if(dir == null) return;

        ViewUtils.handleErrors(parent, () -> {
            Path[] paths = selection.stream()
                    .filter(f -> f.isFile() || f.isDirectory())
                    .map(f -> f.getStatus().getPath())
                    .toArray(Path[]::new);
            parent.getConnection().copyToLocal(paths, dir);
        });
    }

    public FileAction removeAction() {
        return FileAction.builder((items) -> doRemove(items, true))
                .name("Delete permanently")
                .icon("item-remove")
                .get();
    }

    public FileAction moveToTrashAction() {
        return FileAction.builder((items) -> doRemove(items, false))
                .name("Move to trash")
                .icon("item-bin")
                .get();
    }

    private void doRemove(List<FileItem> selection, boolean skipTrash) {
        ViewUtils.handleErrors(parent, () -> {
            Path[] paths = getItemPaths(selection);
            if(paths.length == 0) return;
            String format = skipTrash ? "delete <b>%s</b>?" : "move <b>%s</b> to trash?</html>";
            String message = "<html>Do you want to " + String.format(format, getPathsName(paths));
            Window window = SwingUtils.getWindowAncestor(parent);
            int result = WebOptionPane.showConfirmDialog(window, message, "Confirm",
                    WebOptionPane.YES_NO_OPTION, WebOptionPane.QUESTION_MESSAGE);
            if(result == WebOptionPane.YES_OPTION) {
                parent.getConnection().remove(paths, skipTrash);
                parent.getModel().reloadView();
            }
        });
    }

    public FileAction mkdirAction() {
        return FileAction.builder(this::doMkdir)
                .name("Create directory (mkdir)")
                .icon("folder-new")
                .get();
    }

    private void doMkdir(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            Window window = SwingUtils.getWindowAncestor(parent);
            String value = WebOptionPane.showInputDialog(window, "Directory name", "Create directory",
                    WebOptionPane.QUESTION_MESSAGE);
            if(StringUtils.isNotBlank(value)) {
                Path path = new Path(parent.getModel().getCurrentPath(), value);
                parent.getConnection().mkdir(path);
                parent.getModel().reloadView();
            }
        });
    }

    public FileAction touchAction() {
        return FileAction.builder(this::doTouch)
                .name("Create file (touch)")
                .icon("file-new")
                .get();
    }

    private void doTouch(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            Window window = SwingUtils.getWindowAncestor(parent);
            String value = WebOptionPane.showInputDialog(window, "File name", "Create file",
                    WebOptionPane.QUESTION_MESSAGE);
            if(StringUtils.isNotBlank(value)) {
                Path path = new Path(parent.getModel().getCurrentPath(), value);
                parent.getConnection().touch(path);
                parent.getModel().reloadView();
            }
        });
    }

    public FileAction copyAction() {
        return FileAction.builder(this::doCopy)
                .name("Copy")
                .icon("copy")
                .predicates(this::notEmptyPredicate)
                .get();
    }

    public void doCopy(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            Path[] paths = getItemPaths(selection);
            if(paths.length == 0) return;
            ClipboardHelper clipboard = parent.getClipboard();
            clipboard.copy(parent, Lists.newArrayList(paths));
        });
    }

    public FileAction cutAction() {
        return FileAction.builder(this::doCut)
                .name("Cut")
                .icon("cut")
                .predicates(this::notEmptyPredicate)
                .get();
    }

    public void doCut(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            Path[] paths = getItemPaths(selection);
            if(paths.length == 0) return;
            ClipboardHelper clipboard = parent.getClipboard();
            clipboard.cut(parent, Lists.newArrayList(paths));
        });
    }

    public FileAction pasteAction() {
        return FileAction.builder(this::doPaste)
                .name("Paste")
                .icon("paste")
                .get();
    }

    public void doPaste(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            ClipboardHelper clipboard = parent.getClipboard();
            if(clipboard.isEmpty()) return;
            ClipboardHelper.Paths paths = clipboard.getPaths();
            doPaste(paths);
            clipboard.clear();
        });
    }

    public void doPaste(ClipboardHelper.Paths paths) throws FsException {
        List<Path> src = paths.getPaths();
        Path dest = parent.getModel().getCurrentPath();
        if(FileSystemUtils.isSameFileSystem(paths.getConnection(), parent.getConnection())) {
            FsConnection conn = parent.getConnection();
            if(ClipboardAction.COPY.equals(paths.getAction())) {
                conn.copy(src.toArray(new Path[src.size()]), dest);
            } else {
                conn.move(src.toArray(new Path[src.size()]), dest);
                if(!paths.getSource().isClosed()) {
                    paths.getSource().getModel().reloadView();
                }
            }
            parent.getModel().reloadView();
        } else {
            if(!ViewUtils.requireNativeLibraries(parent)) return;
                /* TODO: Ara you sure? */
            doDistCp(paths.getConnection(), parent.getConnection(), src, dest);
            if(!paths.getSource().isClosed()) {
                paths.getSource().getModel().reloadView();
            }
            parent.getModel().reloadView();
        }
    }

    private void doDistCp(FsConnection from, FsConnection to, List<Path> src, Path dest) throws FsException {
        ConnectionConfig config = new ConnectionConfig(to.getUser(), from.getConfig(), to.getConfig());
        FsConnection conn = new FsConnection(config);
        try {
            String[] srcUris = src.stream().map(p -> p.toUri().toString()).toArray(String[]::new);
            String destUri = dest.toUri().toString();
            conn.distCp(srcUris, destUri);
        } finally {
            IOUtils.closeQuietly(conn);
        }
    }

    public FileAction gotoAction() {
        return FileAction.builder(this::doGoto)
                .name("Go to directory")
                .icon("goto")
                .get();
    }

    private void doGoto(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () ->  {
            String path = parent.getModel().getCurrentPath().toUri().getPath();
            Object result = WebOptionPane.showInputDialog(parent, "Go to directory", "Go to directory",
                    WebOptionPane.QUESTION_MESSAGE, null, null, path);
            if(result != null && result instanceof String) {
                String value = StringUtils.strip((String) result);
                parent.getModel().onFileClicked(new Path(value));
            }
        });
    }

    public FileAction previewFileAction() {
        return FileAction.builder(this::doPreviewFile)
                .name("Preview file")
                .icon("preview")
                .predicates(this::singlePredicate, this::filesOnlyPredicate)
                .get();
    }

    public void doPreviewFile(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            FileItem item = selection.get(0);
            FSDataInputStream stream = parent.getConnection().read(item.getPath());
            Window owner = SwingUtils.getWindowAncestor(parent);
            FileContentDialog dialog = new FileContentDialog(stream, item.getStatus().getLen(), owner);
            dialog.setTitle(item.getName());
            dialog.showDialog();
        });
    }

    public FileAction cleanupAction() {
        return FileAction.builder(this::doCleanup)
                .name("Cleanup temporary directories")
                .icon("cleanup")
                .get();
    }

    private void doCleanup(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            Window owner = SwingUtils.getWindowAncestor(parent);
            CleanupDialog dialog = new CleanupDialog(parent.getConnection(), owner);
            CleanupDialog.Result result = dialog.showDialog();
            if(result != null) {
                parent.getConnection().cleanup(result.getPaths(), result.getDate());
            }
        });
    }

    public FileAction emptyTrashAction() {
        return FileAction.builder(this::doEmptyTrash)
                .name("Empty trash")
                .icon("empty-trash")
                .get();
    }

    private void doEmptyTrash(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            Window window = SwingUtils.getWindowAncestor(parent);
            String dir = String.format("/user/%s/.Trash", parent.getConnection().getUser());
            String message = String.format("<html>Do you want to clean <b>%s</b> directory?</html>", dir);
            int result = WebOptionPane.showConfirmDialog(window, message, "Confirm",
                    WebOptionPane.YES_NO_OPTION, WebOptionPane.QUESTION_MESSAGE);
            if(result == WebOptionPane.YES_OPTION) {
                parent.getConnection().emptyTrash();
                if(FileSystemUtils.isParentPath(parent.getModel().getCurrentPath(), new Path(dir))) {
                    parent.getModel().reloadView();
                }
            }
        });
    }

    public FileAction refreshAction() {
        return FileAction.builder(this::doRefresh)
                .name("Refresh current view")
                .icon("refresh")
                .get();
    }

    private void doRefresh(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () ->  {
            parent.getModel().reloadView();
        });
    }

    private Path[] getItemPaths(List<FileItem> selection) {
        return selection.stream().map(FileItem::getPath).filter(Objects::nonNull).toArray(Path[]::new);
    }

    private String getPathsName(Path[] paths) {
        int size = paths.length;
        return size > 1 ? String.format("%d items", size) : paths[0].getName();
    }

    private File selectDirectory() {
        WebDirectoryChooser chooser = new WebDirectoryChooser(SwingUtils.getWindowAncestor(parent));
        chooser.setVisible(true);
        if(chooser.getResult() == WebDirectoryChooser.OK_OPTION) {
            return chooser.getSelectedDirectory();
        }
        return null;
    }

    private File[] selectFiles() {
        WebFileChooser chooser = new WebFileChooser();
        chooser.setMultiSelectionEnabled(true);
        if(chooser.showOpenDialog(SwingUtils.getWindowAncestor(parent)) == WebFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFiles();
        }
        return new File[0];
    }

    private boolean singlePredicate(List<FileItem> selection) {
        return selection != null && selection.size() == 1;
    }

    private boolean filesOnlyPredicate(List<FileItem> selection) {
        return selection.stream().filter(f -> !f.isFile()).count() == 0;
    }

    private boolean dirsOnlyPredicate(List<FileItem> selection) {
        return selection.stream().filter(f -> !f.isDirectory()).count() == 0;
    }

    private boolean notEmptyPredicate(List<FileItem> selection) {
        return selection != null && !selection.isEmpty();
    }
}
