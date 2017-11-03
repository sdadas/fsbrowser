/*
 * Copyright 2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.sdadas.fsbrowser.fs.common;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.util.Progressable;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class ListenableFileSystem extends FileSystem {

    private final FileSystem fs;

    private final List<FileSystemListener> listeners;

    public ListenableFileSystem(FileSystem fs) {
        this.fs = fs;
        this.listeners = new ArrayList<>();
    }

    public void addFileSystemListener(FileSystemListener listener) {
        this.listeners.add(listener);
    }

    public void setConf(Configuration conf) {
        if (fs != null) {
            fs.setConf(conf);
        }
    }

    public Configuration getConf() {
        return fs.getConf();
    }

    public int hashCode() {
        return fs.hashCode();
    }

    public boolean equals(Object obj) {
        return fs.equals(obj);
    }

    public void initialize(URI name, Configuration conf) throws IOException {
        fs.initialize(name, conf);
    }

    public URI getUri() {
        return fs.getUri();
    }

    public String getCanonicalServiceName() {
        return fs.getCanonicalServiceName();
    }

    public String getName() {
        return fs.getName();
    }

    public String toString() {
        return fs.toString();
    }

    public Path makeQualified(String path) {
        return fs.makeQualified(path(path));
    }

    public Path makeQualified(Path path) {
        return fs.makeQualified(path);
    }

    public BlockLocation[] getFileBlockLocations(FileStatus file, long start, long len) throws IOException {
        return fs.getFileBlockLocations(file, start, len);
    }

    public FSDataInputStream open(Path f, int bufferSize) throws IOException {
        FSDataInputStream is = fs.open(f, bufferSize);
        listeners.forEach(listener -> listener.fileOpened(f, is));
        return is;
    }

    public FSDataInputStream open(Path f) throws IOException {
        FSDataInputStream is = fs.open(f);
        listeners.forEach(listener -> listener.fileOpened(f, is));
        return is;
    }

    public FSDataOutputStream create(Path f) throws IOException {
        return fs.create(f);
    }

    public FSDataOutputStream create(Path f, boolean overwrite) throws IOException {
        return fs.create(f, overwrite);
    }

    public FSDataOutputStream create(Path f, Progressable progress) throws IOException {
        return fs.create(f, progress);
    }

    public FSDataOutputStream create(Path f, short replication) throws IOException {
        return fs.create(f, replication);
    }

    public FSDataOutputStream create(Path f, short replication, Progressable progress) throws IOException {
        return fs.create(f, replication, progress);
    }

    public FSDataOutputStream create(Path f, boolean overwrite, int bufferSize) throws IOException {
        return fs.create(f, overwrite, bufferSize);
    }

    public FSDataOutputStream create(Path f, boolean overwrite, int bufferSize, Progressable progress)
            throws IOException {
        return fs.create(f, overwrite, bufferSize, progress);
    }

    public FSDataOutputStream create(Path f, boolean overwrite, int bufferSize, short replication, long blockSize)
            throws IOException {
        return fs.create(f, overwrite, bufferSize, replication, blockSize);
    }

    public FSDataOutputStream create(Path f, boolean overwrite, int bufferSize, short replication,
                                     long blockSize, Progressable progress)
            throws IOException {
        return fs.create(f, overwrite, bufferSize, replication, blockSize, progress);
    }

    public FSDataOutputStream create(Path f, FsPermission permission, boolean overwrite, int bufferSize,
                                     short replication, long blockSize, Progressable progress)
            throws IOException {
        return fs.create(f, permission, overwrite, bufferSize, replication, blockSize, progress);
    }

    public boolean createNewFile(Path f) throws IOException {
        return fs.createNewFile(f);
    }

    public FSDataOutputStream append(Path f) throws IOException {
        return fs.append(f);
    }

    public FSDataOutputStream append(Path f, int bufferSize) throws IOException {
        return fs.append(f, bufferSize);
    }

    public FSDataOutputStream append(Path f, int bufferSize, Progressable progress) throws IOException {
        return fs.append(f, bufferSize, progress);
    }

    public short getReplication(Path src) throws IOException {
        return fs.getReplication(src);
    }

    public boolean setReplication(Path src, short replication) throws IOException {
        return fs.setReplication(src, replication);
    }

    public boolean rename(Path src, Path dst) throws IOException {
        return fs.rename(src, dst);
    }

    public boolean delete(Path f) throws IOException {
        return fs.delete(f);
    }

    public boolean delete(Path f, boolean recursive) throws IOException {
        return fs.delete(f, recursive);
    }

    public boolean deleteOnExit(Path f) throws IOException {
        return fs.deleteOnExit(f);
    }

    public boolean exists(Path f) throws IOException {
        return fs.exists(f);
    }

    public boolean isDirectory(Path f) throws IOException {
        return fs.isDirectory(f);
    }

    public boolean isFile(Path f) throws IOException {
        return fs.isFile(f);
    }

    public long getLength(Path f) throws IOException {
        return fs.getLength(f);
    }

    public ContentSummary getContentSummary(Path f) throws IOException {
        return fs.getContentSummary(f);
    }

    public FileStatus[] listStatus(Path f) throws IOException {
        return fs.listStatus(f);
    }

    public FileStatus[] listStatus(Path f, PathFilter filter) throws IOException {
        return fs.listStatus(f, filter);
    }

    public FileStatus[] listStatus(Path[] files) throws IOException {
        return fs.listStatus(files);
    }

    public FileStatus[] listStatus(Path[] files, PathFilter filter) throws IOException {
        return fs.listStatus(files, filter);
    }

    public FileStatus[] globStatus(Path pathPattern) throws IOException {
        return fs.globStatus(pathPattern);
    }

    public FileStatus[] globStatus(Path pathPattern, PathFilter filter) throws IOException {
        return fs.globStatus(pathPattern, filter);
    }

    public Path getHomeDirectory() {
        return fs.getHomeDirectory();
    }

    public Token<?> getDelegationToken(String renewer) throws IOException {
        return fs.getDelegationToken(renewer);
    }

    public void setWorkingDirectory(Path new_dir) {
        fs.setWorkingDirectory(new_dir);
    }

    public Path getWorkingDirectory() {
        return fs.getWorkingDirectory();
    }

    public boolean mkdirs(Path f) throws IOException {
        return fs.mkdirs(f);
    }

    public boolean mkdirs(Path f, FsPermission permission) throws IOException {
        return fs.mkdirs(f, permission);
    }


    public void copyFromLocalFile(Path src, Path dst) throws IOException {
        fs.copyFromLocalFile(src, dst);
    }

    public void moveFromLocalFile(Path[] srcs, Path dst) throws IOException {
        fs.moveFromLocalFile(srcs, dst);
    }

    public void moveFromLocalFile(Path src, Path dst) throws IOException {
        fs.moveFromLocalFile(src, dst);
    }

    public void copyFromLocalFile(boolean delSrc, Path src, Path dst) throws IOException {
        fs.copyFromLocalFile(delSrc, src, dst);
    }

    public void copyFromLocalFile(boolean delSrc, boolean overwrite, Path[] srcs, Path dst) throws IOException {
        fs.copyFromLocalFile(delSrc, overwrite, srcs, dst);
    }

    public void copyFromLocalFile(boolean delSrc, boolean overwrite, Path src, Path dst) throws IOException {
        fs.copyFromLocalFile(delSrc, overwrite, src, dst);
    }

    public void copyToLocalFile(Path src, Path dst) throws IOException {
        fs.copyToLocalFile(src, dst);
    }

    public void moveToLocalFile(Path src, Path dst) throws IOException {
        fs.moveToLocalFile(src, dst);
    }

    public void copyToLocalFile(boolean delSrc, Path src, Path dst) throws IOException {
        fs.copyToLocalFile(delSrc, src, dst);
    }

    public Path startLocalOutput(Path fsOutputFile, Path tmpLocalFile) throws IOException {
        return fs.startLocalOutput(fsOutputFile, tmpLocalFile);
    }

    public void completeLocalOutput(Path fsOutputFile, Path tmpLocalFile) throws IOException {
        fs.completeLocalOutput(fsOutputFile, tmpLocalFile);
    }

    public void close() throws IOException {
        fs.close();
    }

    public long getUsed() throws IOException {
        return fs.getUsed();
    }

    public long getBlockSize(Path f) throws IOException {
        return fs.getBlockSize(f);
    }

    public long getDefaultBlockSize() {
        return fs.getDefaultBlockSize();
    }

    public short getDefaultReplication() {
        return fs.getDefaultReplication();
    }

    public FileStatus getFileStatus(Path f) throws IOException {
        return fs.getFileStatus(f);
    }

    public FileChecksum getFileChecksum(Path f) throws IOException {
        return fs.getFileChecksum(f);
    }

    public void setVerifyChecksum(boolean verifyChecksum) {
        fs.setVerifyChecksum(verifyChecksum);
    }

    public void setPermission(Path p, FsPermission permission) throws IOException {
        fs.setPermission(p, permission);
    }

    public void setOwner(Path p, String username, String groupname) throws IOException {
        fs.setOwner(p, username, groupname);
    }

    public void setTimes(Path p, long mtime, long atime) throws IOException {
        fs.setTimes(p, mtime, atime);
    }

    private Path path(String path) {
        return new Path(path);
    }
}