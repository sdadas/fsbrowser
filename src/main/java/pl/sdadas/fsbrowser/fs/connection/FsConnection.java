package pl.sdadas.fsbrowser.fs.connection;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.tools.HadoopArchives;
import org.springframework.data.hadoop.fs.DistCp;
import org.springframework.data.hadoop.fs.FsShell;
import pl.sdadas.fsbrowser.exception.FsAccessException;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.fs.action.*;
import pl.sdadas.fsbrowser.fs.common.ProgressFileSystemListener;
import pl.sdadas.fsbrowser.utils.FileSystemUtils;
import pl.sdadas.fsbrowser.view.common.loading.Progress;

import java.io.*;
import java.security.PrivilegedExceptionAction;
import java.util.*;

/**
 * @author Sławomir Dadas
 */
public class FsConnection implements Closeable {

    private final ConnectionConfig config;

    private FileSystem fs;

    private FsShell shell;

    public FsConnection(ConnectionConfig config) {
        this.config = config;
        init();
    }

    private void init() {
        try {
            Configuration configuration = config.getConfiguration();
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser(config.getUser());
            ugi.doAs((PrivilegedExceptionAction<Void>) () -> {
                this.fs = FileSystem.get(configuration);
                this.shell = new FsShell(configuration, fs);
                return null;
            });
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> T execute(FsAction<T> action) throws FsException {
        return execute(false, action);
    }

    public <T> T execute(boolean separateConnection, FsAction<T> action) throws FsException {
        try {
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser(config.getUser());
            return ugi.doAs((PrivilegedExceptionAction<T>) () -> {
                Configuration conf = config.getConfiguration();
                FileSystem actionFs = separateConnection ? FileSystem.newInstance(conf) : this.fs;
                FsShell actionShell = separateConnection ? new FsShell(conf, actionFs) : this.shell;
                T ret = action.execute(actionShell, actionFs);
                if(separateConnection) {
                    actionShell.close();
                    actionFs.close();
                }
                return ret;
            });
        } catch (AccessControlException ex) {
            throw new FsAccessException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FsException("Problem invoking file system action: " + ex.getMessage(), ex);
        }
    }

    public FsIterator list(Path path) throws FsException {
        return execute((shell, fs) -> new FsIterator(shell, fs, fs.listLocatedStatus(path)));
    }

    public FileStatus status(String path) throws FsException {
        return execute((shell, fs) -> fs.getFileStatus(new Path(path)));
    }

    public FileStatus status(Path path) throws FsException {
        return execute((shell, fs) -> fs.getFileStatus(path));
    }

    public Map<String, String> fsck(Path path) throws FsException {
        return execute(new FsckAction(path));
    }

    public void copyFromLocal(File[] files, Path dest) throws FsException {
        execute(new CopyFromLocalAction(files, dest));
    }

    public void copyFromLocal(File[] files, Path dest, Progress progress) throws FsException {
        execute(new CopyFromLocalAction(files, dest, progress));
    }

    public void copyToLocal(Path[] paths, File dest) throws FsException {
        execute((shell, fs) ->  {
            for (Path path : paths) {
                String src = path.toUri().getPath();
                String target = new File(dest, path.getName()).getAbsolutePath();
                shell.copyToLocal(src, target);
            }
            return null;
        });
    }

    public void remove(Path[] paths, boolean skipTrash) throws FsException {
        String[] items = FileSystemUtils.pathsToStrings(paths);
        execute((shell, fs) -> {
            shell.rmr(skipTrash, items);
            return null;
        });
    }

    public void mkdir(Path path) throws FsException {
        execute((shell, fs) -> {
            shell.mkdir(path.toUri().getPath());
            return null;
        });
    }

    public void touch(Path... paths) throws FsException {
        String[] items = FileSystemUtils.pathsToStrings(paths);
        execute((shell, fs) -> {
            shell.touchz(items);
            return null;
        });
    }

    public void copy(Path[] src, Path dest) throws FsException {
        doCopy(src, dest, false);
    }

    public void move(Path[] src, Path dest) throws FsException {
        doCopy(src, dest, true);
    }

    public void rename(Path path, String name) throws FsException {
        execute((shell, fs) -> {
            Path to = new Path(path.getParent(), name);
            fs.rename(path, to);
            return null;
        });
    }

    public void distCp(String [] from, String to) throws FsException {
        execute((shell, fs) ->  {
            DistCp tool = new DistCp(this.getConfig(), null);
            EnumSet<DistCp.Preserve> preserve = EnumSet.noneOf(DistCp.Preserve.class);
            String[] uris = ArrayUtils.add(from, to);
            tool.copy(preserve, false, false, false, false, uris);
            return null;
        });
    }

    public void archive(String archiveName, Path workingDir, Path[] src, Path dest) throws FsException {
        Validate.isTrue(StringUtils.endsWithIgnoreCase(archiveName, ".har"), "archiveName should end with .har");
        execute(new HadoopArchivesAction(archiveName, workingDir, src, dest));
    }

    private void doCopy(Path[] src, Path dest, boolean deleteSrc) throws FsException {
        List<String> paths = Lists.newArrayList(FileSystemUtils.pathsToStrings(src));
        execute((shell, fs) -> {
            String target = dest.toUri().getPath();
            String first = paths.get(0);
            String second = paths.size() > 2 ? paths.get(1) : target;
            String [] rest;
            if(paths.size() > 2) {
                paths.add(target);
                rest = ArrayUtils.subarray(paths.toArray(new String[paths.size()]), 2, paths.size());
            } else {
                rest = new String[0];
            }

            if(deleteSrc) {
                shell.mv(first, second, rest);
            } else {
                shell.cp(first, second, rest);
            }
            return null;
        });
    }

    public FSDataInputStream read(Path path) throws FsException {
        return execute((shell, fs) -> fs.open(path));
    }

    public boolean exists(Path path) throws FsException {
        return execute((shell, fs) -> fs.exists(path));
    }

    public void emptyTrash() throws FsException {
        execute((shell, fs) -> {
            shell.expunge();
            return null;
        });
    }

    public void cleanup(List<Path> paths, Date olderThan) throws FsException {
        execute(new CleanupAction(paths, olderThan));
    }

    public void chmod(Path path, String chmod, boolean recursive) throws FsException {
        execute((shell, fs) -> {
            shell.chmod(recursive, chmod, path.toUri().getPath());
            return null;
        });
    }

    public void chown(Path path, String owner, boolean recursive) throws FsException {
        execute((shell, fs) -> {
            String uri = path.toUri().getPath();
            FsShellPermissions.changePermissions(fs, fs.getConf(), FsShellPermissions.Op.CHOWN, recursive, owner, uri);
            return null;
        });
    }

    public void chgrp(Path path, String group, boolean recursive) throws FsException {
        execute((shell, fs) -> {
            String uri = path.toUri().getPath();
            FsShellPermissions.changePermissions(fs, fs.getConf(), FsShellPermissions.Op.CHGRP, recursive, group, uri);
            return null;
        });
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(fs);
        IOUtils.closeQuietly(shell);
    }

    public Configuration getConfig() {
        return this.config.getConfiguration();
    }

    public String getUser() {
        return config.getUser();
    }
}
