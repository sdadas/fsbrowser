package pl.sdadas.fsbrowser.fs.connection;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsShell;
import org.springframework.beans.BeanUtils;
import org.springframework.data.hadoop.HadoopException;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author Sławomir Dadas
 */
public class FsShellPermissions {

    private static boolean IS_HADOOP_20X = ClassUtils.isPresent("org.apache.hadoop.fs.FsShellPermissions$Chmod",
            FsShellPermissions.class.getClassLoader());

    public enum Op {
        CHOWN("-chown", "Chown"), CHMOD("-chmod", "Chmod"), CHGRP("-chgrp", "Chgrp");

        private final String cmd;

        private final String innerClass;

        Op(String cmd, String innerClass) {
            this.cmd = cmd;
            this.innerClass = innerClass;
        }

        public String getCmd() {
            return cmd;
        }

        public String getInnerClass() {
            return innerClass;
        }
    }

    static <T> T[] concatAll(T[] first, T[]... rest) {
        // can add some sanity checks
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    static void changePermissions(FileSystem fs, Configuration config, Op op,
                                  boolean recursive, String group, String... uris) {

        String[] argvs = new String[0];
        if (recursive) {
            ObjectUtils.addObjectToArray(argvs, "-R");
        }
        argvs = concatAll(argvs, new String[] { group }, uris);
        ClassLoader classLoader = config.getClass().getClassLoader();

        // Hadoop 1.0.x
        if (!IS_HADOOP_20X) {
            Class<?> cls = ClassUtils.resolveClassName("org.apache.hadoop.fs.FsShellPermissions", classLoader);
            Object[] args = new Object[] { fs, op.getCmd(), argvs, 0, new FsShell(config) };

            Method m = ReflectionUtils.findMethod(cls, "changePermissions",
                    FileSystem.class, String.class, String[].class, int.class, FsShell.class);
            ReflectionUtils.makeAccessible(m);
            ReflectionUtils.invokeMethod(m, null, args);
        }
        // Hadoop 2.x
        else {
            Class<?> cmd = ClassUtils.resolveClassName("org.apache.hadoop.fs.shell.Command", classLoader);
            String targetClzName = "org.apache.hadoop.fs.FsShellPermissions$" + op.getInnerClass();
            Class<?> targetClz = ClassUtils.resolveClassName(targetClzName, classLoader);
            Configurable target = (Configurable) BeanUtils.instantiate(targetClz);
            target.setConf(config);
            LinkedList<String> args = new LinkedList<String>(Arrays.asList(argvs));
            try {
                Method m = ReflectionUtils.findMethod(cmd, "processOptions", LinkedList.class);
                ReflectionUtils.makeAccessible(m);
                ReflectionUtils.invokeMethod(m, target, args);
                m = ReflectionUtils.findMethod(cmd, "processRawArguments", LinkedList.class);
                ReflectionUtils.makeAccessible(m);
                ReflectionUtils.invokeMethod(m, target, args);
            } catch (IllegalStateException ex){
                throw new HadoopException("Cannot change permissions/ownership " + ex);
            }
        }
    }
}
