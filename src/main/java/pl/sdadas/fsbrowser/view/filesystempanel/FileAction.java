package pl.sdadas.fsbrowser.view.filesystempanel;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.view.common.loading.Progress;
import pl.sdadas.fsbrowser.view.filebrowser.FileItem;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Sławomir Dadas
 */
public class FileAction {

    private final Consumer<List<FileItem>> runnable;

    private final BiConsumer<List<FileItem>, Progress> progressRunnable;

    private List<ActionPredicate> predicates = Lists.newArrayList();

    private Icon icon = IconFactory.getIcon("file");

    private String name;

    private String tooltip;

    public static FileAction.Buider builder(Consumer<List<FileItem>> runnable) {
        Validate.notNull(runnable);
        return new Buider(runnable);
    }

    public static FileAction.Buider builder(BiConsumer<List<FileItem>, Progress> runnable) {
        Validate.notNull(runnable);
        return new Buider(runnable);
    }

    private FileAction(Consumer<List<FileItem>> action) {
        this.runnable = action;
        this.progressRunnable = null;
    }

    private FileAction(BiConsumer<List<FileItem>, Progress> action) {
        this.progressRunnable = action;
        this.runnable = null;
    }

    public boolean canAcitvate(List<FileItem> selection) {
        for (ActionPredicate predicate : predicates) {
            if(!predicate.test(selection)) return false;
        }
        return true;
    }

    public void run(List<FileItem> selection) {
        if(!canAcitvate(selection)) return;
        if(supportsProgress()) {
            this.progressRunnable.accept(selection, null);
        } else {
            this.runnable.accept(selection);
        }
    }

    public void run(List<FileItem> selection, Progress progress) {
        if(!canAcitvate(selection)) return;
        if(supportsProgress()) {
            this.progressRunnable.accept(selection, progress);
        } else {
            throw new IllegalArgumentException("This action does not support progress");
        }
    }

    public boolean supportsProgress() {
        return this.progressRunnable != null;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public List<ActionPredicate> getPredicates() {
        return predicates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public static class Buider {

        private final FileAction action;

        public Buider(Consumer<List<FileItem>> runnable) {
            this.action = new FileAction(runnable);
        }

        public Buider(BiConsumer<List<FileItem>, Progress> runnable) {
            this.action = new FileAction(runnable);
        }

        public Buider icon(String value) {
            this.action.setIcon(IconFactory.getIcon(value));
            return this;
        }

        public Buider predicates(ActionPredicate... values) {
            this.action.predicates.addAll(Arrays.asList(values));
            return this;
        }

        public Buider name(String value) {
            this.action.name = value;
            return this;
        }

        public Buider tooltip(String value) {
            this.action.tooltip = value;
            return this;
        }

        public FileAction get() {
            return this.action;
        }
    }
}
