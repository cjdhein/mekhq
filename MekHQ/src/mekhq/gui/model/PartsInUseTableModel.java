package mekhq.gui.model;

import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.Part;
import mekhq.campaign.parts.PartInUse;
import mekhq.campaign.parts.PartInventory;
import mekhq.gui.utilities.MekHqTableCellRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
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

public class PartsInUseTableModel extends PartsTableModel {
    private Campaign campaign;
    private static final DecimalFormat FORMATTER = new DecimalFormat();
    static {
        FORMATTER.setMaximumFractionDigits(3);
    }
    private static final String EMPTY_CELL = "";

    public final static int COL_PART = 0;
    public final static int COL_TONNAGE = 1;
    public final static int COL_IN_USE = 2;
    public final static int COL_SUPPLY = 3;
    public final static int COL_TRANSIT = 4;
    public final static int COL_ORDERED = 5;
    public final static int COL_COST = 6;
    public final static int COL_BUTTON_BUY = 7;
    public final static int COL_BUTTON_BUY_BULK = 8;
    public final static int COL_BUTTON_GMADD = 9;
    public final static int COL_BUTTON_GMADD_BULK = 10;

    private final transient ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.PartsInUseTableModel",
        MekHQ.getMHQOptions().getLocale());

    public PartsInUseTableModel(Campaign c) {
        this.campaign = c;
        data = new ArrayList<PartInUse>();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COL_BUTTON_GMADD_BULK + 1;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case COL_PART:
                return resourceMap.getString("part.heading");
            case COL_IN_USE:
                return resourceMap.getString("inUse.heading");
            case COL_SUPPLY:
                return resourceMap.getString("stored.heading");
            case COL_TONNAGE:
                return resourceMap.getString("storedTonnage.heading");
            case COL_TRANSIT:
                return resourceMap.getString("transit.heading");
            case COL_COST:
                return resourceMap.getString("cost.heading");
            case COL_ORDERED:
                return resourceMap.getString("ordered.heading");
            default:
                return EMPTY_CELL;
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        PartInUse piu = getPartInUse(row);
        Part part;
        PartInventory inventory;
        if (piu == null) {
            return EMPTY_CELL;
        } else {
            part = piu.getPartToBuy().getAcquisitionPart();
            inventory = campaign.getPartInventory(part);
            switch (column) {
                case COL_PART:
                    return piu.getDescription();
                case COL_IN_USE:
                    return FORMATTER.format(piu.getUseCount());
                case COL_SUPPLY:
                    return (inventory.getSupply() > 0) ? FORMATTER.format(inventory.getSupply()) : EMPTY_CELL;
                case COL_TONNAGE:
                    return (inventory.getSupply() * part.getTonnage() > 0) ? FORMATTER.format(inventory.getSupply() * part.getTonnage()) : EMPTY_CELL;
                case COL_TRANSIT:
                    return (inventory.getTransit() > 0) ? FORMATTER.format(inventory.getTransit()) : EMPTY_CELL;
                case COL_ORDERED:
                    return (piu.getPlannedCount() > 0) ? FORMATTER.format(piu.getPlannedCount()) : EMPTY_CELL;
                case COL_COST:
                    return piu.getCost().toAmountAndSymbolString();
                case COL_BUTTON_BUY:
                    return resourceMap.getString("buy.text");
                case COL_BUTTON_BUY_BULK:
                    return resourceMap.getString("buyInBulk.text");
                case COL_BUTTON_GMADD:
                    return resourceMap.getString("add.text");
                case COL_BUTTON_GMADD_BULK:
                    return resourceMap.getString("addInBulk.text");
                default:
                    return EMPTY_CELL;
            }
        }
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case COL_BUTTON_BUY:
            case COL_BUTTON_BUY_BULK:
            case COL_BUTTON_GMADD:
            case COL_BUTTON_GMADD_BULK:
                return true;
            default:
                return false;
        }
    }

    public void setData(Set<PartInUse> data) {
        setData(new ArrayList<>(data));
    }

    @SuppressWarnings("unchecked")
    public void updateRow(int row, PartInUse piu) {
        ((ArrayList<PartInUse>) data).set(row, piu);
        fireTableRowsUpdated(row, row);
    }

    public PartInUse getPartInUse(int row) {
        if ((row < 0) || (row >= data.size())) {
            return null;
        }
        return (PartInUse) data.get(row);
    }

    @Override
    public Part getPartAt(int row) {
        return ((PartInUse) data.get(row)).getPartToBuy().getAcquisitionPart();
    }

    public boolean isBuyable(int row) {
        return (row >= 0) && (row < data.size())
            && (null != ((PartInUse) data.get(row)).getPartToBuy());
    }

    public int getAlignment(int column) {
        switch (column) {
            case COL_PART:
                return SwingConstants.LEFT;
            case COL_TONNAGE:
            case COL_COST:
                return SwingConstants.RIGHT;
            default:
                return SwingConstants.CENTER;
        }
    }

    public int getPreferredWidth(int column) {
        switch (column) {
            case COL_PART:
                return 120;
            case COL_IN_USE:
            case COL_SUPPLY:
            case COL_TONNAGE:
            case COL_TRANSIT:
            case COL_ORDERED:
                return 20;
            case COL_COST:
                return 50;
            case COL_BUTTON_BUY:
            case COL_BUTTON_GMADD:
                return 80;
            default:
                return 100;
        }
    }

    public boolean hasConstantWidth(int col) {
        switch (col) {
            case COL_BUTTON_BUY:
            case COL_BUTTON_BUY_BULK:
            case COL_BUTTON_GMADD:
            case COL_BUTTON_GMADD_BULK:
                return true;
            default:
                return false;
        }
    }

    public int getWidth(int col) {
        switch (col) {
            case COL_BUTTON_BUY:
            case COL_BUTTON_BUY_BULK:
            case COL_BUTTON_GMADD:
            case COL_BUTTON_GMADD_BULK:
                // Calculate from button width, respecting style
                JButton btn = new JButton(getValueAt(0, col).toString());
                return btn.getPreferredSize().width;
            default:
                return Integer.MAX_VALUE;
        }
    }

    public DefaultTableCellRenderer getRenderer() {
        return new PartsInUseTableModel.Renderer();
    }

    public static class Renderer extends MekHqTableCellRenderer {

        public static void setupTigerStripes(Component c, JTable table, int row) {
            Color background = table.getBackground();
            if (row % 2 != 0) {
                Color alternateColor = UIManager.getColor("Table.alternateRowColor");
                if (alternateColor == null) {
                    // If we don't have an alternate row color, use 'controlHighlight'
                    // as it is pretty reasonable across the various themes.
                    alternateColor = UIManager.getColor("controlHighlight");
                }
                if (alternateColor != null) {
                    background = alternateColor;
                }
            }
            c.setForeground(table.getForeground());
            c.setBackground(background);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setOpaque(true);
            setHorizontalAlignment(((PartsInUseTableModel) table.getModel()).getAlignment(column));
            setupTigerStripes(this, table, row);
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

        public ButtonColumn(JTable table, Action action, int column) {
            this.table = table;
            this.action = action;

            renderButton = new JButton();
            editButton = new JButton();
            editButton.setFocusPainted(false);
            editButton.addActionListener(this);
            originalBorder = editButton.getBorder();
            enabled = true;

            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(column).setCellRenderer(this);
            columnModel.getColumn(column).setCellEditor(this);
            table.addMouseListener(this);
        }

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
            boolean buyable = ((PartsInUseTableModel) table.getModel()).isBuyable(table.getRowSorter().convertRowIndexToModel(row));

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
            boolean buyable = ((PartsInUseTableModel) table.getModel()).isBuyable(table.getRowSorter().convertRowIndexToModel(row));

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
