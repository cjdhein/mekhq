/*
 * Copyright (c) 2020 The MegaMek Team. All rights reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui.dialog;

import mekhq.campaign.Campaign;
import mekhq.campaign.parts.*;
import mekhq.campaign.work.IAcquisitionWork;
import mekhq.gui.CampaignGUI;
import mekhq.gui.model.PartsInUseTableModel;
import mekhq.gui.sorter.FormattedNumberSorter;
import mekhq.gui.sorter.PartsDetailSorter;
import mekhq.gui.sorter.TwoNumbersSorter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

/**
 * A dialog to show parts in use, ordered, in transit with actionable buttons for buying or adding more
 * taken from the Overview tab originally but now a dialog.
 */
public class PartsReportDialog extends AbstractPartsDialog {

    private JPanel overviewPartsPanel;
    private JTable overviewPartsInUseTable;
    private PartsInUseTableModel overviewPartsModel;

    private Campaign campaign;
    private CampaignGUI gui;

    public PartsReportDialog(CampaignGUI gui, boolean modal) {
        super(gui.getFrame(), modal, gui);
        this.gui = gui;
        this.campaign = gui.getCampaign();
        initComponents();
        refreshOverviewPartsInUse();
        pack();
        setLocationRelativeTo(gui.getFrame());
    }

    protected void initComponents() {
        overviewPartsPanel = new JPanel(new BorderLayout());

        GridBagConstraints c = new GridBagConstraints();
        JPanel panFilter = new JPanel();
        JLabel lblPartsChoice = new JLabel(resourceMap.getString("lblPartsChoice.text"));
        DefaultComboBoxModel<String> partsGroupModel = new DefaultComboBoxModel<>();
        for (int i = 0; i < SG_NUM; i++) {
            partsGroupModel.addElement(getPartsGroupName(i));
        }
        choiceParts = new JComboBox<>(partsGroupModel);
        choiceParts.setName("choiceParts");
        choiceParts.setSelectedIndex(0);
        choiceParts.addActionListener(evt -> filterParts());
        partsSorter = new TableRowSorter<>(partsModel);
        partsSorter.setComparator(PartsInUseTableModel.COL_NAME, new PartsDetailSorter());
        panFilter.setLayout(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5,5,5,5);
        panFilter.add(lblPartsChoice, c);
        c.gridx = 1;
        c.weightx = 1.0;
        panFilter.add(choiceParts, c);

        JLabel lblFilter = new JLabel(resourceMap.getString("lblFilter.text"));
        lblFilter.setName("lblFilter");
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        panFilter.add(lblFilter, c);
        txtFilter = new JTextField();
        txtFilter.setText("");
        txtFilter.setMinimumSize(new Dimension(200, 28));
        txtFilter.setName("txtFilter");
        txtFilter.setPreferredSize(new Dimension(200, 28));
        txtFilter.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                filterParts();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterParts();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterParts();
            }
        });
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0;
        panFilter.add(txtFilter, c);
        overviewPartsPanel.add(panFilter, BorderLayout.NORTH);

        overviewPartsModel = new PartsInUseTableModel(campaign);
        overviewPartsInUseTable = new JTable(overviewPartsModel);
        overviewPartsInUseTable.setRowSelectionAllowed(false);
        overviewPartsInUseTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumn column;
        for (int i = 0; i < overviewPartsModel.getColumnCount(); ++i) {
            column = overviewPartsInUseTable.getColumnModel().getColumn(i);
            column.setCellRenderer(overviewPartsModel.getRenderer());
            if (overviewPartsModel.hasConstantWidth(i)) {
                column.setMinWidth(overviewPartsModel.getWidth(i));
                column.setMaxWidth(overviewPartsModel.getWidth(i));
            } else {
                column.setPreferredWidth(overviewPartsModel.getPreferredWidth(i));
            }
        }
        overviewPartsInUseTable.setIntercellSpacing(new Dimension(0, 0));
        overviewPartsInUseTable.setShowGrid(false);
        partsSorter = new TableRowSorter<>(overviewPartsModel);
        partsSorter.setSortsOnUpdates(true);
        // Don't sort the buttons
        partsSorter.setSortable(PartsInUseTableModel.COL_BUTTON_BUY, false);
        partsSorter.setSortable(PartsInUseTableModel.COL_BUTTON_BUY_BULK, false);
        partsSorter.setSortable(PartsInUseTableModel.COL_BUTTON_GMADD, false);
        partsSorter.setSortable(PartsInUseTableModel.COL_BUTTON_GMADD_BULK, false);
        // Numeric columns
        partsSorter.setComparator(PartsInUseTableModel.COL_IN_USE, new FormattedNumberSorter());
        partsSorter.setComparator(PartsInUseTableModel.COL_SUPPLY, new FormattedNumberSorter());
        partsSorter.setComparator(PartsInUseTableModel.COL_TONNAGE, new FormattedNumberSorter());
        partsSorter.setComparator(PartsInUseTableModel.COL_TRANSIT, new TwoNumbersSorter());
        partsSorter.setComparator(PartsInUseTableModel.COL_COST, new FormattedNumberSorter());
        // Default starting sort
        partsSorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));;
        overviewPartsInUseTable.setRowSorter(partsSorter);

        // Add buttons and actions. TODO: Only refresh the row we are working
        // on, not the whole table
        @SuppressWarnings("serial")
        Action buy = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                PartInUse piu = overviewPartsModel.getPartInUse(row);
                IAcquisitionWork partToBuy = piu.getPartToBuy();
                campaign.getShoppingList().addShoppingItem(partToBuy, 1, campaign);
                refreshOverviewSpecificPart(row, piu, partToBuy);
            }
        };
        @SuppressWarnings("serial")
        Action buyInBulk = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                PartInUse piu = overviewPartsModel.getPartInUse(row);
                int quantity = 1;
                PopupValueChoiceDialog pcd = new PopupValueChoiceDialog(gui.getFrame(), true,
                        "How Many " + piu.getPartToBuy().getAcquisitionName(), quantity, 1, CampaignGUI.MAX_QUANTITY_SPINNER);
                pcd.setVisible(true);
                quantity = pcd.getValue();
                if (quantity <= 0) {
                    return;
                }
                IAcquisitionWork partToBuy = piu.getPartToBuy();
                campaign.getShoppingList().addShoppingItem(partToBuy, quantity, campaign);
                refreshOverviewSpecificPart(row, piu, partToBuy);
            }
        };
        @SuppressWarnings("serial")
        Action add = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                PartInUse piu = overviewPartsModel.getPartInUse(row);
                IAcquisitionWork partToBuy = piu.getPartToBuy();
                campaign.getQuartermaster().addPart((Part) partToBuy.getNewEquipment(), 0);
                refreshOverviewSpecificPart(row, piu, partToBuy);
            }
        };
        @SuppressWarnings("serial")
        Action addInBulk = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                PartInUse piu = overviewPartsModel.getPartInUse(row);
                int quantity = 1;
                PopupValueChoiceDialog pcd = new PopupValueChoiceDialog(gui.getFrame(), true,
                        "How Many " + piu.getPartToBuy().getAcquisitionName(), quantity, 1, CampaignGUI.MAX_QUANTITY_SPINNER);
                pcd.setVisible(true);
                quantity = pcd.getValue();
                IAcquisitionWork partToBuy = piu.getPartToBuy();
                while (quantity > 0) {
                    campaign.getQuartermaster().addPart((Part) partToBuy.getNewEquipment(), 0);
                    --quantity;
                }
                refreshOverviewSpecificPart(row, piu, partToBuy);
            }
        };

        new PartsInUseTableModel.ButtonColumn(overviewPartsInUseTable, buy, PartsInUseTableModel.COL_BUTTON_BUY);
        new PartsInUseTableModel.ButtonColumn(overviewPartsInUseTable, buyInBulk,
                PartsInUseTableModel.COL_BUTTON_BUY_BULK);
        new PartsInUseTableModel.ButtonColumn(overviewPartsInUseTable, add, PartsInUseTableModel.COL_BUTTON_GMADD);
        new PartsInUseTableModel.ButtonColumn(overviewPartsInUseTable, addInBulk,
                PartsInUseTableModel.COL_BUTTON_GMADD_BULK);

        overviewPartsPanel.add(new JScrollPane(overviewPartsInUseTable), BorderLayout.CENTER);

        JPanel panButtons = new JPanel(new GridBagLayout());
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(evt -> setVisible(false));
        panButtons.add(btnClose, new GridBagConstraints());
        overviewPartsPanel.add(panButtons, BorderLayout.PAGE_END);

        this.setLayout(new BorderLayout());
        this.add(overviewPartsPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000, 800));

    }

    private void refreshOverviewSpecificPart(int row, PartInUse piu, IAcquisitionWork newPart) {
        if (piu.equals(new PartInUse((Part) newPart))) {
            // Simple update
            campaign.updatePartInUse(piu);
            overviewPartsModel.fireTableRowsUpdated(row, row);
        } else {
            // Some other part changed; fire a full refresh to be sure
            refreshOverviewPartsInUse();
        }
    }

    private void refreshOverviewPartsInUse() {
        overviewPartsModel.setData(campaign.getPartsInUse());
        TableColumnModel tcm = overviewPartsInUseTable.getColumnModel();
        PartsInUseTableModel.ButtonColumn column = (PartsInUseTableModel.ButtonColumn) tcm.getColumn(PartsInUseTableModel.COL_BUTTON_GMADD)
                .getCellRenderer();
        column.setEnabled(campaign.isGM());
        column = (PartsInUseTableModel.ButtonColumn) tcm.getColumn(PartsInUseTableModel.COL_BUTTON_GMADD_BULK).getCellRenderer();
        column.setEnabled(campaign.isGM());
    }
}
