/*
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
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

import megamek.client.ui.baseComponents.MMButton;
import megamek.client.ui.enums.ValidationState;
import megamek.common.annotations.Nullable;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.*;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.universe.inventoryGenerator.InventoryGenerator;
import mekhq.campaign.universe.inventoryGenerator.InventoryGeneratorOptions;
import mekhq.campaign.universe.enums.PartGenerationMethod;
import mekhq.campaign.universe.generators.ammunitionGenerators.BasicAmmunitionGenerator;
import mekhq.campaign.universe.generators.armourGenerators.BasicArmourGenerator;
import mekhq.campaign.universe.generators.partGenerators.AbstractPartGenerator;
import mekhq.gui.baseComponents.AbstractMHQValidationButtonDialog;
import mekhq.gui.panels.InventoryGeneratorOptionsPanel;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class InventoryGeneratorDialog extends AbstractMHQValidationButtonDialog {
    //region Variable Declarations
    private Campaign campaign;
    private InventoryGeneratorOptions inventoryGeneratorOptions;
    private InventoryGeneratorOptionsPanel inventoryGeneratorOptionsPanel;
    //endregion Variable Declarations

    //region Constructors
    public InventoryGeneratorDialog(final JFrame frame, final Campaign campaign) {
        super(frame, "InventoryGeneratorDialog", "InventoryGeneratorDialog.title");
        setCampaign(campaign);
        setInventoryGeneratorOptions(null);
        initialize();
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(final Campaign campaign) {
        this.campaign = campaign;
    }

    public @Nullable InventoryGeneratorOptions getInventoryGeneratorOptions() {
        return inventoryGeneratorOptions;
    }

    public void setInventoryGeneratorOptions(final @Nullable InventoryGeneratorOptions inventoryGeneratorOptions) {
        this.inventoryGeneratorOptions = inventoryGeneratorOptions;
    }

    public InventoryGeneratorOptionsPanel getInventoryGeneratorOptionsPanel() {
        return inventoryGeneratorOptionsPanel;
    }

    public void setInventoryGeneratorOptionsPanel(final InventoryGeneratorOptionsPanel inventoryGeneratorOptionsPanel) {
        this.inventoryGeneratorOptionsPanel = inventoryGeneratorOptionsPanel;
    }
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected Container createCenterPane() {
        setInventoryGeneratorOptionsPanel(new InventoryGeneratorOptionsPanel(getFrame(), getCampaign(),
                getInventoryGeneratorOptions()));
        return new JScrollPane(getInventoryGeneratorOptionsPanel());
    }

    @Override
    protected JPanel createButtonPanel() {
        final JPanel panel = new JPanel(new GridLayout(1, 3));

        setOkButton(new MMButton("btnGenerate", resources, "Generate.text",
                "CompanyGenerationDialog.btnGenerate.toolTipText", this::okButtonActionPerformed));
        panel.add(getOkButton());

        panel.add(new MMButton("btnCancel", resources, "Cancel.text",
                "Cancel.toolTipText", this::cancelActionPerformed));

        panel.add(new MMButton("btnRestore", resources, "RestoreDefaults.text",
                "CompanyGenerationDialog.btnRestore.toolTipText",
                evt -> getInventoryGeneratorOptionsPanel().setOptions(PartGenerationMethod.WINDCHILD)));

        return panel;
    }
    //endregion Initialization


    /*TODO: Add generated parts to a temp warehouse and compare to current campaign warehouse and remove all items already in stock.
            Then display a dialog with a table of all items and options to select which to order/add immediately.
     */
    @Override
    protected void okAction() {
        final InventoryGeneratorOptions options = getInventoryGeneratorOptionsPanel().createOptionsFromPanel();
        final InventoryGenerator inventoryGenerator = new InventoryGenerator(options, getCampaign());
        List<PartsGenerationResult> generatedParts = new ArrayList<>(inventoryGenerator.generateInventory(true));

        new PartsGenerationReportDialog(getFrame(), getCampaign(), true, generatedParts).setVisible(true);
    }

    @Override
    protected ValidationState validateAction(final boolean display) {
        return getInventoryGeneratorOptionsPanel().validateOptions(display);
    }
}
