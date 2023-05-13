package mekhq.campaign.parts;

import megamek.common.AmmoType;
import mekhq.campaign.Campaign;
import mekhq.campaign.Warehouse;
import mekhq.campaign.finances.Money;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.work.IAcquisitionWork;

import java.util.Objects;

public class PartsGenerationResult extends PartInUse {

    private boolean shouldBeAcquired;
    public PartsGenerationResult(Part part, Campaign campaign) {
        super(part);
        campaign.updatePartInUse(this);
//        super(campaign.getWarehouse().getParts(part.getName()).stream().reduce(part, (p,acc) -> acc.));
        if (part instanceof Armor){
            setPlannedCount(((Armor) part).getAmount());
        } else {
            setPlannedCount(part.getQuantity());
        }
        setShouldBeAcquired(true);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((null == obj) || (getClass() != obj.getClass() && !obj.getClass().isInstance(this))) {
            return false;
        }
        final PartInUse other = (PartInUse) obj;
        return Objects.equals(getDescription(), other.getDescription());
    }

    public boolean isShouldBeAcquired() {
        return shouldBeAcquired;
    }

    public void setShouldBeAcquired(boolean shouldBeAcquired) {
        this.shouldBeAcquired = shouldBeAcquired;
    }
}
