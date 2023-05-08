package mekhq.campaign.universe.generators.ammunitionGenerators;

import megamek.common.AmmoType;
import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Money;
import mekhq.campaign.parts.AmmoStorage;
import mekhq.campaign.parts.equipment.AmmoBin;
import mekhq.campaign.unit.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BasicAmmunitionGenerator {

  /**
   * @param campaign the campaign to generate ammunition for
   * @param units the list of units to generate ammunition for
   * @return the generated ammunition
   */
  public static List<AmmoStorage> generateAmmunition(final Campaign campaign,
                                                     final List<Unit> units,
                                                     boolean generateSpareAmmunition,
                                                     boolean generateFractionalMachineGunAmmunition,
                                                     int numberReloadsPerWeapon) {
    if (!generateSpareAmmunition || (numberReloadsPerWeapon <= 0)
        && !generateFractionalMachineGunAmmunition) {
      return new ArrayList<>();
    }

    final List<AmmoBin> ammoBins = units.stream()
        .flatMap(unit -> unit.getParts().stream())
        .filter(part -> part instanceof AmmoBin)
        .map(part -> (AmmoBin) part)
        .collect(Collectors.toList());

    final List<AmmoStorage> ammunition = new ArrayList<>();
    final boolean generateReloads = numberReloadsPerWeapon > 0;
    ammoBins.forEach(ammoBin -> {
      if (generateFractionalMachineGunAmmunition && ammoBinIsMachineGun(ammoBin)) {
        ammunition.add(new AmmoStorage(0, ammoBin.getType(), 50, campaign));
      } else if (generateReloads) {
        ammunition.add(new AmmoStorage(0, ammoBin.getType(),
            ammoBin.getFullShots() * numberReloadsPerWeapon, campaign));
      }
    });

    return ammunition;
  }

  /**
   * @param ammoBin the ammo bin to check
   * @return whether the ammo bin's ammo type is a machine gun type
   */
  private static boolean ammoBinIsMachineGun(final AmmoBin ammoBin) {
    switch (ammoBin.getType().getAmmoType()) {
      case AmmoType.T_MG:
      case AmmoType.T_MG_HEAVY:
      case AmmoType.T_MG_LIGHT:
        return true;
      default:
        return false;
    }
  }

  /**
   * @param ammunition the list of ammunition to get the cost for
   * @return the cost of the ammunition, or zero if you aren't paying for ammunition
   */
  public static Money calculateAmmunitionCosts(final List<AmmoStorage> ammunition) {
    Money ammunitionCosts = Money.zero();
    for (final AmmoStorage ammoStorage : ammunition) {
      ammunitionCosts = ammunitionCosts.plus(ammoStorage.getActualValue());
    }

    return ammunitionCosts;
  }
}
