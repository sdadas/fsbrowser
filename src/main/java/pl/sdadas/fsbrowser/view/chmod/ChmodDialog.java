package pl.sdadas.fsbrowser.view.chmod;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.panel.WebButtonGroup;
import com.alee.laf.button.WebButton;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.checkbox.WebCheckBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import pl.sdadas.fsbrowser.fs.connection.FsConnection;
import pl.sdadas.fsbrowser.utils.ViewUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.apache.hadoop.fs.permission.FsAction.*;

/**
 * @author Sławomir Dadas
 */
public class ChmodDialog extends WebDialog {

    private WebPanel panel;

    private Set<PermissionKey> permissions = new HashSet<>();

    private boolean recursive = true;

    private boolean stickyBit;

    private String chmod;

    public ChmodDialog(FsConnection connection, Path path, Window owner) {
        super(owner);
        readPermissions(connection, path);
        initView();
    }

    private void readPermissions(FsConnection connection, Path path) {
        ViewUtils.handleErrors(this, () -> {
            FileStatus status = connection.status(path);
            FsPermission permission = status.getPermission();
            this.stickyBit = permission.getStickyBit();
            this.permissions.clear();
            readPermissionsSubject(permission.getUserAction(), PermissionSubject.User);
            readPermissionsSubject(permission.getGroupAction(), PermissionSubject.Group);
            readPermissionsSubject(permission.getOtherAction(), PermissionSubject.Other);
        });
    }

    private void readPermissionsSubject(FsAction actions, PermissionSubject subject) {
        EnumSet<FsAction> read = EnumSet.of(READ, READ_WRITE, READ_EXECUTE, ALL);
        if(read.contains(actions)) permissions.add(new PermissionKey(subject, PermissionType.Read));

        EnumSet<FsAction> write = EnumSet.of(WRITE, WRITE_EXECUTE, READ_WRITE, ALL);
        if(write.contains(actions)) permissions.add(new PermissionKey(subject, PermissionType.Write));

        EnumSet<FsAction> exec = EnumSet.of(EXECUTE, WRITE_EXECUTE, READ_EXECUTE, ALL);
        if(exec.contains(actions)) permissions.add(new PermissionKey(subject, PermissionType.Execute));
    }

    private String getChmodValue() {
        char[] result = new char[4];
        result[0] = stickyBit ? '1' : '0';
        result[1] = getChmodSubject(PermissionSubject.User);
        result[2] = getChmodSubject(PermissionSubject.Group);
        result[3] = getChmodSubject(PermissionSubject.Other);
        return new String(result);
    }

    private char getChmodSubject(PermissionSubject subject) {
        int res = permissions.stream()
                .filter(key -> key.subject.equals(subject))
                .mapToInt(key -> key.getType().value).sum();
        return String.valueOf(res).charAt(0);
    }

    private void initView() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Change permissions");
        setResizable(true);
        setModal(true);

        WebPanel userPanel = permissionsPanel(PermissionSubject.User);
        WebPanel groupPanel = permissionsPanel(PermissionSubject.Group);
        WebPanel otherPanel = permissionsPanel(PermissionSubject.Other);
        WebCheckBox recursiveCheckbox = new WebCheckBox("Recursive");
        recursiveCheckbox.setSelected(this.recursive);
        recursiveCheckbox.addItemListener(event -> this.recursive = event.getStateChange() == ItemEvent.SELECTED);
        WebCheckBox stickyBitCheckbox = new WebCheckBox("Sticky bit");
        stickyBitCheckbox.setSelected(this.stickyBit);
        stickyBitCheckbox.addItemListener(event -> this.stickyBit = event.getStateChange() == ItemEvent.SELECTED);
        stickyBitCheckbox.setMargin(0, 10, 0, 0);
        WebPanel optionsPanel = ViewUtils.leftRightPanel(recursiveCheckbox, stickyBitCheckbox);
        optionsPanel.setMargin(10, 0, 20, 0);
        WebPanel buttonsPanel = createButtonsPanel();

        VerticalFlowLayout layout = new VerticalFlowLayout();
        this.panel = new WebPanel(layout);
        this.panel.setMargin(10);
        this.panel.add(userPanel, groupPanel, otherPanel, optionsPanel, buttonsPanel);

        add(this.panel);
        ViewUtils.setupDialogWindow(this);
    }

    private WebPanel createButtonsPanel() {
        return ViewUtils.rightLeftPanel(50, new WebButton("Cancel", this::reject), new WebButton("Ok", this::accept));
    }

    private void accept(ActionEvent event) {
        this.chmod = getChmodValue();
        setVisible(false);
    }

    private void reject(ActionEvent event) {
        this.chmod = null;
        setVisible(false);
    }

    public String showDialog() {
        setVisible(true);
        return chmod;
    }

    public boolean isRecursive() {
        return recursive;
    }

    private WebPanel permissionsPanel(PermissionSubject subject) {
        WebLabel label = new WebLabel(subject.name());
        label.setPreferredWidth(70);
        WebButtonGroup group = new WebButtonGroup(false);
        group.add(createButton(subject, PermissionType.Read));
        group.add(createButton(subject, PermissionType.Write));
        group.add(createButton(subject, PermissionType.Execute));
        return ViewUtils.leftRightPanel(label, group);
    }

    private WebToggleButton createButton(PermissionSubject subject, PermissionType type) {
        WebToggleButton result = new WebToggleButton(type.code);
        result.setDrawFocus(false);
        result.addItemListener(event -> {
            if(event.getStateChange() == ItemEvent.SELECTED) {
                permissions.add(new PermissionKey(subject, type));
            } else if(event.getStateChange() == ItemEvent.DESELECTED) {
                permissions.remove(new PermissionKey(subject, type));
            }
        });
        result.setSelected(permissions.contains(new PermissionKey(subject, type)));
        result.setPreferredWidth(30);
        return result;
    }

    private class PermissionKey {

        private final PermissionSubject subject;

        private final PermissionType type;

        public PermissionKey(PermissionSubject subject, PermissionType type) {
            this.subject = subject;
            this.type = type;
        }

        public PermissionSubject getSubject() {
            return subject;
        }

        public PermissionType getType() {
            return type;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null) return false;

            if (other instanceof PermissionKey) {
                PermissionKey that = (PermissionKey) other;
                return new EqualsBuilder()
                        .append(subject, that.subject)
                        .append(type, that.type)
                        .isEquals();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(subject, type);
        }
    }

    private enum PermissionSubject {
        User, Group, Other;
    }

    private enum PermissionType {
        Read("R", 4), Write("W", 2), Execute("X", 1);

        private final String code;

        private final int value;

        PermissionType(String code, int value) {
            this.code = code;
            this.value = value;
        }
    }

}
