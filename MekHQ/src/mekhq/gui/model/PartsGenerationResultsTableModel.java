package mekhq.gui.model;

import mekhq.MekHQ;
import mekhq.campaign.parts.PartsGenerationResult;
import mekhq.campaign.parts.PartsGenerationResult;
import mekhq.gui.utilities.MekHqTableCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;

public class PartsGenerationResultsTableModel extends DataTableModel {
    private static final DecimalFormat FORMATTER = new DecimalFormat();
    static {
        FORMATTER.setMaximumFractionDigits(3);
    }
    private static final String EMPTY_CELL = "";

    public final static int COL_PART = 0;
    public final static int COL_IN_USE = 1;
    public final static int COL_STORED = 2;
    public final static int COL_IN_TRANSFER  = 3;
    public final static int COL_TONNAGE = 4;
    public final static int COL_COST = 5;
    public final static int COL_CHKBX_ORDER = 6;
    public final static int COL_CHKBX_GMADD = 7;

    private final transient ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.PartsGenerationResultTableModel",
            MekHQ.getMHQOptions().getLocale());

    public PartsGenerationResultsTableModel() {
        data = new ArrayList<PartsGenerationResult>();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COL_CHKBX_GMADD + 1;
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
                return resourceMap.getString("ordered.heading");
            case COL_COST:
                return resourceMap.getString("cost.heading");
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
                if ( pgr.getInTransitCount() > 0 && pgr.getPlannedCount() <= 0 ) {
                    return FORMATTER.format(pgr.getInTransitCount());
                } else if ( pgr.getPlannedCount() > 0 ) {
                    return String.format("%s [+%s]",
                        FORMATTER.format(pgr.getInTransitCount()), FORMATTER.format(pgr.getPlannedCount()));
                } else {
                    return EMPTY_CELL;
                }
            case COL_COST:
                return pgr.getCost().toAmountAndSymbolString();
            default:
                return EMPTY_CELL;
        }
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case COL_CHKBX_ORDER:
            case COL_CHKBX_GMADD:
                return true;
            default:
                return false;
        }
    }

    public void setData(Set<PartsGenerationResult> data) {
        setData(new ArrayList<>(data));
    }

    @SuppressWarnings("unchecked")
    public void updateRow(int row, PartsGenerationResult piu) {
        ((ArrayList<PartsGenerationResult>) data).set(row, piu);
        fireTableRowsUpdated(row, row);
    }

    public PartsGenerationResult getPartsGenerationResult(int row) {
        if ((row < 0) || (row >= data.size())) {
            return null;
        }
        return (PartsGenerationResult) data.get(row);
    }

    public boolean isBuyable(int row) {
        return (row >= 0) && (row < data.size())
            && (null != ((PartsGenerationResult) data.get(row)).getPartToBuy());
    }

    public int getAlignment(int column) {
        switch (column) {
            case COL_PART:
                return SwingConstants.LEFT;
            case COL_IN_USE:
            case COL_STORED:
            case COL_TONNAGE:
            case COL_IN_TRANSFER:
            case COL_COST:
                return SwingConstants.RIGHT;
            default:
                return SwingConstants.CENTER;
        }
    }

    public int getPreferredWidth(int column) {
        switch (column) {
            case COL_PART:
                return 300;
            case COL_IN_USE:
            case COL_STORED:
            case COL_TONNAGE:
            case COL_IN_TRANSFER:
            case COL_COST:
                return 20;
            case COL_CHKBX_ORDER:
            case COL_CHKBX_GMADD:
                return 10;
            default:
                return 100;
        }
    }

    public boolean hasConstantWidth(int col) {
        switch (col) {
            case COL_CHKBX_ORDER:
            case COL_CHKBX_GMADD:
                return true;
            default:
                return false;
        }
    }

    public int getWidth(int col) {
        switch (col) {
            case COL_CHKBX_ORDER:
            case COL_CHKBX_GMADD:
                // Calculate from button width, respecting style
                JCheckBox chkBox = new JCheckBox(getValueAt(0, col).toString());
                return chkBox.getPreferredSize().width;
            default:
                return Integer.MAX_VALUE;
        }
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

    public static class ButtonColumn extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor, ActionListener, MouseListener {

        private JTable table;
        private Action action;
        private Border originalBorder;
        private Border focusBorder;

        private JButton renderButton;
        private JButton editButton;
        private Object editorValue;
        private boolean isButtonColumnEditor;
        private boolean enabled;

        public Border getFocusBorder()
        {
            return focusBorder;
        }

        public void setFocusBorder(Border focusBorder)
        {
            this.focusBorder = focusBorder;
            editButton.setBorder(focusBorder);
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
            editButton.setEnabled(enabled);
            renderButton.setEnabled(enabled);
        }

        @Override
        public Object getCellEditorValue() {
            return editorValue;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (table.isEditing() && (this == table.getCellEditor())) {
                isButtonColumnEditor = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isButtonColumnEditor && table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            isButtonColumnEditor = false;
        }

        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.convertRowIndexToModel(table.getEditingRow());
            fireEditingStopped();

            //  Invoke the Action
            ActionEvent event = new ActionEvent(table, ActionEvent.ACTION_PERFORMED, "" + row);
            action.actionPerformed(event);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            boolean buyable = ((PartsGenerationResultsTableModel) table.getModel()).isBuyable(table.getRowSorter().convertRowIndexToModel(row));

            if (value == null) {
                editButton.setText(EMPTY_CELL);
                editButton.setIcon(null);
            } else if (value instanceof Icon) {
                editButton.setText(EMPTY_CELL);
                editButton.setIcon((Icon) value);
            } else {
                editButton.setText(value.toString());
                editButton.setIcon(null);
            }
            editButton.setEnabled(enabled && buyable);

            this.editorValue = value;
            return editButton;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean buyable = ((PartsGenerationResultsTableModel) table.getModel()).isBuyable(table.getRowSorter().convertRowIndexToModel(row));

            if (isSelected && enabled && buyable) {
                renderButton.setForeground(table.getSelectionForeground());
                 renderButton.setBackground(table.getSelectionBackground());
            } else {
                renderButton.setForeground(table.getForeground());
                renderButton.setBackground(UIManager.getColor("Button.background"));
            }

            if (hasFocus && enabled && buyable) {
                renderButton.setBorder(focusBorder);
            } else {
                renderButton.setBorder(originalBorder);
            }

            if (value == null)
            {
                renderButton.setText(EMPTY_CELL);
                renderButton.setIcon(null);
            } else if (value instanceof Icon) {
                renderButton.setText(EMPTY_CELL);
                renderButton.setIcon((Icon) value);
            } else {
                renderButton.setText(value.toString());
                renderButton.setIcon(null);
            }
            renderButton.setEnabled(enabled && buyable);

            return renderButton;
        }
    }
}
