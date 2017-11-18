package pl.sdadas.fsbrowser.view.common.messages;

import com.alee.extended.image.WebImage;
import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.extended.panel.GroupPanel;
import com.alee.extended.panel.GroupingType;
import com.alee.extended.window.WebPopOver;
import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import org.apache.commons.lang3.StringUtils;
import pl.sdadas.fsbrowser.utils.IconFactory;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * @author Sławomir Dadas
 */
public class MessagePopup extends WebPopOver {

    private WebImage icon;

    private WebLabel title;

    private WebLabel text;

    private WebButton close;

    public MessagePopup(Window owner) {
        super(owner);
        initView();
    }

    private void initView() {
        this.setCloseOnFocusLoss(true);
        this.setMargin(10);
        this.setLayout(new VerticalFlowLayout());
        this.setLocationRelativeTo(getOwner());
        this.setFocusable(true);

        this.icon = new WebImage(MessageLevel.Info.getIcon());
        this.title = new WebLabel("", WebLabel.CENTER);
        this.title.setFontStyle(true, false);
        this.text = new WebLabel("", WebLabel.CENTER);
        this.close = new WebButton(IconFactory.getIcon("close"), (event) -> dispose());
        this.close.setUndecorated(true);

        this.add(new GroupPanel(GroupingType.fillMiddle, 4, icon, title, close).setMargin(0, 0, 10, 0));
        this.add(text);
        this.addKeyListener(keyListener());
    }

    private KeyAdapter keyListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        };
    }

    public void setText(String value) {
        String label = StringUtils.isBlank(value) ? "" : wrapText(value);
        this.text.setText(label);
    }

    private String wrapText(String value) {
        String[] split = value.split("\n");
        int maxLineLength = Arrays.stream(split).map(String::length).max(Integer::compare).orElse(10);
        int width = Math.min(maxLineLength * 5, 300);
        String text = StringUtils.replace(value, "\n", "<br/>");
        return "<html><div style=\"width:" + width + "px;text-align:center\">" + text + "</div></html>";
    }

    public void setLevel(MessageLevel value) {
        this.icon.setIcon(value.getIcon());
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }
}
