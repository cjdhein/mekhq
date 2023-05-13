package mekhq.campaign.parts;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import megamek.common.AmmoType;
import megamek.common.EquipmentType;
import megamek.common.MiscType;
import mekhq.campaign.finances.Money;
import mekhq.campaign.parts.equipment.EquipmentPart;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.work.IAcquisitionWork;

public class PartInUse {
    private String description;
    private IAcquisitionWork partToBuy;
    private int useCount;
    private int storeCount;
    private double tonnagePerItem;
    private int transferCount;
    private int plannedCount;
    private Money cost = Money.zero();

    private void appendDetails(StringBuilder sb, Part part) {
        String details = part.getDetails(false);
        if (!details.isEmpty()) {
            sb.append(" (").append(details).append(")");
        }
    }

    public static boolean isValidForPartInUse(Part p) {
        // SI isn't a proper "part"
        boolean isSI = p instanceof StructuralIntegrity;

        // Skip out on "not armor" (as in 0 point armor on men or field guns)
        boolean isInvalidArmor = ((p instanceof Armor) && (((Armor) p).getType() == EquipmentType.T_ARMOR_UNKNOWN));

        // Makes no sense buying those separately from the chasis
        boolean isEquipmentPart = ((p instanceof EquipmentPart)
            && (((EquipmentPart) p).getType() != null)
            && (((EquipmentPart) p).getType().hasFlag(MiscType.F_CHASSIS_MODIFICATION)));


        return !(isSI || isInvalidArmor || isEquipmentPart || (p.getAcquisitionWork() == null));
    }

    public PartInUse(Part part) {
        // Replace a "missing" part with a corresponding "new" one.
        if (part instanceof MissingPart) {
            part = ((MissingPart) part).getNewPart();
        }
        StringBuilder sb = new StringBuilder(part.getName());
        Unit u = part.getUnit();
        if (!(part instanceof MissingBattleArmorSuit)) {
            part.setUnit(null);
        }
        if (!(part instanceof Armor) && !(part instanceof AmmoStorage)) {
            appendDetails(sb, part);
        }
        part.setUnit(u);
        this.description = sb.toString();
        setPartToBuy(part.getAcquisitionWork());
        this.tonnagePerItem = part.getTonnage();
        // AmmoBin are special: They aren't buyable (yet?), but instead buy you the ammo inside
        // We redo the description based on that
        if (partToBuy instanceof AmmoStorage) {
            sb.setLength(0);
            sb.append(((AmmoStorage) partToBuy).getName());
            appendDetails(sb, (Part) ((AmmoStorage) partToBuy).getAcquisitionWork());
            this.description = sb.toString();
            AmmoType ammoType = (AmmoType) ((AmmoStorage) partToBuy).getType();
            if (ammoType.getKgPerShot() > 0) {
                this.tonnagePerItem = ammoType.getKgPerShot() / 1000.0;
            } else {
                this.tonnagePerItem = 1.0 / ammoType.getShots();
            }
        }
        if (part instanceof Armor) {
            // Armor needs different tonnage values
            this.tonnagePerItem = 1.0 / ((Armor) part).getArmorPointsPerTon();
        }
        if (null != partToBuy) {
            this.cost = partToBuy.getBuyCost();
        }
    }

    public PartInUse(String description, IAcquisitionWork partToBuy, Money cost) {
        this.description = Objects.requireNonNull(description);
        setPartToBuy(Objects.requireNonNull(partToBuy));
        this.cost = cost;
    }

    public PartInUse(String description, IAcquisitionWork partToBuy) {
        this(description, partToBuy, partToBuy.getBuyCost());
    }

    public String getDescription() {
        return description;
    }

    public IAcquisitionWork getPartToBuy() {
        return partToBuy;
    }

    public void setPartToBuy(IAcquisitionWork partToBuy) {
        this.partToBuy = partToBuy;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

    public void incUseCount() {
        ++ useCount;
    }

    public int getStoreCount() {
        return storeCount;
    }

    public double getStoreTonnage() {
        return storeCount * tonnagePerItem;
    }

    public void setStoreCount(int storeCount) {
        this.storeCount = storeCount;
    }

    public void incStoreCount() {
        ++ storeCount;
    }

    public int getTransferCount() {
        return transferCount;
    }

    public void incTransferCount() {
        ++ transferCount;
    }

    public void setTransferCount(int transferCount) {
        this.transferCount = transferCount;
    }

    public int getPlannedCount() {
        return plannedCount;
    }

    public void setPlannedCount(int plannedCount) {
        this.plannedCount = plannedCount;
    }

    public void incPlannedCount() {
        ++ plannedCount;
    }

    public Money getCost() {
        return cost;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(description);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((null == obj) || (getClass() != obj.getClass())) {
            return false;
        }
        final PartInUse other = (PartInUse) obj;
        return Objects.equals(description, other.description);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getPartToBuy().getAcquisitionName());
        sb.append(", q: ");
        sb.append(this.getUseCount());
        if (null != this.getPartToBuy().getUnit()) {
            sb.append(", mounted: ");
            sb.append(this.getPartToBuy().getUnit());
        }
        return sb.toString();
    }
}
