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

import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.Part;
import mekhq.campaign.parts.PartInUse;
import mekhq.campaign.parts.PartsGenerationResult;
import mekhq.campaign.work.IAcquisitionWork;
import mekhq.gui.model.PartsGenerationResultsTableModel;
import mekhq.gui.sorter.FormattedNumberSorter;
import mekhq.gui.sorter.TwoNumbersSorter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A dialog to show the resulting parts from an InventoryGenerator run, alongside parts in use, ordered, and in transit.
 */
public class PartsGenerationReportDialog extends JDialog {

    private JPanel overviewPartsPanel;
    private JTable overviewPartsGenerationResultsTable;
    private PartsGenerationResultsTableModel overviewPartsModel;
    private Collection<PartsGenerationResult> partsGenerationResults;
    private Campaign campaign;
    protected ResourceBundle resources;

    public PartsGenerationReportDialog(Frame frame, Campaign c, boolean modal, List<PartsGenerationResult> generationResults) {
        super(frame, modal);
        campaign = c;
        this.resources = ResourceBundle.getBundle("mekhq.resources.PartsGenerationResultsTableModel",
            MekHQ.getMHQOptions().getLocale());
        initComponents();
        partsGenerationResults = generationResults;
        refreshOverviewPartsGenerationResults();
        pack();
        setLocationRelativeTo(frame);
    }

    private void initComponents() {
        overviewPartsPanel = new JPanel(new BorderLayout());

        overviewPartsModel = new PartsGenerationResultsTableModel();
        overviewPartsGenerationResultsTable = new JTable(overviewPartsModel);
        overviewPartsGenerationResultsTable.setRowSelectionAllowed(false);
        overviewPartsGenerationResultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumn column;
        for (int i = 0; i < overviewPartsModel.getColumnCount()-1; ++i) {
            column = overviewPartsGenerationResultsTable.getColumnModel().getColumn(i);
                column.setCellRenderer(overviewPartsModel.getRenderer());
            }
            overviewPartsGenerationResultsTable.setIntercellSpacing(new Dimension(0, 0));
            overviewPartsGenerationResultsTable.setShowGrid(false);
        TableRowSorter<PartsGenerationResultsTableModel> partsGenerationResultSorter = new TableRowSorter<>(overviewPartsModel);
        partsGenerationResultSorter.setSortsOnUpdates(true);
        // Numeric columns
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_IN_USE, new FormattedNumberSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_STORED, new FormattedNumberSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_TONNAGE, new FormattedNumberSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_IN_TRANSFER, new FormattedNumberSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_PLANNED, new FormattedNumberSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_COST, new FormattedNumberSorter());
        // Default starting sort
        partsGenerationResultSorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        overviewPartsGenerationResultsTable.setRowSorter(partsGenerationResultSorter);

        overviewPartsPanel.add(new JScrollPane(overviewPartsGenerationResultsTable), BorderLayout.CENTER);

        JPanel panButtons = new JPanel(new GridBagLayout());
        JButton btnAdd = new JButton(resources.getString("btnAdd.text"));
        btnAdd.addActionListener(evt -> handleAddAction());
        JButton btnOrder = new JButton(resources.getString("btnOrder.text"));
        btnOrder.addActionListener(evt -> handleOrderAction());
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(evt -> setVisible(false));
        panButtons.add(btnAdd, new GridBagConstraints());
        panButtons.add(btnOrder, new GridBagConstraints());
        panButtons.add(btnClose, new GridBagConstraints());
        overviewPartsPanel.add(panButtons, BorderLayout.PAGE_END);

        this.setLayout(new BorderLayout());
        this.add(overviewPartsPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000, 800));

    }

    private void handleAddAction() {
        Collection<PartsGenerationResult> resultToAcquire = getPartsTaggedForAcquisition();
        resultToAcquire.forEach(pgr -> campaign.getQuartermaster().addPart((Part) pgr.getPartToBuy().getNewEquipment(), 0));
        setVisible(false);
    }

    private void handleOrderAction() {
        Collection<PartsGenerationResult> resultToAcquire = getPartsTaggedForAcquisition();
        resultToAcquire.forEach(pgr -> campaign.getShoppingList().addShoppingItem(pgr.getPartToBuy(), pgr.getPlannedCount(),campaign));
        setVisible(false);
    }

    private Collection<PartsGenerationResult> getPartsTaggedForAcquisition() {
        for (int i = 0; i < overviewPartsModel.getRowCount(); i++) {
            boolean shouldAcquire = (boolean) overviewPartsGenerationResultsTable.getValueAt(i, PartsGenerationResultsTableModel.COL_CHKBX_INCLUDE);
            overviewPartsModel.getPartsGenerationResult(i).setShouldBeAcquired(shouldAcquire);
        }
       return partsGenerationResults.stream().filter(PartsGenerationResult::isShouldBeAcquired).collect(Collectors.toList());
    }

    private void refreshOverviewPartsGenerationResults() {
        overviewPartsModel.setData(partsGenerationResults);
    }

}
