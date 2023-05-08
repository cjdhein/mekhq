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
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.event.OrganizationChangedEvent;
import mekhq.campaign.finances.Money;
import mekhq.campaign.finances.enums.TransactionType;
import mekhq.campaign.parts.AmmoStorage;
import mekhq.campaign.parts.Armor;
import mekhq.campaign.parts.Part;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.universe.companyGeneration.SparePartsGenerationOptions;
import mekhq.campaign.universe.enums.PartGenerationMethod;
import mekhq.campaign.universe.generators.ammunitionGenerators.BasicAmmunitionGenerator;
import mekhq.campaign.universe.generators.armourGenerators.BasicArmourGenerator;
import mekhq.campaign.universe.generators.partGenerators.AbstractPartGenerator;
import mekhq.gui.baseComponents.AbstractMHQValidationButtonDialog;
import mekhq.gui.panels.SparePartsGenerationOptionsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
        final JPanel panel = new JPanel(new GridLayout(1, 3));

        setOkButton(new MMButton("btnGenerate", resources, "Generate.text",
                "CompanyGenerationDialog.btnGenerate.toolTipText", this::okButtonActionPerformed));
        panel.add(getOkButton());

        panel.add(new MMButton("btnCancel", resources, "Cancel.text",
                "Cancel.toolTipText", this::cancelActionPerformed));

        panel.add(new MMButton("btnRestore", resources, "RestoreDefaults.text",
                "CompanyGenerationDialog.btnRestore.toolTipText",
                evt -> getSparePartsGenerationOptionsPanel().setOptions()));

        return panel;
    }
    //endregion Initialization

    @Override
    protected void okAction() {
        final SparePartsGenerationOptions options = getSparePartsGenerationOptionsPanel().createOptionsFromPanel();
        final AbstractPartGenerator generator = options.getPartGenerationMethod().getGenerator();
        final List<Unit> units;
        final List<Part> parts;
        final List<Armor> armour;
        final List<AmmoStorage> ammunition;

        units = campaign.getUnits().stream().collect(Collectors.toList());
        if (options.getPartGenerationMethod() != PartGenerationMethod.DISABLED) {
            parts = generator.generate(units, false, false);
            parts.forEach(p -> campaign.getWarehouse().addPart(p, true));
        } else {
            parts = new ArrayList<>();
        }
        armour = BasicArmourGenerator.generateArmour(units, options.getStartingArmourWeight());
        ammunition = BasicAmmunitionGenerator.generateAmmunition(getCampaign(), units, options.isGenerateSpareAmmunition(),
            options.isGenerateFractionalMachineGunAmmunition(), options.getNumberReloadsPerWeapon());

        armour.forEach(a -> campaign.getWarehouse().addPart(a, true));
        ammunition.forEach(a -> campaign.getWarehouse().addPart(a, true));

        final Money costs = options.isPayForParts() ? AbstractPartGenerator.calculatePartCosts(parts) : Money.zero()
            .plus(options.isPayForArmour() ? BasicArmourGenerator.calculateArmourCosts(armour) : Money.zero())
            .plus(options.isPayForAmmunition() ? BasicAmmunitionGenerator.calculateAmmunitionCosts(ammunition) : Money.zero());

        if (!costs.isZero()) {
            campaign.getFinances().debit(TransactionType.EQUIPMENT_PURCHASE, campaign.getLocalDate(), costs, "Purchase of Spare Parts, Armour and Ammunition");
        }

        MekHQ.triggerEvent(new OrganizationChangedEvent(getSparePartsGenerationOptionsPanel().getCampaign().getForces()));
    }
    @Override
    protected ValidationState validateAction(final boolean display) {
        return getSparePartsGenerationOptionsPanel().validateOptions(display);
    }
}
