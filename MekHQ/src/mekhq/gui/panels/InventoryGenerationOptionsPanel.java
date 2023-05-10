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
import mekhq.campaign.universe.generators.partGenerators.CustomPartGeneratorOptions;
import mekhq.campaign.universe.inventoryGeneration.InventoryGenerationOptions;
import mekhq.campaign.universe.enums.PartGenerationMethod;
import mekhq.gui.baseComponents.AbstractMHQScrollablePanel;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;

/**
 * @author cjdhein
 */
public class InventoryGenerationOptionsPanel extends AbstractMHQScrollablePanel {
    //region Variable Declarations
    private final Campaign campaign;
    private CustomPartGeneratorOptions customPartGeneratorOptions;
    private JSpinner spnCustPartGenTargetMultiplier;
    private CustomPartGeneratorOptionsPanel customPartGeneratorOptionsPanel;


    // Spares
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
    public InventoryGenerationOptionsPanel(final JFrame frame, final Campaign campaign,
                                           final @Nullable InventoryGenerationOptions inventoryGenerationOptions) {
        super(frame, "SparePartsGenerationOptionsPanel", new GridBagLayout());
        this.campaign = campaign;
        setTracksViewportWidth(false);

        initialize();
        if (inventoryGenerationOptions == null) {
            setOptions(new InventoryGenerationOptions(PartGenerationMethod.WINDCHILD));
        } else {
            setOptions(inventoryGenerationOptions);
        }
    }
    //endregion Constructors

    //region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    public CustomPartGeneratorOptionsPanel getCustomPartGeneratorOptionsPanel() {
        return customPartGeneratorOptionsPanel;
    }

    public void setCustomPartGeneratorOptionsPanel(CustomPartGeneratorOptionsPanel customPartGeneratorOptionsPanel) {
        this.customPartGeneratorOptionsPanel = customPartGeneratorOptionsPanel;
    }

//    public CustomPartGeneratorOptions getCustomPartGeneratorOptions() {
//        return customPartGeneratorOptions;
//    }
//
//    public void setCustomPartGeneratorOptions(CustomPartGeneratorOptions customPartGeneratorOptions) {
//        this.customPartGeneratorOptions = customPartGeneratorOptions;
//    }

    public JSpinner getSpnCustPartGenTargetMultiplier() {
        return spnCustPartGenTargetMultiplier;
    }
    public void setSpnCustPartGenTargetMultiplier(final JSpinner spnCustPartGenTargetMultiplier) {
        this.spnCustPartGenTargetMultiplier = spnCustPartGenTargetMultiplier;
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
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(createSparesPanel(), gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        add(createFinancesPanel(), gbc);
    }

    private CustomPartGeneratorOptionsPanel createCustomPartGeneratorOptionsPanel() {
        // Create Panel Components
        final JLabel lblSparePartTargetMultiplier = new JLabel(resources.getString("lblSparePartTargetMultiplier.text"));
        lblSparePartTargetMultiplier.setName("lblSparePartTargetMultiplier");
        lblSparePartTargetMultiplier.setToolTipText(resources.getString("lblSparePartTargetMultiplier.toolTipText"));
        setSpnCustPartGenTargetMultiplier(new JSpinner(new SpinnerNumberModel(0.1, 0.0, 100.0, 0.05)));
        getSpnCustPartGenTargetMultiplier().setToolTipText(resources.getString("lblSparePartTargetMultiplier.toolTipText"));
        getSpnCustPartGenTargetMultiplier().setName("spnCustPartGenTargetMultiplier");
        // Programmatically Assign Accessibility Labels
        lblSparePartTargetMultiplier.setLabelFor(getSpnCustPartGenTargetMultiplier());

        // Layout the UI
        final CustomPartGeneratorOptionsPanel panel = new CustomPartGeneratorOptionsPanel(getFrame(), getCampaign(), null);
        panel.setBorder(BorderFactory.createTitledBorder(resources.getString("pnlCustomPartGeneratorOptions.title")));
        panel.setName("pnlCustomPartGeneratorOptions");
        final GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblSparePartTargetMultiplier)
                    .addComponent(getSpnCustPartGenTargetMultiplier(), Alignment.LEADING))
        );

        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(lblSparePartTargetMultiplier)
                    .addComponent(getSpnCustPartGenTargetMultiplier()))
        );

        panel.setEnabled(
            getComboPartGenerationMethod()
                .getSelectedItem() != PartGenerationMethod.CUSTOM);
        return panel;
    }
    private JPanel createSparesPanel() {
        // Initialize Labels Used in ActionListeners
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

        getComboPartGenerationMethod().addActionListener(evt -> {
            final boolean enablePartPayment = getComboPartGenerationMethod().isEnabled() && getComboPartGenerationMethod().getSelectedItem() != PartGenerationMethod.DISABLED;
            getChkPayForParts().setEnabled(enablePartPayment);
            getCustomPartGeneratorOptionsPanel().setEnabled(getComboPartGenerationMethod().getSelectedItem() == PartGenerationMethod.CUSTOM);
        });
        setCustomPartGeneratorOptionsPanel(createCustomPartGeneratorOptionsPanel());

        final JLabel lblStartingArmourWeight = new JLabel(resources.getString("lblStartingArmourWeight.text"));
        lblStartingArmourWeight.setToolTipText(resources.getString("lblStartingArmourWeight.toolTipText"));
        lblStartingArmourWeight.setName("lblStartingArmourWeight");

        setSpnStartingArmourWeight(new JSpinner(new SpinnerNumberModel(0, 0, 500, 1)));
        getSpnStartingArmourWeight().setToolTipText(resources.getString("lblStartingArmourWeight.toolTipText"));
        getSpnStartingArmourWeight().setName("spnStartingArmourWeight");
        getSpnStartingArmourWeight().addChangeListener(evt -> {
            final boolean enabled = getSpnStartingArmourWeight().isEnabled() && ((Integer) getSpnStartingArmourWeight().getValue()) > 0;
            getChkPayForArmour().setEnabled(enabled);
        });

        setChkGenerateSpareAmmunition(new JCheckBox(resources.getString("chkGenerateSpareAmmunition.text")));
        getChkGenerateSpareAmmunition().setToolTipText(resources.getString("chkGenerateSpareAmmunition.toolTipText"));
        getChkGenerateSpareAmmunition().setName("chkGenerateSpareAmmunition");
        getChkGenerateSpareAmmunition().addActionListener(evt -> {
            final boolean selected = getChkGenerateSpareAmmunition().isSelected();
            lblNumberReloadsPerWeapon.setEnabled(selected);
            getSpnNumberReloadsPerWeapon().setEnabled(selected);
            getChkGenerateFractionalMachineGunAmmunition().setEnabled(selected);
            getChkPayForAmmunition().setEnabled(selected);
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
//        getChkGenerateSpareAmmunition().doClick();

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
                            .addComponent(getCustomPartGeneratorOptionsPanel(), Alignment.LEADING))
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
                        .addGroup(layout.createSequentialGroup())
                                .addComponent(getCustomPartGeneratorOptionsPanel())
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
        financialDebitsPanel.setEnabled(true);
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

        setChkPayForParts(new JCheckBox(resources.getString("chkPayForParts.text")));
        getChkPayForParts().setToolTipText(resources.getString("chkPayForParts.toolTipText"));
        getChkPayForParts().setName("chkPayForParts");

        setChkPayForArmour(new JCheckBox(resources.getString("chkPayForArmour.text")));
        getChkPayForArmour().setToolTipText(resources.getString("chkPayForArmour.toolTipText"));
        getChkPayForArmour().setName("chkPayForArmour");

        setChkPayForAmmunition(new JCheckBox(resources.getString("chkPayForAmmunition.text")));
        getChkPayForAmmunition().setToolTipText(resources.getString("chkPayForAmmunition.toolTipText"));
        getChkPayForAmmunition().setName("chkPayForAmmunition");

        getChkPayForParts().setEnabled(true);
        getChkPayForArmour().setEnabled(true);
        getChkPayForAmmunition().setEnabled(true);

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
     * Sets the options for this panel to the default for the selected CompanyGenerationMethod
     */
    public void setOptions(final PartGenerationMethod partGenerationMethod) {
        setOptions(new InventoryGenerationOptions(partGenerationMethod));
    }

    /**
     * Sets the options for this panel based on the provided CompanyGenerationOptions
     * @param options the CompanyGenerationOptions to use
     */
    public void setOptions(final InventoryGenerationOptions options) {
        // Spares
        getComboPartGenerationMethod().setSelectedItem(options.getPartGenerationMethod());
        getSpnStartingArmourWeight().setValue(options.getTargetArmourWeight());
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
    public InventoryGenerationOptions createOptionsFromPanel() {
        final InventoryGenerationOptions options = new InventoryGenerationOptions(getComboPartGenerationMethod().getSelectedItem());

    // Custom Part Generator
//        options.getCustomPartGeneratorOptions().setSparePartTargetMultiplier((Double) getSpnCustPartGenTargetMultiplier().getValue());

    // Spares
        options.setCustomPartGeneratorOptions(getCustomPartGeneratorOptionsPanel().createOptionsFromPanel());
        options.setTargetArmourWeight((Integer) getSpnStartingArmourWeight().getValue());
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

}
