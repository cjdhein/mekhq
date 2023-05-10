/*
 * Copyright (c) 2021-2022 - The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.panels;

import megamek.client.ui.baseComponents.JDisableablePanel;
import megamek.client.ui.baseComponents.MMComboBox;
import megamek.client.ui.enums.ValidationState;
import megamek.common.annotations.Nullable;
import mekhq.campaign.Campaign;
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.universe.enums.PartGenerationMethod;
import mekhq.campaign.universe.generators.partGenerators.CustomPartGeneratorOptions;
import mekhq.campaign.universe.inventoryGeneration.InventoryGenerationOptions;
import mekhq.gui.baseComponents.AbstractMHQScrollablePanel;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;

/**
 * @author cjdhein
 */
public class CustomPartGeneratorOptionsPanel extends AbstractMHQScrollablePanel  {
    //region Variable Declarations
    private final Campaign campaign;

    // Spares
    private JSpinner spnSparePartTargetMultiplier;
    //region Constructors
    public CustomPartGeneratorOptionsPanel(final JFrame frame, final Campaign campaign,
                                           final @Nullable CustomPartGeneratorOptions customPartGeneratorOptions) {
        super(frame, "SparePartsGenerationOptionsPanel", new GridBagLayout());
        this.campaign = campaign;
        setTracksViewportWidth(false);

        initialize();
        if (customPartGeneratorOptions == null) {
            setOptions();
        } else {
            setOptions(customPartGeneratorOptions);
        }
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public JSpinner getSpnSparePartTargetMultiplier() {
        return spnSparePartTargetMultiplier;
    }
    public void setSpnSparePartTargetMultiplier(final JSpinner spnSparePartTargetMultiplier) {
        this.spnSparePartTargetMultiplier = spnSparePartTargetMultiplier;
    }
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected void initialize() {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createGeneratorOptionsPanel(), gbc);
    }

    private JPanel createGeneratorOptionsPanel() {
        // Initialize Labels Used in ActionListeners

        // Create Panel Components
        final JLabel lblSparePartTargetMultiplier = new JLabel(resources.getString("lblSparePartTargetMultiplier.text"));
        lblSparePartTargetMultiplier.setName("lblSparePartTargetMultiplier");
        lblSparePartTargetMultiplier.setToolTipText(resources.getString("lblSparePartTargetMultiplier.toolTipText"));
        setSpnSparePartTargetMultiplier(new JSpinner(new SpinnerNumberModel(0.1, 0.0, 100.0, 0.05)));
        getSpnSparePartTargetMultiplier().setToolTipText(resources.getString("lblSparePartTargetMultiplier.toolTipText"));
        getSpnSparePartTargetMultiplier().setName("spnSparePartTargetMultiplier");
        // Programmatically Assign Accessibility Labels
        lblSparePartTargetMultiplier.setLabelFor(getSpnSparePartTargetMultiplier());
        
        // Layout the UI
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(resources.getString("sparesPanel.title")));
        panel.setName("sparesPanel");
        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblSparePartTargetMultiplier)
                                .addComponent(getSpnSparePartTargetMultiplier(), Alignment.LEADING))
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblSparePartTargetMultiplier)
                                .addComponent(getSpnSparePartTargetMultiplier()))
        );

        return panel;
    }
    //endregion Initialization

    //region Options

    /**
     * Sets the options for this panel to the default for the selected CompanyGenerationMethod
     */
    public void setOptions() {
        setOptions(new CustomPartGeneratorOptions());
    }

    /**
     * Sets the options for this panel based on the provided CustomPartGeneratorOptions
     * @param options the CustomPartGeneratorOptions to use
     */
    public void setOptions(final CustomPartGeneratorOptions options) {
        // Spares
        getSpnSparePartTargetMultiplier().setValue(options.getSparePartTargetMultiplier());
    }

    /**
     * @return the CustomPartGeneratorOptions created from the current panel
     */
    public CustomPartGeneratorOptions createOptionsFromPanel() {
        final CustomPartGeneratorOptions options = new CustomPartGeneratorOptions();

        // Spares
        options.setSparePartTargetMultiplier((Double) getSpnSparePartTargetMultiplier().getValue());
        return options;
    }

    /**
     * Validates the data contained in this panel, returning the current state of validation.
     * @param display to display dialogs containing the messages or not
     * @return true if the data validates successfully, otherwise false
     */
    public ValidationState validateOptions(final boolean display) {
        //region Errors

        // The options specified are correct, and thus can be saved
        return ValidationState.SUCCESS;
    }
    //endregion Options
    /**
     * This override forces all child components to be the same value for enabled as this component,
     * thus allowing one to easily enable/disable child components and panels.
     * @param enabled whether to enable the child components or not
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        for (final Component component : getComponents()) {
            component.setEnabled(enabled);
        }
    }

}
