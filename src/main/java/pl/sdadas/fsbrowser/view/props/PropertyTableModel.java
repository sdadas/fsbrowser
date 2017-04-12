package pl.sdadas.fsbrowser.view.props;

import javax.swing.table.AbstractTableModel;
import java.util.Map;

/**
 * @author Sławomir Dadas
 */
public class PropertyTableModel extends AbstractTableModel {

    private final String [][] data;

    private final String [] columns = {"Property", "Value"};

    public PropertyTableModel(Map<String, String> properties) {
        this.data = createDataModel(properties);
    }

    private String[][] createDataModel(Map<String, String> properties) {
        String[][] res = new String[properties.size()][2];
        int idx = 0;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            res[idx][0] = entry.getKey();
            res[idx][1] = entry.getValue();
            idx++;
        }
        return res;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }
}
