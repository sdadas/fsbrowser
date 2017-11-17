package pl.sdadas.fsbrowser.fs.action;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.tools.HadoopArchives;
import org.apache.hadoop.util.ToolRunner;
import org.springframework.data.hadoop.fs.FsShell;
import pl.sdadas.fsbrowser.utils.FileSystemUtils;

import java.util.Arrays;

/**
 * @author Sławomir Dadas
 */
public class HadoopArchivesAction implements FsAction<Integer> {

    private String archiveName;

    private Path workingDir;

    private Path[] src;

    private Path dest;

    public HadoopArchivesAction(String archiveName, Path workingDir, Path[] src, Path dest) {
        this.archiveName = archiveName;
        this.workingDir = workingDir;
        this.src = src;
        this.dest = dest;
    }

    @Override
    public Integer execute(FsShell shell, FileSystem fs) throws Exception {
        String[] args = {"-archiveName", archiveName, "-p", workingDir.toUri().getPath(), "-r", "3"};
        String[] paths = ArrayUtils.add(this.createRelativeSourcePaths(), dest.toUri().toString());

        JobConf job = new JobConf(HadoopArchives.class);
        HadoopArchives archives = new HadoopArchives(job);
        return ToolRunner.run(fs.getConf(), archives, ArrayUtils.addAll(args, paths));
    }

    private String[] createRelativeSourcePaths() {
        String wd = workingDir.toUri().getPath();
        return Arrays.stream(FileSystemUtils.pathsToStrings(src))
                .map(val -> StringUtils.removeStart(val, wd))
                .map(val -> StringUtils.removeStart(val, "/"))
                .toArray(String[]::new);
    }
}
