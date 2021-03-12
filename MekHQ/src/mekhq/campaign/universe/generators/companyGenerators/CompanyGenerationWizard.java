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
package mekhq.campaign.universe.generators.companyGenerators;

import megamek.common.Entity;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.parts.AmmoStorage;
import mekhq.campaign.parts.Armor;
import mekhq.campaign.parts.Part;
import mekhq.campaign.unit.Unit;

import java.util.List;

public class CompanyGenerationWizard {
    //region Variable Declarations
    private final Campaign campaign;
    private final AbstractCompanyGenerator generator;
    //endregion Variable Declarations

    //region Constructors
    public CompanyGenerationWizard(final Campaign campaign, final AbstractCompanyGenerator generator) {
        this.campaign = campaign;
        this.generator = generator;
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public AbstractCompanyGenerator getGenerator() {
        return generator;
    }
    //endregion Getters/Setters

    public void set() {
        generator.applyPhaseZeroToCampaign(getCampaign());

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
    }



    /*
     public void generate() {
     if ((getCompanyGenerator() != null) && (JOptionPane.showConfirmDialog(getFrame(),
     resources.getString("CompanyGenerationPanel.OverwriteGenerationWarning.text"),
     resources.getString("CompanyGenerationPanel.OverwriteGenerationWarning.title"),
     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)) {
     return;
     } else if (!validateOptions()) {
     return;
     }

     setCompanyGenerator(getCompanyGenerationMethod().getGenerator(getCampaign(), createOptionsFromPanel()));

     setCombatPersonnel(getCompanyGenerator().generateCombatPersonnel(getCampaign()));
     setSupportPersonnel(getCompanyGenerator().generateSupportPersonnel(getCampaign()));
     setEntities(getCompanyGenerator().generateUnits(getCampaign(), getCombatPersonnel()));
     }

     public void apply() {
     if (getCompanyGenerator() == null) {
     if (JOptionPane.showConfirmDialog(getFrame(),
     resources.getString("CompanyGenerationPanel.ImmediateApplicationWarning.text"),
     resources.getString("CompanyGenerationPanel.ImmediateApplicationWarning.title"),
     JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
     generate();
     // Catch statement for bad data
     if (getCompanyGenerator() == null) {
     return;
     }
     } else {
     return;
     }
     }

     getCompanyGenerator().applyToCampaign(getCampaign(), getCombatPersonnel(),
     getSupportPersonnel(), getEntities());
     }
     */
}
