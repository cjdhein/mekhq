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
import megamek.common.Entity;
import megamek.common.annotations.Nullable;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.event.OrganizationChangedEvent;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.parts.AmmoStorage;
import mekhq.campaign.parts.Armor;
import mekhq.campaign.parts.Part;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.universe.companyGeneration.SparePartsGenerationOptions;
import mekhq.campaign.universe.companyGeneration.CompanyGenerationPersonTracker;
import mekhq.campaign.universe.companyGeneration.SparePartsGenerationOptions;
import mekhq.campaign.universe.generators.companyGenerators.AbstractCompanyGenerator;
import mekhq.campaign.universe.generators.companyGenerators.WindchildCompanyGenerator;
import mekhq.gui.baseComponents.AbstractMHQValidationButtonDialog;
import mekhq.gui.panels.SparePartsGenerationOptionsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * This is currently just a temporary dialog over the SparePartsGenerationOptionsPanel.
 * Wave 5 will be when this gets redone to be far nicer and more customizable.
 * @author Justin "Windchild" Bowen
 */
public class SparePartsGenerationDialog extends AbstractMHQValidationButtonDialog {
    //region Variable Declarations
    private Campaign campaign;
    private SparePartsGenerationOptions sparePartsGenerationOptions;
    private SparePartsGenerationOptionsPanel sparePartsGenerationOptionsPanel;
    //endregion Variable Declarations

    //region Constructors
    public SparePartsGenerationDialog(final JFrame frame, final Campaign campaign) {
        super(frame, "CompanyGenerationDialog", "CompanyGenerationDialog.title");
        setCampaign(campaign);
        setSparePartsGenerationOptions(null);
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

    public @Nullable SparePartsGenerationOptions getSparePartsGenerationOptions() {
        return sparePartsGenerationOptions;
    }

    public void setSparePartsGenerationOptions(final @Nullable SparePartsGenerationOptions sparePartsGenerationOptions) {
        this.sparePartsGenerationOptions = sparePartsGenerationOptions;
    }

    public SparePartsGenerationOptionsPanel getSparePartsGenerationOptionsPanel() {
        return sparePartsGenerationOptionsPanel;
    }

    public void setSparePartsGenerationOptionsPanel(final SparePartsGenerationOptionsPanel sparePartsGenerationOptionsPanel) {
        this.sparePartsGenerationOptionsPanel = sparePartsGenerationOptionsPanel;
    }
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected Container createCenterPane() {
        setSparePartsGenerationOptionsPanel(new SparePartsGenerationOptionsPanel(getFrame(), getCampaign(),
                getSparePartsGenerationOptions()));
        return new JScrollPane(getSparePartsGenerationOptionsPanel());
    }

    @Override
    protected JPanel createButtonPanel() {
        final JPanel panel = new JPanel(new GridLayout(2, 3));

        setOkButton(new MMButton("btnGenerate", resources, "Generate.text",
                "CompanyGenerationDialog.btnGenerate.toolTipText", this::okButtonActionPerformed));
        panel.add(getOkButton());

        panel.add(new MMButton("btnApply", resources, "Apply.text",
                "CompanyGenerationDialog.btnApply.toolTipText", this::okButtonActionPerformed));

        panel.add(new MMButton("btnCancel", resources, "Cancel.text",
                "Cancel.toolTipText", this::cancelActionPerformed));

        panel.add(new MMButton("btnRestore", resources, "RestoreDefaults.text",
                "CompanyGenerationDialog.btnRestore.toolTipText",
                evt -> getSparePartsGenerationOptionsPanel().setOptions()));

        panel.add(new MMButton("btnImport", resources, "Import.text",
                "CompanyGenerationDialog.btnImport.toolTipText",
                evt -> getSparePartsGenerationOptionsPanel().importOptionsFromXML()));

        panel.add(new MMButton("btnExport", resources, "Export.text",
                "CompanyGenerationDialog.btnExport.toolTipText",
                evt -> getSparePartsGenerationOptionsPanel().exportOptionsToXML()));

        return panel;
    }
    //endregion Initialization

    @Override
    protected void okAction() {
        final SparePartsGenerationOptions options = getSparePartsGenerationOptionsPanel().createOptionsFromPanel();
        final AbstractCompanyGenerator generator = new WindchildCompanyGenerator(campaign, options)

        final List<CompanyGenerationPersonTracker> trackers = generator.generatePersonnel(getCampaign());
        generator.generateUnitGenerationParameters(trackers);
        generator.generateEntities(getCampaign(), trackers);
        final List<Unit> units = generator.applyPhaseOneToCampaign(getCampaign(), trackers);

        final List<Entity> mothballedEntities = generator.generateMothballedEntities(getCampaign(), trackers);
        final List<Part> parts = generator.generateSpareParts(units);
        final List<Armor> armour = generator.generateArmour(units);
        final List<AmmoStorage> ammunition = generator.generateAmmunition(getCampaign(), units);
        units.addAll(generator.applyPhaseTwoToCampaign(getCampaign(), mothballedEntities, parts, armour, ammunition));

        final Contract contract = null;
        generator.applyPhaseThreeToCampaign(getCampaign(), trackers, units, parts, armour, ammunition, contract);

        MekHQ.triggerEvent(new OrganizationChangedEvent(getSparePartsGenerationOptionsPanel().getCampaign().getForces()));
    }

    @Override
    protected ValidationState validateAction(final boolean display) {
        return getSparePartsGenerationOptionsPanel().validateOptions(display);
    }
}
