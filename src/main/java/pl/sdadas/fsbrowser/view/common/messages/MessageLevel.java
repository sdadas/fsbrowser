package pl.sdadas.fsbrowser.view.common.messages;

import pl.sdadas.fsbrowser.utils.IconFactory;

import javax.swing.*;

/**
 * @author Sławomir Dadas
 */
public enum MessageLevel {

    Info("info"),
    Warning("warning"),
    Error("error");

    private final String icon;

    MessageLevel(String icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        return IconFactory.getIcon(this.icon);
    }
}
