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
import pl.sdadas.fsbrowser.app.BeanFactory;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardAction;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.fs.connection.ConnectionConfig;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.fs.connection.HarFsConnection;
import pl.sdadas.fsbrowser.utils.FileSystemUtils;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.chmod.ChmodDialog;
import pl.sdadas.fsbrowser.view.chown.ChownDialog;
import pl.sdadas.fsbrowser.view.cleanup.CleanupDialog;
import pl.sdadas.fsbrowser.view.common.loading.Progress;
import pl.sdadas.fsbrowser.view.filebrowser.FileItem;
import pl.sdadas.fsbrowser.view.filebrowser.FileSystemTableModel;
import pl.sdadas.fsbrowser.view.filecontent.FileContentDialog;
import pl.sdadas.fsbrowser.view.mainwindow.MainPanel;
import pl.sdadas.fsbrowser.view.props.PropertiesDialog;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                .name("Check (fsck)")
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

    private void doCopyFromLocal(List<FileItem> selection, Progress progress) {
        File[] files = selectFiles();
        if(files.length == 0) return;

        ViewUtils.handleErrors(parent, () -> {
            parent.getConnection().copyFromLocal(files, parent.getModel().getCurrentPath(), progress);
            parent.getModel().reloadView();
        });
    }

    public FileAction copyToLocalAction() {
        return FileAction.builder(this::doCopyToLocal)
                .name("Copy to local")
                .icon("copy-to-local")
                .readOnly(true)
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
                .readOnly(true)
                .get();
    }

    private void doGoto(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () ->  {
            String path = parent.getModel().getCurrentPath().toUri().getPath();
            Object result = WebOptionPane.showInputDialog(parent, "Go to directory", "Go to directory",
                    WebOptionPane.QUESTION_MESSAGE, null, null, path);
            if(result != null && result instanceof String) {
                String value = StringUtils.strip((String) result);
                parent.clearFilterAndSort();
                parent.getModel().onFileClicked(new Path(value));
            }
        });
    }

    public FileAction previewFileAction() {
        return FileAction.builder(this::doPreviewFile)
                .name("Preview")
                .icon("preview")
                .readOnly(true)
                .predicates(this::singlePredicate)
                .get();
    }

    public void doPreviewFile(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            FileItem item = selection.get(0);
            if(item.isDirectory()) {
                parent.clearFilterAndSort();
                parent.getModel().onFileClicked(item.getPath());
            } else {
                FSDataInputStream stream = parent.getConnection().read(item.getPath());
                Window owner = SwingUtils.getWindowAncestor(parent);
                FileContentDialog dialog = new FileContentDialog(stream, item.getStatus().getLen(), owner);
                dialog.setTitle(item.getName());
                dialog.showDialog();
            }
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
                .readOnly(true)
                .get();
    }

    private void doRefresh(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () ->  {
            parent.getModel().reloadView();
        });
    }

    public FileAction renameAction() {
        return FileAction.builder(this::doRename)
                .name("Rename")
                .icon("file-rename")
                .predicates(this::singlePredicate)
                .get();
    }

    private void doRename(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () ->  {
            FileItem item = selection.get(0);
            Object result = WebOptionPane.showInputDialog(parent, "Rename to", "Rename",
                    WebOptionPane.QUESTION_MESSAGE, null, null, item.getName());
            if(result != null && result instanceof String) {
                String value = StringUtils.strip((String) result);
                parent.getConnection().rename(item.getPath(), value);
                parent.getModel().reloadView();
            }
        });
    }

    public FileAction chownAction() {
        return FileAction.builder(this::doChown)
                .name("Change owner/group")
                .icon("chown")
                .predicates(this::singlePredicate)
                .get();
    }

    private void doChown(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            FileItem item = selection.get(0);
            Path path = item.getPath();
            if(path == null) return;
            ChownDialog dialog = new ChownDialog(item, SwingUtils.getWindowAncestor(parent));
            ChownDialog.Result chown = dialog.showDialog();
            if(chown != null) {
                FsConnection connection = parent.getConnection();
                if(StringUtils.isNotBlank(chown.getOwner())) {
                    connection.chown(path, chown.getOwner(), chown.isRecursive());
                }
                if(StringUtils.isNotBlank(chown.getGroup())) {
                    connection.chgrp(path, chown.getGroup(), chown.isRecursive());
                }
                parent.getModel().reloadView();
            }
        });
    }

    public FileAction chmodAction() {
        return FileAction.builder(this::doChmod)
                .name("Change permissions")
                .icon("chmod")
                .predicates(this::singlePredicate)
                .get();
    }

    private void doChmod(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            FileItem item = selection.get(0);
            Path path = item.getPath();
            if(path == null) return;
            ChmodDialog dialog = new ChmodDialog(parent.getConnection(), path, SwingUtils.getWindowAncestor(parent));
            String chmod = dialog.showDialog();
            if(StringUtils.isNotBlank(chmod)) {
                parent.getConnection().chmod(path, chmod, dialog.isRecursive());
                parent.getModel().reloadView();
            }
        });
    }

    public FileAction archiveAction() {
        return FileAction.builder(this::doArchive)
                .name("Archive")
                .icon("har-create")
                .predicates(this::notEmptyPredicate)
                .get();
    }

    private void doArchive(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            String defaultName = selection.size() > 1 ? "archive.har" : selection.get(0).getName() + ".har";
            String defaultPath = parent.getModel().getCurrentPath().toUri().getPath() + "/" + defaultName;
            Object result = WebOptionPane.showInputDialog(parent, "Save archive to", "Archive path",
                    WebOptionPane.QUESTION_MESSAGE, null, null, defaultPath);
            if(result != null && result instanceof String) {
                String output = StringUtils.strip((String) result);
                if(!ViewUtils.requireNativeLibraries(parent)) return;
                if(!StringUtils.endsWith(output, ".har")) {
                    ViewUtils.error(parent, "Hadoop archive should have a .har extension.");
                    return;
                }
                Path workingDir = parent.getModel().getCurrentPath();
                Path[] sources = getItemPaths(selection);
                String archiveName = StringUtils.substringAfterLast(output, "/");
                String dest = StringUtils.removeEnd(output, archiveName);
                Path destPath = dest.startsWith("/") ? new Path(dest) : new Path(workingDir, dest);
                if(parent.getConnection().exists(new Path(output))) {
                    ViewUtils.error(parent, String.format("File %s already exists.", output));
                    return;
                }

                parent.getConnection().archive(archiveName, workingDir, sources, destPath);
                parent.getModel().reloadView();
            }
        });
    }

    public FileAction openArchiveAction() {
        return FileAction.builder(this::doOpenArchive)
                .name("Open as Hadoop Archive")
                .icon("har-open")
                .predicates(this::singlePredicate, this::dirsOnlyPredicate)
                .get();
    }

    private void doOpenArchive(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () -> {
            Path archivePath = selection.get(0).getPath();
            String archiveName = archivePath.getName();
            if(!StringUtils.endsWith(archiveName, ".har")) {
                ViewUtils.error(parent, "Hadoop archive should have a .har extension.");
                return;
            }

            HarFsConnection connection = new HarFsConnection(parent.getConnection(), archivePath);
            MainPanel mainPanel = BeanFactory.mainPanel();
            mainPanel.openFileSystemTab(connection, StringUtils.abbreviate(archiveName, 50));
        });
    }

    private void doLoadAllRows(List<FileItem> selection) {
        ViewUtils.handleErrors(parent, () ->  {
            FileSystemTableModel model = parent.getModel();
            if(model.hasMoreRows()) {
                model.loadAllRows();
            }
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
