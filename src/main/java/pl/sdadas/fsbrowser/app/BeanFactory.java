package pl.sdadas.fsbrowser.app;

import com.google.common.collect.Lists;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.exception.FsAccessException;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.fs.connection.ConnectionConfig;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.view.filebrowser.FileSystemTableModel;
import pl.sdadas.fsbrowser.view.filesystempanel.FileSystemPanel;
import pl.sdadas.fsbrowser.view.mainwindow.MainPanel;
import pl.sdadas.fsbrowser.view.mainwindow.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Sławomir Dadas
 */
public final class BeanFactory {

    private static Map<Class<?>, Object> beans = new HashMap<>();

    private BeanFactory() {
    }

    public static FsConnection connection(AppConnection connection) {
        ConnectionConfig config = new ConnectionConfig(connection.getUser(), connection.getPropertiesMap());
        return new FsConnection(config);
    }

    public static FileSystemTableModel tableModel(FsConnection connection) {
        try {
            return new FileSystemTableModel(connection, "/");
        } catch (FsException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ClipboardHelper clipboardHelper() {
        return singleton(ClipboardHelper.class, ClipboardHelper::new);
    }

    public static MainPanel mainPanel() {
        return singleton(MainPanel.class, () -> new MainPanel(configProvider(), clipboardHelper()));
    }

    public static MainWindow mainWindow() {
        return singleton(MainWindow.class, () -> new MainWindow(mainPanel()));
    }

    public static AppConfigProvider configProvider() {
        return singleton(AppConfigProvider.class, AppConfigProvider::new);
    }

    @SuppressWarnings("unchecked")
    private static <T> T singleton(Class<T> clazz, Supplier<T> supplier) {
        return (T) beans.computeIfAbsent(clazz, key -> supplier.get());
    }
}
