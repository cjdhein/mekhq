package mekhq.campaign.universe.generators.ammunitionGenerators;

import megamek.common.AmmoType;
import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Money;
import mekhq.campaign.parts.AmmoStorage;
import mekhq.campaign.parts.Part;
import mekhq.campaign.parts.PartInUse;
import mekhq.campaign.parts.equipment.AmmoBin;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.universe.inventoryGenerator.InventoryGeneratorOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BasicAmmunitionGenerator {

  /**
   * @param campaign the campaign to generate ammunition for
   * @param source a List of either Units or AmmoBin
   * @param options a set of InventoryGeneratorOptions
   * @return the generated ammunition
   */
  public static List<AmmoStorage> generateAmmunition(final Campaign campaign,
                                                     final List<?> source,
                                                     final InventoryGeneratorOptions options) {
    return generateAmmunition(campaign, source,
        options.isGenerateSpareAmmunition(),
        options.isGenerateFractionalMachineGunAmmunition(),
        options.getNumberReloadsPerWeapon());
  }

  /**
   * @param campaign the campaign to generate ammunition for
   * @param source a List of either Units or AmmoBin
   * @param generateSpareAmmunition whether to even generate ammo
   * @param generateFractionalMachineGunAmmunition should fractional MG ammo be generated
   * @param numberReloadsPerWeapon number of reloads worth of ammo to generate
   * @return the generated ammunition
   */
  public static List<AmmoStorage> generateAmmunition(final Campaign campaign,
                                                     final List<?> source,
                                                     boolean generateSpareAmmunition,
                                                     boolean generateFractionalMachineGunAmmunition,
                                                     int numberReloadsPerWeapon) {
    if (!generateSpareAmmunition || (numberReloadsPerWeapon <= 0)
        && !generateFractionalMachineGunAmmunition) {
      return new ArrayList<>();
    }
    final List<AmmoBin> ammoBins = new ArrayList<>();
    source.forEach(elem -> {
      if (elem instanceof AmmoBin) {
        ammoBins.add((AmmoBin) elem);
      } else if (elem instanceof Unit) {
        ((Unit) elem).getParts().stream().filter(part -> part instanceof AmmoBin)
            .forEach(ab -> ammoBins.add((AmmoBin) ab));
      }
    });
    return generate(campaign, ammoBins, generateFractionalMachineGunAmmunition, numberReloadsPerWeapon);
  }

  private static List<AmmoStorage> generate(final Campaign campaign,
                                            final List<AmmoBin> ammoBins,
                                            boolean generateFractionalMachineGunAmmunition,
                                            int numberReloadsPerWeapon) {
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
