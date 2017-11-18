package pl.sdadas.fsbrowser.fs.connection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.HarFileSystem;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.fs.common.CloseSuppressingFileSystem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author Sławomir Dadas
 */
public class HarFsConnection extends FsConnection {

    private final FileSystem parentFileSystem;

    public HarFsConnection(FsConnection other, Path harFile) {
        super(other.getConnectionConfig());
        this.setReadOnly(true);
        this.setRoot(getHarPath(harFile));
        this.parentFileSystem = new CloseSuppressingFileSystem(other.getFileSystem());
    }

    @Override
    protected FileSystem createNewFileSystem(Configuration configuration) throws IOException {
        HarFileSystem fs = new HarFileSystem(this.parentFileSystem);
        fs.initialize(this.getRoot().toUri(), configuration);
        return fs;
    }

    @Override
    protected FileSystem createSharedFileSystem(Configuration configuration) throws IOException {
        return this.getFileSystem() != null ? this.getFileSystem() : createNewFileSystem(configuration);
    }

    private URI getHarUri(Path file) {
        URI uri = file.toUri();
        try {
            return new URI("har", uri.getScheme() + "-" + uri.getAuthority(), uri.getPath(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path getHarPath(Path file) {
        return new Path(getHarUri(file));
    }

    @Override
    protected Path fsPath(Path path) {
        String relative = StringUtils.stripToNull(StringUtils.removeStart(path.toUri().getPath(), "/"));
        return relative != null ? path : this.getRoot();
    }
}
