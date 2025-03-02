/*
 * ProtomechArmor.java
 *
 * Copyright (c) 2009 Jay Lawson (jaylawson39 at yahoo.com). All rights reserved.
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

package mekhq.campaign.parts;

import java.util.Objects;

import megamek.common.EquipmentType;
import megamek.common.Protomech;
import megamek.common.TechAdvancement;
import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Money;
import mekhq.campaign.work.IAcquisitionWork;

/**
 *
 * @author Jay Lawson (jaylawson39 at yahoo.com)
 */
public class ProtomekArmor extends Armor implements IAcquisitionWork {
    public ProtomekArmor() {
        this(0, EquipmentType.T_ARMOR_STANDARD, 0, -1, false, null);
    }

    public ProtomekArmor(int tonnage, int type, int points, int loc, boolean clan, Campaign c) {
        // Amount is used for armor quantity, not tonnage
        super(tonnage, type, points, loc, false, clan, c);
        this.name = "Protomech Armor";
    }

    @Override
    public ProtomekArmor clone() {
        ProtomekArmor clone = new ProtomekArmor(0, type, 0, amount, clan, campaign);
        clone.copyBaseData(this);
        return clone;
    }

    @Override
    public double getTonnage() {
        return EquipmentType.getProtomechArmorWeightPerPoint(type) * amount;
    }

    @Override
    public Money getActualValue() {
        return adjustCostsForCampaignOptions(
                Money.of(amount * EquipmentType.getProtomechArmorCostPerPoint(type)));
    }

    @Override
    public double getTonnageNeeded() {
        return amountNeeded / EquipmentType.getProtomechArmorWeightPerPoint(type);
    }

    @Override
    public Money getValueNeeded() {
        return adjustCostsForCampaignOptions(
                Money.of(amountNeeded * EquipmentType.getProtomechArmorCostPerPoint(type)));
    }

    @Override
    public Money getStickerPrice() {
        // always in 5-ton increments
        return Money.of(5.0 / EquipmentType.getProtomechArmorWeightPerPoint(type) * getArmorPointsPerTon()
                * EquipmentType.getProtomechArmorCostPerPoint(type));
    }

    @Override
    public Money getBuyCost() {
        return getActualValue();
    }

    @Override
    public boolean isSamePartType(Part part) {
        return (getClass() == part.getClass())
                && getType() == ((ProtomekArmor) part).getType()
                && isClanTechBase() == part.isClanTechBase()
                && Objects.equals(getRefitUnit(), part.getRefitUnit());
    }

    @Override
    protected boolean isClanTechBase() {
        return clan;
    }

    @Override
    public double getArmorWeight(int points) {
        return points * 50/1000.0;
    }

    @Override
    public IAcquisitionWork getAcquisitionWork() {
        return new ProtomekArmor(0, type, (int) Math.round(5.0 * getArmorPointsPerTon()),
                -1, clan, campaign);
    }

    @Override
    public int getDifficulty() {
        return -2;
    }

    @Override
    public double getArmorPointsPerTon() {
        return 1.0 / EquipmentType.getProtomechArmorWeightPerPoint(type);
    }

    @Override
    public Part getNewPart() {
        return new ProtomekArmor(0, type, (int) Math.round(5 * getArmorPointsPerTon()),
                -1, clan, campaign);
    }

    @Override
    public int getAmountAvailable() {
        ProtomekArmor a = (ProtomekArmor) campaign.getWarehouse().findSparePart(part ->
                (part instanceof ProtomekArmor)
                        && part.isPresent()
                        && !part.isReservedForRefit()
                        && isClanTechBase() == part.isClanTechBase()
                        && (getType() == ((ProtomekArmor) part).getType()));

        return a != null ? a.getAmount() : 0;
    }

    @Override
    public void changeAmountAvailable(int amount) {
        ProtomekArmor a = (ProtomekArmor) campaign.getWarehouse().findSparePart(part ->
                isSamePartType(part) && part.isPresent());

        if (null != a) {
            a.setAmount(a.getAmount() + amount);
            if (a.getAmount() <= 0) {
                campaign.getWarehouse().removePart(a);
            }
        } else if (amount > 0) {
            campaign.getQuartermaster().addPart(new ProtomekArmor(getUnitTonnage(), type, amount, -1, isClanTechBase(), campaign), 0);
        }
    }

    @Override
    public TechAdvancement getTechAdvancement() {
        if (type != EquipmentType.T_ARMOR_STANDARD) {
            final EquipmentType eq = EquipmentType.get(EquipmentType.getArmorTypeName(type, clan));
            if (null != eq) {
                return eq.getTechAdvancement();
            }
        }
        // Standard Protomech armor is not the same as Standard armor, but does not have an associated
        // type entry so we can just use the base protomech advancement
        return Protomech.TA_STANDARD_PROTOMECH;
    }
}
