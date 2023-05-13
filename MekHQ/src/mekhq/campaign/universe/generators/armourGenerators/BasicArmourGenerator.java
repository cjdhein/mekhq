package mekhq.campaign.universe.generators.armourGenerators;

import mekhq.campaign.finances.Money;
import mekhq.campaign.parts.Armor;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.work.WorkTime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BasicArmourGenerator {

  /**
   * @param units the list of units to generate spare armour based on
   * @param targetArmourWeight the total armour amount to generate
   * @return the generated armour
   */
  public static List<Armor> generateFromUnits(final List<Unit> units, int targetArmourWeight) {
    if (targetArmourWeight <= 0) {
      return new ArrayList<>();
    }
    final List<Armor> unitAssignedArmour = units.stream()
        .flatMap(unit -> unit.getParts().stream())
        .filter(part -> part instanceof Armor)
        .map(part -> (Armor) part)
        .collect(Collectors.toList());
    return generate(unitAssignedArmour, targetArmourWeight);
  }

  /**
   * @param sourceArmour the list of Armor to generate
   * @param targetArmourWeight the total armour amount to generate
   * @return the generated armour
   */
  public static List<Armor> generate(final List<Armor> sourceArmour, int targetArmourWeight) {
    final List<Armor> armour = mergeIdenticalArmour(sourceArmour);
    final double armourTonnageMultiplier = targetArmourWeight
        / armour.stream().mapToDouble(Armor::getTonnage).sum();
    armour.forEach(a -> a.setAmount(Math.toIntExact(Math.round(a.getAmount() * armourTonnageMultiplier))));
    return armour;
  } 

  /**
   * This clones and merges armour determined by the custom check below together
   * @param unmergedArmour the unmerged list of armour, which may be assigned to a unit
   * @return the merged list of armour
   */
  private static List<Armor> mergeIdenticalArmour(final List<Armor> unmergedArmour) {
    final List<Armor> mergedArmour = new ArrayList<>();
    unmergedArmour.forEach(armour -> {
      boolean unmerged = true;
      for (final Armor a : mergedArmour) {
        if (areSameArmour(a, armour)) {
          a.addAmount(armour.getAmount());
          unmerged = false;
          break;
        }
      }

      if (unmerged) {
        final Armor a = armour.clone();
        a.setMode(WorkTime.NORMAL);
        a.setOmniPodded(false);
        mergedArmour.add(a);
      }
    });
    return mergedArmour;
  }

  /**
   * This is a custom equals comparison utilized by this class to determine if two Armour Parts
   * are the same
   * @param a1 the first Armour part
   * @param a2 the second Armour part
   * @return whether this class considers both types of Armour to be the same. This DIFFERS
   * from Armor::equals
   */
  private static boolean areSameArmour(final Armor a1, final Armor a2) {
    return (a1.getClass() == a2.getClass())
        && a1.isSameType(a2)
        && (a1.isClan() == a2.isClan())
        && (a1.getQuality() == a2.getQuality())
        && (a1.getHits() == a2.getHits())
        && (a1.getSkillMin() == a2.getSkillMin());
  }

  /**
   * @param armours the list of different armours to get the cost for
   * @return the cost of the armour, or zero if you aren't paying for armour
   */
  public static Money calculateArmourCosts(final List<Armor> armours) {
    Money armourCosts = Money.zero();
    for (final Armor armour : armours) {
      armourCosts = armourCosts.plus(armour.getActualValue());
    }
    return armourCosts;
  }
}
