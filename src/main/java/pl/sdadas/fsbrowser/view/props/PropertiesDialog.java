package pl.sdadas.fsbrowser.view.props;

import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * @author Sławomir Dadas
 */
public class PropertiesDialog extends WebDialog {

    private final PropertyTableModel model;

    public PropertiesDialog(Map<String, String> props, Window owner) {
        super(owner);
        this.model = new PropertyTableModel(props);
        initView();
        setLocationRelativeTo(owner);
    }

    private void initView() {
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(600, 600));
        setTitle("Properties");

        WebTable table = new WebTable(model);
        WebScrollPane scroll = new WebScrollPane(table);
        add(scroll);
    }
}
