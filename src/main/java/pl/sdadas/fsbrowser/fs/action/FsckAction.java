package pl.sdadas.fsbrowser.fs.action;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.tools.DFSck;
import org.springframework.data.hadoop.fs.FsShell;
import pl.sdadas.fsbrowser.utils.FileSystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sławomir Dadas
 */
public class FsckAction implements FsAction<Map<String,String>> {

    private final static Pattern BYTES_PATTERN = Pattern.compile("(\\d+)\\s+B");

    private final Path path;

    public FsckAction(Path path) {
        this.path = path;
    }

    @Override
    public Map<String, String> execute(FsShell shell, FileSystem fs) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printer = new PrintStream(out, true, StandardCharsets.UTF_8.name());
        DFSck fsck = new DFSck(fs.getConf(), printer);
        fsck.run(new String[]{path.toUri().getPath()});
        String res = new String(out.toByteArray(), StandardCharsets.UTF_8);
        IOUtils.closeQuietly(printer);
        return parseOutput(res);
    }

    private Map<String, String> parseOutput(String value) {
        int startIdx = StringUtils.indexOf(value, "Status");
        if(startIdx < 0) return Collections.emptyMap();
        String props = StringUtils.substring(value, startIdx);
        Map<String, String> result = new LinkedHashMap<>();

        LineIterator iter = IOUtils.lineIterator(new StringReader(props));
        int unnamedIdx = 1;
        while(iter.hasNext()) {
            String line = iter.next();
            if(StringUtils.isBlank(line)) continue;
            int colons = StringUtils.countMatches(line, ':');
            if(colons == 1) {
                String key = StringUtils.strip(StringUtils.substringBefore(line, ":"));
                String val = StringUtils.strip(StringUtils.substringAfter(line, ":"));
                result.put(key, replaceBytes(val));
            } else {
                String key = "#" + unnamedIdx++;
                String val = StringUtils.strip(line);
                result.put(key, val);
            }
        }
        return result;
    }

    private String replaceBytes(String value) {
        Matcher matcher = BYTES_PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            long bytes = Long.parseLong(matcher.group(1));
            matcher.appendReplacement(sb, FileSystemUtils.formatByteCount(bytes));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
