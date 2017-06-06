package pl.sdadas.fsbrowser.view.chown;

import com.alee.extended.layout.FormLayout;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.button.WebButton;
import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.text.WebTextField;
import org.apache.commons.lang3.StringUtils;
import pl.sdadas.fsbrowser.utils.ViewUtils;
import pl.sdadas.fsbrowser.view.filebrowser.FileItem;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

/**
 * @author Sławomir Dadas
 */
public class ChownDialog extends WebDialog {

    private String owner;

    private String group;

    private boolean recursive;

    private WebTextField ownerField;

    private WebTextField groupField;

    private WebPanel panel;

    private Result result;

    public ChownDialog(FileItem file, Window owner) {
        super(owner);
        this.owner = file.getOwner();
        this.group = file.getGroup();
        initView();
    }

    private void initView() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Change owner/group");
        setMinimumSize(new Dimension(300, 150));
        setResizable(true);
        setModal(true);

        FormLayout formLayout = new FormLayout(5, 5);
        WebPanel formPanel = new WebPanel(formLayout);
        this.ownerField = new WebTextField(this.owner);
        this.groupField = new WebTextField(this.group);
        formPanel.setMargin(0, 0, 10, 0);
        formPanel.add(new WebLabel("Owner"));
        formPanel.add(ownerField);
        formPanel.add(new WebLabel("Group"));
        formPanel.add(groupField);

        WebCheckBox recursiveCheckbox = new WebCheckBox("Recursive");
        recursiveCheckbox.setSelected(this.recursive);
        recursiveCheckbox.addItemListener(event -> this.recursive = event.getStateChange() == ItemEvent.SELECTED);

        VerticalFlowLayout layout = new VerticalFlowLayout();
        this.panel = new WebPanel(layout);
        this.panel.setMargin(10);
        this.panel.add(formPanel);
        this.panel.add(recursiveCheckbox);
        this.panel.add(createButtonsPanel());

        add(this.panel);
        ViewUtils.setupDialogWindow(this);
    }

    private WebPanel createButtonsPanel() {
        return ViewUtils.rightLeftPanel(50, new WebButton("Cancel", this::reject), new WebButton("Ok", this::accept));
    }

    private void accept(ActionEvent event) {
        this.result = new Result();
        this.result.owner = getChangedValue(ownerField, owner);
        this.result.group = getChangedValue(groupField, group);
        this.result.recursive = recursive;
        setVisible(false);
    }

    private String getChangedValue(WebTextField field, String oldValue) {
        String value = StringUtils.strip(field.getText());
        if(StringUtils.isBlank(value)) return null;
        if(StringUtils.equals(value, oldValue)) return null;
        return value;
    }

    private void reject(ActionEvent event) {
        this.result = null;
        setVisible(false);
    }

    public Result showDialog() {
        setVisible(true);
        return result;
    }

    public static class Result {

        private String owner;

        private String group;

        private boolean recursive;

        public String getOwner() {
            return owner;
        }

        public String getGroup() {
            return group;
        }

        public boolean isRecursive() {
            return recursive;
        }
    }

}
