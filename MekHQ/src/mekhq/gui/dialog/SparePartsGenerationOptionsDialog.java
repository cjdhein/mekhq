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

import megamek.client.ui.enums.ValidationState;
import megamek.common.annotations.Nullable;
import mekhq.campaign.Campaign;
import mekhq.campaign.universe.companyGeneration.SparePartsGenerationOptions;
import mekhq.gui.baseComponents.AbstractMHQValidationButtonDialog;
import mekhq.gui.panels.SparePartsGenerationOptionsPanel;

import javax.swing.*;
import java.awt.*;

/**
 * @author cjdhein
 */
public class SparePartsGenerationOptionsDialog extends AbstractMHQValidationButtonDialog {
    //region Variable Declarations
    private final Campaign campaign;
    private final SparePartsGenerationOptions sparePartsGenerationOptions;
    private SparePartsGenerationOptionsPanel sparePartsGenerationOptionsPanel;
    //endregion Variable Declarations

    //region Constructors
    public SparePartsGenerationOptionsDialog(final JFrame frame, final Campaign campaign,
                                             final @Nullable SparePartsGenerationOptions sparePartsGenerationOptions) {
        //TODO: Move strings to resources
        super(frame, "SparePartsGenerationOptionsDialog", "Generate Spare Parts for The Company");
        this.campaign = campaign;
        this.sparePartsGenerationOptions = sparePartsGenerationOptions;
        initialize();
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public @Nullable SparePartsGenerationOptions getSparePartsGenerationOptions() {
        return sparePartsGenerationOptions;
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
        return getSparePartsGenerationOptionsPanel();
    }
    //endregion Initialization

    //region Button Actions
    @Override
    protected ValidationState validateAction(final boolean display) {
        return getSparePartsGenerationOptionsPanel().validateOptions(display);
    }
    //endregion Button Actions

    
    public @Nullable SparePartsGenerationOptions getSelectedItem() {
        return getResult().isConfirmed() ? getSparePartsGenerationOptionsPanel().createOptionsFromPanel()
                : getSparePartsGenerationOptions();
    }
}
