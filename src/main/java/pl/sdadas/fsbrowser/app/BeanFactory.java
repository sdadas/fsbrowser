package pl.sdadas.fsbrowser.app;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import pl.sdadas.fsbrowser.app.clipboard.ClipboardHelper;
import pl.sdadas.fsbrowser.app.config.AppConfigProvider;
import pl.sdadas.fsbrowser.app.config.AppConnection;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.fs.connection.ConnectionConfig;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.view.filebrowser.FileSystemTableModel;
import pl.sdadas.fsbrowser.view.locations.StatusBarHelper;
import pl.sdadas.fsbrowser.view.mainwindow.MainPanel;
import pl.sdadas.fsbrowser.view.mainwindow.MainWindow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

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
        return singleton(MainPanel.class, () -> {
            return new MainPanel(configProvider(), clipboardHelper(), executorService(), statusBarHelper());
        });
    }

    public static MainWindow mainWindow() {
        return singleton(MainWindow.class, () -> new MainWindow(mainPanel()));
    }

    public static AppConfigProvider configProvider() {
        return singleton(AppConfigProvider.class, AppConfigProvider::new);
    }

    public static ListeningExecutorService executorService() {
        return singleton(ListeningExecutorService.class, () -> {
            ExecutorService executor = Executors.newFixedThreadPool(3);
            return MoreExecutors.listeningDecorator(executor);
        });
    }

    public static StatusBarHelper statusBarHelper() {
        return singleton(StatusBarHelper.class, () -> {
            return new StatusBarHelper(configProvider());
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T singleton(Class<T> clazz, Supplier<T> supplier) {
        return (T) beans.computeIfAbsent(clazz, key -> supplier.get());
    }
}
