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
package mekhq.campaign.universe.companyGeneration;

import megamek.Version;
import megamek.common.EntityWeightClass;
import megamek.common.annotations.Nullable;
import mekhq.MHQConstants;
import mekhq.campaign.RandomOriginOptions;
import mekhq.campaign.personnel.enums.PersonnelRole;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.Factions;
import mekhq.campaign.universe.enums.*;
import mekhq.utilities.MHQXMLUtility;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Justin "Windchild" Bowen
 */
public class SparePartsGenerationOptions {
    //region Variable Declarations

    // Spares
    private PartGenerationMethod partGenerationMethod;
    private int startingArmourWeight;
    private boolean generateSpareAmmunition;
    private int numberReloadsPerWeapon;
    private boolean generateFractionalMachineGunAmmunition;

    // Finances
    private boolean payForParts;
    private boolean payForArmour;
    private boolean payForAmmunition;

    //endregion Variable Declarations

    //region Constructors
    public SparePartsGenerationOptions() {

        // Spares
        setPartGenerationMethod(PartGenerationMethod.WINDCHILD);
        setStartingArmourWeight(60);
        setGenerateSpareAmmunition(true);
        setNumberReloadsPerWeapon(4);
        setGenerateFractionalMachineGunAmmunition(true);

        // Finances
        setPayForParts(true);
        setPayForArmour(true);
        setPayForAmmunition(true);
    }
    //endregion Constructors

    //region Getters/Setters
    //region Spares
    public PartGenerationMethod getPartGenerationMethod() {
        return partGenerationMethod;
    }

    public void setPartGenerationMethod(final PartGenerationMethod partGenerationMethod) {
        this.partGenerationMethod = partGenerationMethod;
    }

    public int getStartingArmourWeight() {
        return startingArmourWeight;
    }

    public void setStartingArmourWeight(final int startingArmourWeight) {
        this.startingArmourWeight = startingArmourWeight;
    }

    public boolean isGenerateSpareAmmunition() {
        return generateSpareAmmunition;
    }

    public void setGenerateSpareAmmunition(final boolean generateSpareAmmunition) {
        this.generateSpareAmmunition = generateSpareAmmunition;
    }

    public int getNumberReloadsPerWeapon() {
        return numberReloadsPerWeapon;
    }

    public void setNumberReloadsPerWeapon(final int numberReloadsPerWeapon) {
        this.numberReloadsPerWeapon = numberReloadsPerWeapon;
    }

    public boolean isGenerateFractionalMachineGunAmmunition() {
        return generateFractionalMachineGunAmmunition;
    }

    public void setGenerateFractionalMachineGunAmmunition(final boolean generateFractionalMachineGunAmmunition) {
        this.generateFractionalMachineGunAmmunition = generateFractionalMachineGunAmmunition;
    }
    //endregion Spares

    //region Finances
    public boolean isPayForParts() {
        return payForParts;
    }

    public void setPayForParts(final boolean payForParts) {
        this.payForParts = payForParts;
    }

    public boolean isPayForArmour() {
        return payForArmour;
    }

    public void setPayForArmour(final boolean payForArmour) {
        this.payForArmour = payForArmour;
    }

    public boolean isPayForAmmunition() {
        return payForAmmunition;
    }

    public void setPayForAmmunition(final boolean payForAmmunition) {
        this.payForAmmunition = payForAmmunition;
    }
    //endregion Finances
    //endregion Getters/Setters

    //region File IO
    /**
     * Writes these options to an XML file
     * @param file the file to write to, or null to not write to a file
     */
    public void writeToFile(@Nullable File file) {
        if (file == null) {
            return;
        }
        String path = file.getPath();
        if (!path.endsWith(".xml")) {
            path += ".xml";
            file = new File(path);
        }

        try (OutputStream fos = new FileOutputStream(file);
             OutputStream bos = new BufferedOutputStream(fos);
             OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(osw)) {
            // Then save it out to that file.
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writeToXML(pw, 0, MHQConstants.VERSION);
        } catch (Exception ex) {
            LogManager.getLogger().error("", ex);
        }
    }

    /**
     * @param pw the print writer to write to
     * @param indent the indent level to write at
     * @param version the version these options were written to file in. This may be null, in which
     *                case they are being written to file as a part of a larger save than just these
     *                options (e.g. saved as part of Campaign or CampaignOptions)
     */
    public void writeToXML(final PrintWriter pw, int indent, final @Nullable Version version) {
        if (version == null) {
            MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "companyGenerationOptions");
        } else {
            MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "companyGenerationOptions", "version", version);
        }

        // Spares
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "partGenerationMethod", getPartGenerationMethod().name());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "startingArmourWeight", getStartingArmourWeight());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "generateSpareAmmunition", isGenerateSpareAmmunition());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "numberReloadsPerWeapon", getNumberReloadsPerWeapon());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "generateFractionalMachineGunAmmunition", isGenerateFractionalMachineGunAmmunition());

        // Finances
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "payForParts", isPayForParts());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "payForArmour", isPayForArmour());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "payForAmmunition", isPayForAmmunition());

    }

    /**
     * @param file the XML file to parse the company generation options from. This should not be null,
     *             but null values are handled nicely.
     * @return the parsed CompanyGenerationOptions, or the default Windchild options if there is an
     * issue parsing the file.
     */
    public static SparePartsGenerationOptions parseFromXML(final @Nullable File file) {
        if (file == null) {
            LogManager.getLogger().error("Received a null file, returning the default Windchild options");
            return new SparePartsGenerationOptions();
        }
        final Element element;

        // Open up the file.
        try (InputStream is = new FileInputStream(file)) {
            element = MHQXMLUtility.newSafeDocumentBuilder().parse(is).getDocumentElement();
        } catch (Exception ex) {
            LogManager.getLogger().error("Failed to open file, returning the default Windchild options", ex);
            return new SparePartsGenerationOptions();
        }
        element.normalize();

        final Version version = new Version(element.getAttribute("version"));
        final SparePartsGenerationOptions options = parseFromXML(element.getChildNodes(), version);
        if (options == null) {
            LogManager.getLogger().error("Failed to parse file, returning the default Windchild options");
            return new SparePartsGenerationOptions();
        } else {
            return options;
        }
    }

    /**
     * @param nl the node list to parse the options from
     * @param version the Version of the XML to parse from
     * @return the parsed company generation options, or null if the parsing fails
     */
    public static @Nullable SparePartsGenerationOptions parseFromXML(final NodeList nl,
                                                                     final Version version) {
        if (MHQConstants.VERSION.isLowerThan(version)) {
            LogManager.getLogger().error(String.format(
                    "Cannot parse Company Generation Options from %s in older version %s.",
                    version.toString(), MHQConstants.VERSION));
            return null;
        }

        final SparePartsGenerationOptions options = new SparePartsGenerationOptions();
        try {
            for (int x = 0; x < nl.getLength(); x++) {
                final Node wn = nl.item(x);
                switch (wn.getNodeName()) {
                    //region Spares
                    case "partGenerationMethod":
                        options.setPartGenerationMethod(PartGenerationMethod.valueOf(wn.getTextContent().trim()));
                        break;
                    case "startingArmourWeight":
                        options.setStartingArmourWeight(Integer.parseInt(wn.getTextContent().trim()));
                        break;
                    case "generateSpareAmmunition":
                        options.setGenerateSpareAmmunition(Boolean.parseBoolean(wn.getTextContent().trim()));
                        break;
                    case "numberReloadsPerWeapon":
                        options.setNumberReloadsPerWeapon(Integer.parseInt(wn.getTextContent().trim()));
                        break;
                    case "generateFractionalMachineGunAmmunition":
                        options.setGenerateFractionalMachineGunAmmunition(Boolean.parseBoolean(wn.getTextContent().trim()));
                        break;
                    //endregion Spares
                //region Finances
                    case "payForParts":
                        options.setPayForParts(Boolean.parseBoolean(wn.getTextContent().trim()));
                        break;
                    case "payForArmour":
                        options.setPayForArmour(Boolean.parseBoolean(wn.getTextContent().trim()));
                        break;
                    case "payForAmmunition":
                        options.setPayForAmmunition(Boolean.parseBoolean(wn.getTextContent().trim()));
                        break;
                    //endregion Finances
                    default:
                        break;
                }
            }
        } catch (Exception ex) {
            LogManager.getLogger().error("", ex);
            return null;
        }

        return options;
    }
    //endregion File IO
}
