package pl.sdadas.fsbrowser.common;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Sławomir Dadas
 */
public class PropertyTableModel extends AbstractTableModel {

    private final String [] columns = {"Property", "Value"};

    private final List<Pair<String, String>> data;

    public PropertyTableModel(Map<String, String> properties) {
        this.data = createDataModel(properties);
    }

    private List<Pair<String, String>> createDataModel(Map<String, String> properties) {
        List<Pair<String, String>> res = new ArrayList<>(properties.size());
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            res.add(ImmutablePair.of(entry.getKey(), entry.getValue()));
        }
        return res;
    }

    public void addProperty(String key, String value) {
        this.data.add(ImmutablePair.of(key, value));
        int rowIdx = this.data.size() - 1;
        fireTableRowsInserted(rowIdx, rowIdx);
    }

    public void addProperties(Map<String, String> properties) {
        int startSize = this.data.size();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            this.data.add(ImmutablePair.of(entry.getKey(), entry.getValue()));
        }
        int endSize = this.data.size();
        fireTableRowsInserted(startSize, Math.max(endSize - 1, 0));
    }

    public void setProperties(Map<String, String> properties) {
        this.data.clear();
        addProperties(properties);
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pair<String, String> entry = data.get(rowIndex);
        return columnIndex == 0 ? entry.getKey() : entry.getValue();
    }
}
