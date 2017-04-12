package pl.sdadas.fsbrowser.view.filebrowser;

import java.util.function.Function;

/**
 * @author Sławomir Dadas
 */
public class FileBrowserColumn {

    private String name;

    private Integer minWidth;

    private Integer maxWidth;

    private Integer width;

    private Class<?> columnClass;

    private Function<FileItem, ?> columnProvider;

    public static <T> FileBrowserColumn create(String name, Class<T> clazz, Function<FileItem, T> columnProvider) {
        return new FileBrowserColumn(name, clazz, columnProvider);
    }

    public static FileBrowserColumn create(String name, Function<FileItem, String> columnProvider) {
        return new FileBrowserColumn(name, String.class, columnProvider);
    }

    private FileBrowserColumn(String name, Class<?> columnClass, Function<FileItem, ?> columnProvider) {
        this.name = name;
        this.columnClass = columnClass;
        this.columnProvider = columnProvider;
    }

    public String getName() {
        return name;
    }

    public Integer getMinWidth() {
        return minWidth;
    }

    public FileBrowserColumn minWidth(Integer value) {
        this.minWidth = value;
        return this;
    }

    public Integer getMaxWidth() {
        return maxWidth;
    }

    public FileBrowserColumn maxWidth(Integer value) {
        this.maxWidth = value;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public FileBrowserColumn width(Integer value) {
        this.width = value;
        return this;
    }

    public Class<?> getColumnClass() {
        return columnClass;
    }

    public Function<FileItem, ?> getColumnProvider() {
        return columnProvider;
    }
}
