package mekhq.gui.model;

import mekhq.MekHQ;
import mekhq.campaign.parts.PartsGenerationResult;
import mekhq.gui.utilities.MekHqTableCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PartsGenerationResultsTableModel extends AbstractTableModel {
    private static final DecimalFormat FORMATTER = new DecimalFormat();
    static {
        FORMATTER.setMaximumFractionDigits(3);
    }

    private List<PartsGenerationResult> partsData;
    private static final String EMPTY_CELL = "";

    public final static int COL_PART = 0;
    public final static int COL_IN_USE = 1;
    public final static int COL_STORED = 2;
    public final static int COL_IN_TRANSFER  = 3;
    public final static int COL_TONNAGE = 4;
    public final static int COL_COST = 5;
    public final static int COL_PLANNED = 6;
    public final static int COL_CHKBX_INCLUDE = 7;
    private final transient ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.PartsGenerationResultsTableModel",
            MekHQ.getMHQOptions().getLocale());

    public PartsGenerationResultsTableModel() {
        partsData = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return partsData.size();
    }

    @Override
    public int getColumnCount() {
        return COL_CHKBX_INCLUDE + 1;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case COL_PART:
                return resourceMap.getString("part.heading");
            case COL_IN_USE:
                return resourceMap.getString("inUse.heading");
            case COL_STORED:
                return resourceMap.getString("stored.heading");
            case COL_TONNAGE:
                return resourceMap.getString("storedTonnage.heading");
            case COL_IN_TRANSFER:
                return resourceMap.getString("intransit.heading");
            case COL_PLANNED:
                return resourceMap.getString("planned.heading");
            case COL_COST:
                return resourceMap.getString("cost.heading");
            case COL_CHKBX_INCLUDE:
                return resourceMap.getString("includeInOrder.heading");
            default:
                return EMPTY_CELL;
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        PartsGenerationResult pgr = getPartsGenerationResult(row);
        switch (column) {
            case COL_PART:
                return pgr.getDescription();
            case COL_IN_USE:
                return FORMATTER.format(pgr.getUseCount());
            case COL_STORED:
                return (pgr.getStoreCount() > 0) ? FORMATTER.format(pgr.getStoreCount()) : EMPTY_CELL;
            case COL_TONNAGE:
                return (pgr.getStoreTonnage() > 0) ? FORMATTER.format(pgr.getStoreTonnage()) : EMPTY_CELL;
            case COL_IN_TRANSFER:
                return FORMATTER.format(pgr.getTransferCount());
            case COL_COST:
                return pgr.getCost().toAmountAndSymbolString();
            case COL_PLANNED:
                return FORMATTER.format(pgr.getPlannedCount());
            case COL_CHKBX_INCLUDE:
                if(null == pgr) {
                    return false;
                } else {
                    return pgr.isShouldBeAcquired();
                }
            default:
                return EMPTY_CELL;
        }
    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == COL_CHKBX_INCLUDE) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col == COL_CHKBX_INCLUDE && row >= 1;
    }

    public void setData(Collection<PartsGenerationResult> data) {
        this.partsData = new ArrayList<>(data);
        fireTableDataChanged();
    }

    @SuppressWarnings("unchecked")
    public void updateRow(int row, PartsGenerationResult piu) {
        partsData.set(row, piu);
        fireTableRowsUpdated(row, row);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex == COL_CHKBX_INCLUDE) {
            PartsGenerationResult piu = getPartsGenerationResult(rowIndex);
            piu.setShouldBeAcquired((Boolean) aValue);
            updateRow(rowIndex, piu);
        }
    }

    public PartsGenerationResult getPartsGenerationResult(int row) {
        if ((row < 0) || (row >= partsData.size())) {
            return null;
        }
        return (PartsGenerationResult) partsData.get(row);
    }

    public int getAlignment(int column) {
        switch (column) {
            case COL_PART:
                return SwingConstants.LEFT;
            default:
                return SwingConstants.CENTER;
        }
    }

    public int getWidth(int col) {
        if (col == COL_CHKBX_INCLUDE) {// Calculate from button width, respecting style
            JCheckBox chkBox = new JCheckBox(getValueAt(0, col).toString());
            return chkBox.getPreferredSize().width;
        }
        return Integer.MAX_VALUE;
    }

    public PartsGenerationResultsTableModel.Renderer getRenderer() {
        return new PartsGenerationResultsTableModel.Renderer();
    }


    public static class Renderer extends MekHqTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setOpaque(true);
            setHorizontalAlignment(((PartsGenerationResultsTableModel) table.getModel()).getAlignment(column));
            return this;
        }

    }


}
