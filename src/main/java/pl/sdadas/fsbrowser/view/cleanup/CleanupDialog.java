package pl.sdadas.fsbrowser.view.cleanup;

import com.alee.extended.label.WebLinkLabel;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.list.CheckBoxListModel;
import com.alee.extended.list.WebCheckBoxList;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.optionpane.WebOptionPane;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.scroll.WebScrollPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.utils.ViewUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Sławomir Dadas
 */
public class CleanupDialog extends WebDialog {

    private final FsConnection connection;

    private CheckBoxListModel model;

    private WebCheckBoxList list;

    private WebPanel panel;

    private Set<String> directories = new HashSet<>();

    public CleanupDialog(FsConnection connection, Window owner) {
        super(owner);
        this.connection = connection;
        this.model = createCheckboxListModel();
        initView();
    }

    private void initView() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(300, 300));
        setTitle("Cleanup");
        setResizable(true);
        setModal(true);

        this.list = createDirectoryList();
        WebLabel header = new WebLabel("Select directories to cleanup:");
        WebScrollPane scroll = new WebScrollPane(this.list);
        scroll.setPreferredHeight(100);
        WebLinkLabel addDirButton = new WebLinkLabel(IconFactory.getIcon("folder-add"));
        addDirButton.setLink("Add directory", this::addDirectory);
        addDirButton.setHorizontalAlignment(SwingConstants.LEFT);

        VerticalFlowLayout layout = new VerticalFlowLayout();
        this.panel = new WebPanel(layout);
        this.panel.setMargin(10);
        this.panel.add(header, scroll, addDirButton);

        add(this.panel);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addDirectory() {
        Object value = WebOptionPane.showInputDialog(this, "Directory name", "Add directory",
                WebOptionPane.QUESTION_MESSAGE, null, null, "/user/" + this.connection.getUser() + "/tmp");
        if(value != null && StringUtils.isNotBlank(value.toString())) {
            addModelPath(value.toString(), this.model, false);
        }
    }

    private WebCheckBoxList createDirectoryList() {
        return new WebCheckBoxList(this.model);
    }

    private CheckBoxListModel createCheckboxListModel() {
        String user = this.connection.getUser();
        CheckBoxListModel result = new CheckBoxListModel();
        addModelPath(String.format("/user/%s/.staging", user), result, true);
        addModelPath(String.format("/user/%s/.sparkStaging", user), result, true);
        addModelPath("/tmp", result, true);
        return result;
    }

    private void addModelPath(String stringPath, CheckBoxListModel model, boolean ignoreErrors) {
        Path path = new Path(stringPath);
        try {
            if(this.connection.exists(path)) {
                FileStatus status = this.connection.status(path);
                if(status.isFile()) {
                    error(ignoreErrors, stringPath + " is not a directory");
                } else{
                    if(!directories.contains(stringPath)) {
                        model.addCheckBoxElement(path, true);
                        directories.add(stringPath);
                    }
                }
            } else {
                error(ignoreErrors, "Directory does not exist");
            }
        } catch (FsException ex) {
            error(ignoreErrors, ex.getMessage());
        }
    }

    private void error(boolean ignore, String message) {
        if(!ignore) {
            ViewUtils.error(this.panel, message);
        }
    }

    public void showDialog() {
        setVisible(true);
    }
}
