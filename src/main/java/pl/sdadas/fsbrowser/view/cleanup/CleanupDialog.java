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
import com.alee.laf.spinner.WebSpinner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import pl.sdadas.fsbrowser.exception.FsException;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.utils.IconFactory;
import pl.sdadas.fsbrowser.utils.ViewUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
public class CleanupDialog extends WebDialog {

    private final FsConnection connection;

    private CheckBoxListModel listModel;

    private WebCheckBoxList list;

    private WebPanel panel;

    private SpinnerDateModel dateModel;

    private Set<String> paths = new HashSet<>();

    private Result result = null;

    public CleanupDialog(FsConnection connection, Window owner) {
        super(owner);
        this.connection = connection;
        this.listModel = createCheckboxListModel();
        this.dateModel = createSpinnerDateModel();
        initView();
    }

    private void initView() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Cleanup");
        setResizable(true);
        setModal(true);

        this.list = createDirectoryList();
        WebLabel listLabel = new WebLabel("Select directories to cleanup:");
        WebScrollPane scroll = new WebScrollPane(this.list);
        scroll.setPreferredHeight(100);
        WebLinkLabel addDirButton = new WebLinkLabel(IconFactory.getIcon("folder-add"));
        addDirButton.setLink("Add directory", this::addDirectory);
        addDirButton.setHorizontalAlignment(SwingConstants.LEFT);
        WebLabel optionsLabel = new WebLabel("Delete files older than: ");
        WebSpinner dateSpinner = new WebSpinner();
        dateSpinner.setModel(dateModel);
        WebPanel optionsPanel = ViewUtils.leftRightPanel(optionsLabel, dateSpinner);
        WebPanel buttonsPanel = createButtonsPanel();
        buttonsPanel.setMargin(10, 0, 0, 0);

        VerticalFlowLayout layout = new VerticalFlowLayout();
        this.panel = new WebPanel(layout);
        this.panel.setMargin(10);
        this.panel.add(listLabel, scroll, addDirButton, optionsPanel, buttonsPanel);

        add(this.panel);
        ViewUtils.setupDialogWindow(this);
    }

    private WebPanel createButtonsPanel() {
        return ViewUtils.rightLeftPanel(50, new WebButton("Ok", this::accept), new WebButton("Cancel", this::reject));
    }

    @SuppressWarnings("unchecked")
    private void accept(ActionEvent event) {
        List<?> paths = this.listModel.getCheckedValues();
        this.result = new Result((List<Path>) paths, this.dateModel.getDate());
        setVisible(false);
    }

    private void reject(ActionEvent event) {
        setVisible(false);
    }

    private void addDirectory() {
        Object value = WebOptionPane.showInputDialog(this, "Directory name", "Add directory",
                WebOptionPane.QUESTION_MESSAGE, null, null, "/user/" + this.connection.getUser() + "/tmp");
        if(value != null && StringUtils.isNotBlank(value.toString())) {
            addModelPath(value.toString(), this.listModel, true, false);
        }
    }

    private WebCheckBoxList createDirectoryList() {
        return new WebCheckBoxList(this.listModel);
    }

    private SpinnerDateModel createSpinnerDateModel() {
        SpinnerDateModel result = new SpinnerDateModel();
        result.setEnd(new Date());
        result.setValue(DateUtils.addDays(new Date(), -7));
        result.setCalendarField(Calendar.DAY_OF_MONTH);
        return result;
    }

    private CheckBoxListModel createCheckboxListModel() {
        String user = this.connection.getUser();
        CheckBoxListModel result = new CheckBoxListModel();
        addModelPath(String.format("/user/%s/.staging", user), result, true, true);
        addModelPath(String.format("/user/%s/.sparkStaging", user), result, true, true);
        addModelPath("/tmp", result, false, true);
        return result;
    }

    private void addModelPath(String stringPath, CheckBoxListModel model, boolean checked, boolean ignoreErrors) {
        Path path = new Path(stringPath);
        try {
            if(this.connection.exists(path)) {
                FileStatus status = this.connection.status(path);
                if(status.isFile()) {
                    error(ignoreErrors, stringPath + " is not a directory");
                } else{
                    if(!paths.contains(stringPath)) {
                        model.addCheckBoxElement(path, checked);
                        paths.add(stringPath);
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

    public Result showDialog() {
        setVisible(true);
        return result;
    }

    public static class Result {

        private final List<Path> paths;

        private final Date date;

        public Result(List<Path> paths, Date date) {
            this.paths = paths;
            this.date = date;
        }

        public List<Path> getPaths() {
            return paths;
        }

        public Date getDate() {
            return date;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("paths", paths)
                    .append("date", date)
                    .toString();
        }
    }
}
