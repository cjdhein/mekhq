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
import mekhq.campaign.parts.PartsGenerationResult;
import mekhq.gui.CampaignGUI;
import mekhq.gui.model.PartsGenerationResultsTableModel;
import mekhq.gui.sorter.FormattedNumberSorter;
import mekhq.gui.sorter.TwoNumbersSorter;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Collections;
import java.util.Set;

/**
 * A dialog to show the resulting parts from an InventoryGeneration run, alongside parts in use, ordered, and in transit.
 */
public class PartsGenerationReportDialog extends JDialog {

    private JPanel overviewPartsPanel;
    private JTable overviewPartsGenerationResultTable;
    private PartsGenerationResultsTableModel overviewPartsModel;
    private Set<PartsGenerationResult> partsGenerationResults;
    private Campaign campaign;

    public PartsGenerationReportDialog(Frame frame, Campaign campaign, boolean modal, Set<PartsGenerationResult> generationResults) {
        super(frame, modal);
        this.campaign = campaign;
        initComponents();
        partsGenerationResults = generationResults;
        refreshOverviewPartsGenerationResults();
        pack();
        setLocationRelativeTo(frame);
    }

    private void initComponents() {
        overviewPartsPanel = new JPanel(new BorderLayout());

        overviewPartsModel = new PartsGenerationResultsTableModel();
        overviewPartsGenerationResultTable = new JTable(overviewPartsModel);
        overviewPartsGenerationResultTable.setRowSelectionAllowed(false);
        overviewPartsGenerationResultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumn column;
        for (int i = 0; i < overviewPartsModel.getColumnCount(); ++i) {
            column = overviewPartsGenerationResultTable.getColumnModel().getColumn(i);
            column.setCellRenderer(overviewPartsModel.getRenderer());
            if (overviewPartsModel.hasConstantWidth(i)) {
                column.setMinWidth(overviewPartsModel.getWidth(i));
                column.setMaxWidth(overviewPartsModel.getWidth(i));
            } else {
                column.setPreferredWidth(overviewPartsModel.getPreferredWidth(i));
            }
        }
        overviewPartsGenerationResultTable.setIntercellSpacing(new Dimension(0, 0));
        overviewPartsGenerationResultTable.setShowGrid(false);
        TableRowSorter<PartsGenerationResultsTableModel> partsGenerationResultSorter = new TableRowSorter<>(overviewPartsModel);
        partsGenerationResultSorter.setSortsOnUpdates(true);
        // Numeric columns
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_IN_USE, new FormattedNumberSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_STORED, new FormattedNumberSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_TONNAGE, new FormattedNumberSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_IN_TRANSFER, new TwoNumbersSorter());
        partsGenerationResultSorter.setComparator(PartsGenerationResultsTableModel.COL_COST, new FormattedNumberSorter());
        // Default starting sort
        partsGenerationResultSorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        overviewPartsGenerationResultTable.setRowSorter(partsGenerationResultSorter);

        overviewPartsPanel.add(new JScrollPane(overviewPartsGenerationResultTable), BorderLayout.CENTER);

        JPanel panButtons = new JPanel(new GridBagLayout());
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(evt -> setVisible(false));
        panButtons.add(btnClose, new GridBagConstraints());
        overviewPartsPanel.add(panButtons, BorderLayout.PAGE_END);

        this.setLayout(new BorderLayout());
        this.add(overviewPartsPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(1000, 800));

    }
    private void refreshOverviewPartsGenerationResults() {
        overviewPartsModel.setData(partsGenerationResults);
        TableColumnModel tcm = overviewPartsGenerationResultTable.getColumnModel();
    }
}
