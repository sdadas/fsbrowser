package pl.sdadas.fsbrowser.view.filesystempanel;

import pl.sdadas.fsbrowser.view.filebrowser.FileItem;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Sławomir Dadas
 */
public interface ActionPredicate extends Predicate<List<FileItem>> {

}
