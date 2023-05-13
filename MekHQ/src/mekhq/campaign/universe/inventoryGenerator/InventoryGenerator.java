package mekhq.campaign.universe.inventoryGenerator;

import mekhq.campaign.Campaign;
import mekhq.campaign.parts.*;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.universe.enums.PartGenerationMethod;
import mekhq.campaign.universe.generators.ammunitionGenerators.BasicAmmunitionGenerator;
import mekhq.campaign.universe.generators.armourGenerators.BasicArmourGenerator;
import mekhq.campaign.universe.generators.partGenerators.AbstractPartGenerator;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cjdhein
 */
public class InventoryGenerator {
  //region Variable Declarations
  private Campaign campaign;
  private InventoryGeneratorOptions inventoryGeneratorOptions;
  private AbstractPartGenerator partGenerator;
  //endregion Variable Declarations

  //region Constructors
  public InventoryGenerator(InventoryGeneratorOptions options, Campaign campaign) {
    this.inventoryGeneratorOptions = options;
    this.campaign = campaign;
    this.partGenerator = options.getPartGenerationMethod().getGenerator(options.getCustomPartGeneratorOptions());
  }
  //endregion Constructors

  public List<PartsGenerationResult> generateInventory(boolean shouldNetWithCurrentStock) {
    final List<Unit> units = new ArrayList<>(campaign.getUnits());
    final List<Part> parts = generateParts(units);
    final List<Armor> armour = BasicArmourGenerator.generateFromUnits(units, inventoryGeneratorOptions.getTargetArmourWeight());
    final List<AmmoStorage> ammunition = BasicAmmunitionGenerator.generateAmmunition(campaign, units, inventoryGeneratorOptions);
    final List<PartsGenerationResult> partsGenerationResults = getPartsGenerationResults(parts, armour, ammunition);
    return netWithCurrentWarehouseQuantities(partsGenerationResults);
//    return partsGenerationResults;
  }

  public List<PartsGenerationResult> netWithCurrentWarehouseQuantities(List<PartsGenerationResult> results) {
    List<PartsGenerationResult> nettedResults = new ArrayList<>();
    Map<String, List<PartInUse>> currentStock = campaign.getPartsInUse().stream()
        .collect(Collectors.groupingBy(p -> p.getPartToBuy().getAcquisitionName()));
    Map<String, List<PartsGenerationResult>> resultsByPartName = results.stream()
        .collect(Collectors.groupingBy(p -> p.getPartToBuy().getAcquisitionName()));
    resultsByPartName.keySet().forEach(k -> {
      PartInUse current = currentStock.get(k).get(0);
      PartsGenerationResult target = resultsByPartName.get(k).get(0);
      target.setPlannedCount(target.getPlannedCount() - current.getStoreCount() - current.getTransferCount());
      if (target.getPlannedCount() > 0) {
        nettedResults.add(target);
      }
    });
    return nettedResults;
  }

  private List<Part> generateParts(List<Unit> units) {
    if (partGenerator.getMethod() != PartGenerationMethod.DISABLED) {
      List<Part> tmp = partGenerator.generate(units, false, false);
      return tmp;
    } else {
      return new ArrayList<>();
    }
  }
  private List<PartsGenerationResult> getPartsGenerationResults(List<Part> parts, List<Armor> armour, List<AmmoStorage> ammo) {
    List<Part> allParts = new ArrayList<>();
    allParts.addAll(parts.stream().filter(PartInUse::isValidForPartInUse).collect(Collectors.toList()));
    allParts.addAll(armour.stream().filter(PartInUse::isValidForPartInUse).collect(Collectors.toList()));
    allParts.addAll(ammo.stream().filter(PartInUse::isValidForPartInUse).collect(Collectors.toList()));
    allParts = mergeParts(allParts);

    List<PartsGenerationResult> generationResults = allParts.stream()
        .map(newPart -> new PartsGenerationResult(newPart, campaign))
        .collect(Collectors.toList());
    generationResults.forEach(p -> LogManager.getLogger().info(p.getDescription() + " -> " + p.getPlannedCount()));
    return generationResults;
  }

  List<Part> mergeParts(List<Part> parts) {
    return parts.stream()
        .collect(Collectors.groupingBy(p -> p.getAcquisitionWork().getAcquisitionName()))
        .values().stream().map(partList -> {
          Part p = partList.get(0);
          Integer qty = partList.stream().map(Part::getQuantity).reduce(0, Integer::sum);
          p.setQuantity(qty);
          return p;
        }).collect(Collectors.toList());
  }
}
