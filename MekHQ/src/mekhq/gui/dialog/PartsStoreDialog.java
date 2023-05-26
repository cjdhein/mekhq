/*
 * PartsStoreDialog.java
 *
 * Copyright (c) 2009 Jay Lawson (jaylawson39 at yahoo.com). All rights reserved.
 * Copyright (c) 2020 - The MegaMek Team. All Rights Reserved.
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

import megamek.common.MiscType;
import megamek.common.TargetRoll;
import megamek.common.WeaponType;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.*;
import mekhq.campaign.parts.equipment.EquipmentPart;
import mekhq.gui.CampaignGUI;
import mekhq.gui.model.PartsStoreTableModel;
import mekhq.gui.model.PartsTableModel;
import mekhq.gui.sorter.PartsDetailSorter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.function.Predicate;

/**
 * @author Taharqa
 */
public class PartsStoreDialog extends AbstractPartsDialog {

    private boolean addToCampaign;

    private JCheckBox hideImpossible;
    private JButton btnUseBonusPart;

    //endregion Variable Declarations

    /** Creates new form PartsStoreDialog */
    public PartsStoreDialog(boolean modal, CampaignGUI gui) {
        this(gui.getFrame(), modal, gui, gui.getCampaign(), true);
    }

    public PartsStoreDialog(final JFrame frame, final boolean modal, final CampaignGUI gui,
                            final Campaign campaign, final boolean add) {
        super(frame, modal, gui);
        this.addToCampaign = add;
        initComponents();
        this.filterPredicate = super.filterPredicate.and(e -> {
            int target = partsModel.getPartProxyAt(e.getIdentifier()).getTarget().getTargetRoll().getValue();
            if (hideImpossible.isSelected()) {
                if (target == TargetRoll.IMPOSSIBLE || target == TargetRoll.AUTOMATIC_FAIL) {
                    return false;
                }
            }
            return true;
        });
        filterParts();
        setLocationRelativeTo(frame);
        setUserPreferences();
    }

    protected void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form");
        setTitle(resourceMap.getString("Form.title"));

        getContentPane().setLayout(new BorderLayout());
        partsTable = new JTable(partsModel);
        partsTable.setName("partsTable");
        partsSorter = new TableRowSorter<>(partsModel);
        partsSorter.setComparator(PartsStoreTableModel.COL_DETAIL, new PartsDetailSorter());
        partsTable.setRowSorter(partsSorter);
        TableColumn column;
        for (int i = 0; i < PartsStoreTableModel.N_COL; i++) {
            column = partsTable.getColumnModel().getColumn(i);
            column.setPreferredWidth(partsModel.getColumnWidth(i));
            column.setCellRenderer(partsModel.getRenderer());
        }
        partsTable.setIntercellSpacing(new Dimension(0, 0));
        partsTable.setShowGrid(false);
        JScrollPane scrollPartsTable = new JScrollPane();
        scrollPartsTable.setName("scrollPartsTable");
        scrollPartsTable.setViewportView(partsTable);
        getContentPane().add(scrollPartsTable, BorderLayout.CENTER);

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

        hideImpossible = new JCheckBox(resourceMap.getString("hideImpossible.text"));
        hideImpossible.setName("hideImpossible");
        hideImpossible.addActionListener(e -> filterParts());
        c.gridx = 2;
        panFilter.add(hideImpossible, c);

        getContentPane().add(panFilter, BorderLayout.PAGE_START);

        JPanel panButtons = new JPanel();
        JButton btnAdd;
        JButton btnClose;
        if (addToCampaign) {
            panButtons.setLayout(new GridBagLayout());

            //region Buy
            JButton btnBuy = new JButton(resourceMap.getString("btnBuy.text"));
            btnBuy.addActionListener(evt -> {
                if (partsTable.getSelectedRowCount() > 0) {
                    int[] selectedRow = partsTable.getSelectedRows();
                    for (int i : selectedRow) {
                        PartsStoreTableModel.PartProxy partProxy = partsModel.getPartProxyAt(partsTable.convertRowIndexToModel(i));
                        addPart(true, partProxy.getPart(), 1);
                        partProxy.updateTargetAndInventories();
                        partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TARGET);
                        partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TRANSIT);
                        partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_SUPPLY);
                        partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_QUEUE);
                    }
                }
            });
            panButtons.add(btnBuy, new GridBagConstraints());
            //endregion Buy

            //region Buy Bulk
            JButton btnBuyBulk = new JButton(resourceMap.getString("btnBuyBulk.text"));
            btnBuyBulk.addActionListener(evt -> {
                if (partsTable.getSelectedRowCount() > 0) {
                    int[] selectedRow = partsTable.getSelectedRows();
                    for (int i : selectedRow) {
                        PartsStoreTableModel.PartProxy partProxy = partsModel.getPartProxyAt(partsTable.convertRowIndexToModel(i));
                        int quantity = 1;
                        PopupValueChoiceDialog pcd = new PopupValueChoiceDialog(campaignGUI.getFrame(),
                                true, "How Many " + partProxy.getName() + "?", quantity,
                                1, CampaignGUI.MAX_QUANTITY_SPINNER);
                        pcd.setVisible(true);
                        quantity = pcd.getValue();

                        if (quantity > 0) {
                            addPart(true, false, partProxy.getPart(), quantity);
                            partProxy.updateTargetAndInventories();
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TARGET);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TRANSIT);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_SUPPLY);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_QUEUE);
                        }
                    }
                }
            });
            panButtons.add(btnBuyBulk, new GridBagConstraints());
            //endregion Buy Bulk

            //region Bonus Part
            if (campaign.getCampaignOptions().isUseAtB() && campaign.hasActiveContract()) {
                btnUseBonusPart = new JButton(resourceMap.getString("useBonusPart.text") + " (" + campaign.totalBonusParts() + ")");
                btnUseBonusPart.addActionListener(evt -> {
                    if (partsTable.getSelectedRowCount() > 0) {
                        int[] selectedRow = partsTable.getSelectedRows();
                        for (int i : selectedRow) {
                            PartsStoreTableModel.PartProxy partProxy = partsModel.getPartProxyAt(partsTable.convertRowIndexToModel(i));
                            addPart(true, campaign.totalBonusParts() > 0, partProxy.getPart(), 1);
                            partProxy.updateTargetAndInventories();
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TARGET);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TRANSIT);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_SUPPLY);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_QUEUE);

                            btnUseBonusPart.setText(resourceMap.getString("useBonusPart.text") + " (" + campaign.totalBonusParts() + ")");
                            btnUseBonusPart.setVisible(campaign.totalBonusParts() > 0);
                        }
                    }
                });
                btnUseBonusPart.setVisible(campaign.totalBonusParts() > 0);

                panButtons.add(btnUseBonusPart, new GridBagConstraints());
            }
            //endregion Bonus Part

            //region Add
            btnAdd = new JButton(resourceMap.getString("btnGMAdd.text"));
            btnAdd.addActionListener(evt -> {
                if (partsTable.getSelectedRowCount() > 0) {
                    int[] selectedRow = partsTable.getSelectedRows();
                    for (int i : selectedRow) {
                        PartsStoreTableModel.PartProxy partProxy = partsModel.getPartProxyAt(partsTable.convertRowIndexToModel(i));
                        addPart(false, partProxy.getPart(), 1);
                        partProxy.updateTargetAndInventories();
                        partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TARGET);
                        partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TRANSIT);
                        partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_SUPPLY);
                        partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_QUEUE);
                    }
                }
            });
            if (campaign.isGM()) {
                panButtons.add(btnAdd, new GridBagConstraints());
            }
            //endregion Add

            //region Add Bulk
            JButton btnAddBulk = new JButton(resourceMap.getString("btnAddBulk.text"));
            btnAddBulk.addActionListener(evt -> {
                if (partsTable.getSelectedRowCount() > 0) {
                    int[] selectedRow = partsTable.getSelectedRows();
                    for (int i : selectedRow) {
                        PartsStoreTableModel.PartProxy partProxy = partsModel.getPartProxyAt(partsTable.convertRowIndexToModel(i));

                        int quantity = 1;
                        PopupValueChoiceDialog pcd = new PopupValueChoiceDialog(campaignGUI.getFrame(),
                                true, "How Many " + partProxy.getName() + "?", quantity,
                                1, CampaignGUI.MAX_QUANTITY_SPINNER);
                        pcd.setVisible(true);
                        quantity = pcd.getValue();

                        if (quantity > 0) {
                            addPart(false, partProxy.getPart(), quantity);
                            partProxy.updateTargetAndInventories();
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TARGET);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_TRANSIT);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_SUPPLY);
                            partsModel.fireTableCellUpdated(partsTable.convertRowIndexToModel(i), PartsStoreTableModel.COL_QUEUE);
                        }
                    }
                }
            });
            if (campaign.isGM()) {
                panButtons.add(btnAddBulk, new GridBagConstraints());
            }
            //endregion Add Bulk

            //region Button Close
            btnClose = new JButton(resourceMap.getString("btnClose.text"));
            btnClose.addActionListener(evt -> setVisible(false));
            //endregion Button Close
        } else {
            //if we aren't adding the unit to the campaign, then different buttons
            btnAdd = new JButton(resourceMap.getString("btnAdd.text"));
            btnAdd.addActionListener(evt -> {
                setSelectedPart();
                setVisible(false);
            });
            panButtons.add(btnAdd, new GridBagConstraints());

            btnClose = new JButton(resourceMap.getString("btnCancel.text"));
            btnClose.addActionListener(evt -> {
                selectedPart = null;
                setVisible(false);
            });
        }
        panButtons.add(btnClose, new GridBagConstraints());

        getContentPane().add(panButtons, BorderLayout.PAGE_END);
        this.setPreferredSize(new Dimension(700,600));
        pack();
    }

    private void addPart(boolean purchase, Part part, int quantity) {
        addPart(purchase, false, part, quantity);
    }

    private void addPart(boolean purchase, boolean bonus, Part part, int quantity) {
        if (bonus) {
            campaign.spendBonusPart(part.getAcquisitionWork());
        } else if (purchase) {
            campaign.getShoppingList().addShoppingItem(part.getAcquisitionWork(), quantity, campaign);
        } else {
            while (quantity > 0) {
                campaign.getQuartermaster().addPart(part.clone(), 0);
                quantity--;
            }
        }
    }

}
