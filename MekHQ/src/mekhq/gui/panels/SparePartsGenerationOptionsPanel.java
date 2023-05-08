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
import megamek.common.EntityWeightClass;
import megamek.common.annotations.Nullable;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.enums.PersonnelRole;
import mekhq.campaign.universe.Factions;
import mekhq.campaign.universe.companyGeneration.CompanyGenerationOptions;
import mekhq.campaign.universe.companyGeneration.SparePartsGenerationOptions;
import mekhq.campaign.universe.enums.*;
import mekhq.gui.FileDialogs;
import mekhq.gui.baseComponents.AbstractMHQScrollablePanel;
import mekhq.gui.displayWrappers.FactionDisplay;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JSpinner.NumberEditor;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Justin "Windchild" Bowen
 */
public class SparePartsGenerationOptionsPanel extends AbstractMHQScrollablePanel {
    //region Variable Declarations
    private final Campaign campaign;

    // Spares
    private JCheckBox chkGenerateMothballedSpareUnits;
    private JSpinner spnSparesPercentOfActiveUnits;
    private MMComboBox<PartGenerationMethod> comboPartGenerationMethod;
    private JSpinner spnStartingArmourWeight;
    private JCheckBox chkGenerateSpareAmmunition;
    private JSpinner spnNumberReloadsPerWeapon;
    private JCheckBox chkGenerateFractionalMachineGunAmmunition;

    // Finances
    private JCheckBox chkPayForParts;
    private JCheckBox chkPayForArmour;
    private JCheckBox chkPayForAmmunition;

    //region Constructors
    public SparePartsGenerationOptionsPanel(final JFrame frame, final Campaign campaign,
                                            final @Nullable SparePartsGenerationOptions sparePartsGenerationOptions) {
        super(frame, "SparePartsGenerationOptionsPanel", new GridBagLayout());
        this.campaign = campaign;
        setTracksViewportWidth(false);

        initialize();

        setOptions(sparePartsGenerationOptions);
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    //region Spares
    public MMComboBox<PartGenerationMethod> getComboPartGenerationMethod() {
        return comboPartGenerationMethod;
    }

    public void setComboPartGenerationMethod(final MMComboBox<PartGenerationMethod> comboPartGenerationMethod) {
        this.comboPartGenerationMethod = comboPartGenerationMethod;
    }

    public JSpinner getSpnStartingArmourWeight() {
        return spnStartingArmourWeight;
    }

    public void setSpnStartingArmourWeight(final JSpinner spnStartingArmourWeight) {
        this.spnStartingArmourWeight = spnStartingArmourWeight;
    }

    public JCheckBox getChkGenerateSpareAmmunition() {
        return chkGenerateSpareAmmunition;
    }

    public void setChkGenerateSpareAmmunition(final JCheckBox chkGenerateSpareAmmunition) {
        this.chkGenerateSpareAmmunition = chkGenerateSpareAmmunition;
    }

    public JSpinner getSpnNumberReloadsPerWeapon() {
        return spnNumberReloadsPerWeapon;
    }

    public void setSpnNumberReloadsPerWeapon(final JSpinner spnNumberReloadsPerWeapon) {
        this.spnNumberReloadsPerWeapon = spnNumberReloadsPerWeapon;
    }

    public JCheckBox getChkGenerateFractionalMachineGunAmmunition() {
        return chkGenerateFractionalMachineGunAmmunition;
    }

    public void setChkGenerateFractionalMachineGunAmmunition(final JCheckBox chkGenerateFractionalMachineGunAmmunition) {
        this.chkGenerateFractionalMachineGunAmmunition = chkGenerateFractionalMachineGunAmmunition;
    }
    //endregion Spares

    //region Finances
    public JCheckBox getChkPayForParts() {
        return chkPayForParts;
    }

    public void setChkPayForParts(final JCheckBox chkPayForParts) {
        this.chkPayForParts = chkPayForParts;
    }

    public JCheckBox getChkPayForArmour() {
        return chkPayForArmour;
    }

    public void setChkPayForArmour(final JCheckBox chkPayForArmour) {
        this.chkPayForArmour = chkPayForArmour;
    }

    public JCheckBox getChkPayForAmmunition() {
        return chkPayForAmmunition;
    }

    public void setChkPayForAmmunition(final JCheckBox chkPayForAmmunition) {
        this.chkPayForAmmunition = chkPayForAmmunition;
    }
    //endregion Finances
    //endregion Getters/Setters

    //region Initialization
    @Override
    protected void initialize() {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createSparesPanel(), gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        add(createFinancesPanel(), gbc);
    }

    private JPanel createSparesPanel() {
        // Initialize Labels Used in ActionListeners
        final JLabel lblSparesPercentOfActiveUnits = new JLabel();
        final JLabel lblNumberReloadsPerWeapon = new JLabel();

        // Create Panel Components
        final JLabel lblPartGenerationMethod = new JLabel(resources.getString("lblPartGenerationMethod.text"));
        lblPartGenerationMethod.setToolTipText(resources.getString("lblPartGenerationMethod.toolTipText"));
        lblPartGenerationMethod.setName("lblPartGenerationMethod");

        setComboPartGenerationMethod(new MMComboBox<>("comboPartGenerationMethod", PartGenerationMethod.values()));
        getComboPartGenerationMethod().setToolTipText(resources.getString("lblPartGenerationMethod.toolTipText"));
        getComboPartGenerationMethod().setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value,
                                                          final int index, final boolean isSelected,
                                                          final boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PartGenerationMethod) {
                    list.setToolTipText(((PartGenerationMethod) value).getToolTipText());
                }
                return this;
            }
        });

        final JLabel lblStartingArmourWeight = new JLabel(resources.getString("lblStartingArmourWeight.text"));
        lblStartingArmourWeight.setToolTipText(resources.getString("lblStartingArmourWeight.toolTipText"));
        lblStartingArmourWeight.setName("lblStartingArmourWeight");

        setSpnStartingArmourWeight(new JSpinner(new SpinnerNumberModel(0, 0, 500, 1)));
        getSpnStartingArmourWeight().setToolTipText(resources.getString("lblStartingArmourWeight.toolTipText"));
        getSpnStartingArmourWeight().setName("spnStartingArmourWeight");

        setChkGenerateSpareAmmunition(new JCheckBox(resources.getString("chkGenerateSpareAmmunition.text")));
        getChkGenerateSpareAmmunition().setToolTipText(resources.getString("chkGenerateSpareAmmunition.toolTipText"));
        getChkGenerateSpareAmmunition().setName("chkGenerateSpareAmmunition");
        getChkGenerateSpareAmmunition().addActionListener(evt -> {
            final boolean selected = getChkGenerateSpareAmmunition().isSelected();
            lblNumberReloadsPerWeapon.setEnabled(selected);
            getSpnNumberReloadsPerWeapon().setEnabled(selected);
            getChkGenerateFractionalMachineGunAmmunition().setEnabled(selected);
        });

        lblNumberReloadsPerWeapon.setText(resources.getString("lblNumberReloadsPerWeapon.text"));
        lblNumberReloadsPerWeapon.setToolTipText(resources.getString("lblNumberReloadsPerWeapon.toolTipText"));
        lblNumberReloadsPerWeapon.setName("lblNumberReloadsPerWeapon");

        setSpnNumberReloadsPerWeapon(new JSpinner(new SpinnerNumberModel(0, 0, 25, 1)));
        getSpnNumberReloadsPerWeapon().setToolTipText(resources.getString("lblNumberReloadsPerWeapon.toolTipText"));
        getSpnNumberReloadsPerWeapon().setName("spnNumberReloadsPerWeapon");

        setChkGenerateFractionalMachineGunAmmunition(new JCheckBox(resources.getString("chkGenerateFractionalMachineGunAmmunition.text")));
        getChkGenerateFractionalMachineGunAmmunition().setToolTipText(resources.getString("chkGenerateFractionalMachineGunAmmunition.toolTipText"));
        getChkGenerateFractionalMachineGunAmmunition().setName("chkGenerateFractionalMachineGunAmmunition");

        // Programmatically Assign Accessibility Labels
     lblPartGenerationMethod.setLabelFor(getComboPartGenerationMethod());
        lblStartingArmourWeight.setLabelFor(getSpnStartingArmourWeight());
        lblNumberReloadsPerWeapon.setLabelFor(getSpnNumberReloadsPerWeapon());

        // Disable Panel Portions by Default
        getChkGenerateSpareAmmunition().setSelected(true);
        getChkGenerateSpareAmmunition().doClick();

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
                                .addComponent(lblPartGenerationMethod)
                                .addComponent(getComboPartGenerationMethod(), Alignment.LEADING))
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblStartingArmourWeight)
                                .addComponent(getSpnStartingArmourWeight(), Alignment.LEADING))
                        .addComponent(getChkGenerateSpareAmmunition())
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(lblNumberReloadsPerWeapon)
                                .addComponent(getSpnNumberReloadsPerWeapon(), Alignment.LEADING))
                        .addComponent(getChkGenerateFractionalMachineGunAmmunition())
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblPartGenerationMethod)
                                .addComponent(getComboPartGenerationMethod()))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblStartingArmourWeight)
                                .addComponent(getSpnStartingArmourWeight()))
                        .addComponent(getChkGenerateSpareAmmunition())
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(lblNumberReloadsPerWeapon)
                                .addComponent(getSpnNumberReloadsPerWeapon()))
                        .addComponent(getChkGenerateFractionalMachineGunAmmunition())
        );

        return panel;
    }


    private JPanel createFinancesPanel() {
        // Initialize Components Used in ActionListeners
        final JPanel financialDebitsPanel = new JDisableablePanel("financialDebitsPanel");

        createFinancialDebitsPanel(financialDebitsPanel);

        // Layout the UI
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(resources.getString("financesPanel.title")));
        panel.setName("financesPanel");
        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(financialDebitsPanel)
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(financialDebitsPanel)
        );

        return panel;
    }

    private void createFinancialDebitsPanel(final JPanel panel) {
        // Create Panel Components

        getChkPayForParts().setEnabled(true);
        getChkPayForArmour().setEnabled(true);
        getChkPayForAmmunition().setEnabled(true);

        setChkPayForParts(new JCheckBox(resources.getString("chkPayForParts.text")));
        getChkPayForParts().setToolTipText(resources.getString("chkPayForParts.toolTipText"));
        getChkPayForParts().setName("chkPayForParts");

        setChkPayForArmour(new JCheckBox(resources.getString("chkPayForArmour.text")));
        getChkPayForArmour().setToolTipText(resources.getString("chkPayForArmour.toolTipText"));
        getChkPayForArmour().setName("chkPayForArmour");

        setChkPayForAmmunition(new JCheckBox(resources.getString("chkPayForAmmunition.text")));
        getChkPayForAmmunition().setToolTipText(resources.getString("chkPayForAmmunition.toolTipText"));
        getChkPayForAmmunition().setName("chkPayForAmmunition");

        // Disable Panel Portions by Default
        // This is handled by createFinancesPanel

        // Layout the UI
        panel.setBorder(BorderFactory.createTitledBorder(resources.getString("financialDebitsPanel.title")));
        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(getChkPayForParts())
                        .addComponent(getChkPayForArmour())
                        .addComponent(getChkPayForAmmunition())
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(getChkPayForParts())
                        .addComponent(getChkPayForArmour())
                        .addComponent(getChkPayForAmmunition())
        );
    }
    //endregion Initialization

    //region Options
    /**
     * Sets the options for this panel based on the provided CompanyGenerationOptions
     * @param options the CompanyGenerationOptions to use
     */
    public void setOptions(final SparePartsGenerationOptions options) {
        // Spares
        getComboPartGenerationMethod().setSelectedItem(options.getPartGenerationMethod());
        getSpnStartingArmourWeight().setValue(options.getStartingArmourWeight());
        if (getChkGenerateSpareAmmunition().isSelected() != options.isGenerateSpareAmmunition()) {
            getChkGenerateSpareAmmunition().doClick();
        }
        getSpnNumberReloadsPerWeapon().setValue(options.getNumberReloadsPerWeapon());
        getChkGenerateFractionalMachineGunAmmunition().setSelected(options.isGenerateFractionalMachineGunAmmunition());

        // Finances
        getChkPayForParts().setSelected(options.isPayForParts());
        getChkPayForArmour().setSelected(options.isPayForArmour());
        getChkPayForAmmunition().setSelected(options.isPayForAmmunition());
    }

    /**
     * @return the CompanyGenerationOptions created from the current panel
     */
    public SparePartsGenerationOptions createOptionsFromPanel() {
        final SparePartsGenerationOptions options = new SparePartsGenerationOptions();

        // Spares
        options.setPartGenerationMethod(getComboPartGenerationMethod().getSelectedItem());
        options.setStartingArmourWeight((Integer) getSpnStartingArmourWeight().getValue());
        options.setGenerateSpareAmmunition(getChkGenerateSpareAmmunition().isSelected());
        options.setNumberReloadsPerWeapon((Integer) getSpnNumberReloadsPerWeapon().getValue());
        options.setGenerateFractionalMachineGunAmmunition(getChkGenerateFractionalMachineGunAmmunition().isSelected());
    // Finances
        options.setPayForParts(getChkPayForParts().isSelected());
        options.setPayForArmour(getChkPayForArmour().isSelected());
        options.setPayForAmmunition(getChkPayForAmmunition().isSelected());

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

    //region File I/O
    /**
     * Imports CompanyGenerationOptions from an XML file
     */
    public void importOptionsFromXML() {
        FileDialogs.openCompanyGenerationOptions(getFrame())
                .ifPresent(file -> setOptions(SparePartsGenerationOptions.parseFromXML(file)));
    }

    /**
     * Exports the CompanyGenerationOptions displayed on this panel to an XML file.
     */
    public void exportOptionsToXML() {
        FileDialogs.saveCompanyGenerationOptions(getFrame())
                .ifPresent(file -> createOptionsFromPanel().writeToFile(file));
    }
    //endregion File I/O
}
