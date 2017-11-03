package pl.sdadas.fsbrowser.fs.common;

import com.google.common.collect.MapMaker;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.utils.FileSystemUtils;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
public class ProgressFileSystemListener implements FileSystemListener {

    private ConcurrentMap<String, FSDataInputStream> files;

    private String lastFilePath;

    public ProgressFileSystemListener() {
        this.files = new MapMaker().weakValues().makeMap();
    }

    @Override
    public void fileOpened(Path path, FSDataInputStream is) {
        String key = path.toUri().getPath();
        files.put(key, is);
        lastFilePath = key;
    }

    public String getProgress() {
        StringBuilder builder = new StringBuilder();
        FSDataInputStream is = files.get(lastFilePath);
        if(is != null) {
            long pos = getPos(is);
            if(pos < 0) return "";
            String fileName = StringUtils.substringAfterLast(lastFilePath, "/");
            builder.append(StringUtils.abbreviateMiddle(fileName + ":", "...", 100));
            builder.append(' ');
            builder.append(FileSystemUtils.formatByteCount(pos));
            builder.append('\n');
        }
        return builder.toString();
    }

    private long getPos(FSDataInputStream is) {
        try {
            return is.getPos();
        } catch (IOException e) {
            return -1;
        }
    }
}
