/*
 * To oprogramowanie jest własnością
 *
 * OPI - Ośrodek Przetwarzania Informacji,
 * Al. Niepodległości 188B, 00-608 Warszawa
 * Numer KRS: 0000127372
 * Sąd Rejonowy dla m. st. Warszawy w Warszawie XII Wydział
 * Gospodarczy KRS
 * REGON: 006746090
 * NIP: 525-000-91-40
 * Wszystkie prawa zastrzeżone. To oprogramowanie może być używane tylko
 * zgodnie z przeznaczeniem. OPI nie odpowiada za ewentualne wadliwe
 * działanie kodu.
 */
package pl.sdadas.fsbrowser.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Shell;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;

import java.util.Arrays;

/**
 * @author Sławomir Dadas
 */
public class FileSystemUtils {

    public static boolean isSameFileSystem(FsConnection first, FsConnection second) {
        String firstName = getDefaultFs(first);
        String secondName = getDefaultFs(second);
        return StringUtils.equals(firstName, secondName);
    }

    public static String getDefaultFs(FsConnection connection) {
        Configuration config = connection.getConfig();
        return ObjectUtils.firstNonNull(config.get("fs.defaultFS"), config.get("fs.default.name"));
    }

    public static String formatByteCount(long bytes) {
        return formatByteCount(bytes, false);
    }

    public static String formatByteCount(long bytes, boolean si) {
        int unit = getMetricUnit(si);
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "i" : "");
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static boolean checkNativeLibraries() {
        if(Shell.WINDOWS && StringUtils.isBlank(Shell.WINUTILS)) {
            return false;
        }
        return true;
    }

    public static boolean isParentPath(Path parent, Path child) {
        String parentPath = parent.toUri().getPath();
        String childPath = child.toUri().getPath();
        return childPath.startsWith(parentPath);
    }

    public static String[] pathsToStrings(Path... paths) {
        return Arrays.stream(paths).map(path -> path.toUri().getPath()).toArray(String[]::new);
    }

    private static int getMetricUnit(boolean si) {
        return si ? 1000 : 1024;
    }

    private FileSystemUtils() {
    }
}
