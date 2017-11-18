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
public class ListenableFileSystem extends DelegatingFileSystem {

    private final List<FileSystemListener> listeners;

    public ListenableFileSystem(FileSystem fs) {
        super(fs);
        this.listeners = new ArrayList<>();
    }

    public void addFileSystemListener(FileSystemListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public FSDataInputStream open(Path f, int bufferSize) throws IOException {
        FSDataInputStream is = super.open(f, bufferSize);
        listeners.forEach(listener -> listener.fileOpened(f, is));
        return is;
    }

    @Override
    public FSDataInputStream open(Path f) throws IOException {
        FSDataInputStream is = super.open(f);
        listeners.forEach(listener -> listener.fileOpened(f, is));
        return is;
    }
}