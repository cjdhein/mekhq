package mekhq.campaign.universe.inventoryGenerator;

import megamek.common.EquipmentType;
import megamek.common.Mech;
import mekhq.TestUtilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.CampaignOptions;
import mekhq.campaign.Warehouse;
import mekhq.campaign.parts.*;
import mekhq.campaign.parts.equipment.AmmoBin;
import mekhq.campaign.personnel.ranks.Ranks;
import mekhq.campaign.unit.UnitTestUtilities;
import mekhq.campaign.universe.Systems;
import mekhq.campaign.universe.enums.PartGenerationMethod;
import mekhq.campaign.universe.generators.partGenerators.MishraPartGenerator;
import mekhq.campaign.universe.generators.partGenerators.MultiplePartGenerator;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InventoryGeneratorTest {

  private InventoryGeneratorOptions options;
  private Campaign campaign;
  private CampaignOptions mockCampaignOptions;
  private Warehouse warehouse;

  @BeforeAll
  static void beforeAll() {
    EquipmentType.initializeTypes();
    Ranks.initializeRankSystems();
    try {
      Systems.setInstance(Systems.loadDefault());
    } catch (Exception ex) {
      LogManager.getLogger().error("", ex);
    }
  }

  @BeforeEach
  void setUp() {
    options = new InventoryGeneratorOptions(PartGenerationMethod.SINGLE);
    campaign = TestUtilities.getTestCampaign();
    campaign.addNewUnit(UnitTestUtilities.getWaspLAMMk1(), false, 0);
    campaign.addNewUnit(UnitTestUtilities.getHeavyTrackedApcMg(), false, 0);
  }

  List<PartsGenerationResult> runGeneration(InventoryGeneratorOptions options) {
    return new InventoryGenerator(options, campaign).generateInventory(false);
  }

  List<PartInUse> getPartsInUseForCurrentOptions() {
    List<Part> filteredParts = campaign.getWarehouse().getParts().stream().filter(getPIUFilter()).collect(Collectors.toList());
    return new ArrayList<>(campaign.getPartsInUse(filteredParts));
  }
  
  private Predicate<Part> getPIUFilter() {
    Predicate<Part> armourFilter = p -> ((options.getTargetArmourWeight() > 0) || !(p instanceof Armor));
    Predicate<Part> ammoFilter = p -> (options.isGenerateSpareAmmunition() || (!(p instanceof AmmoBin) && !(p instanceof AmmoStorage)));
    Predicate<Part> mishraFilter = p -> p.getUnit().getEntity() instanceof Mech && !(p instanceof EnginePart);
    
    return armourFilter.and(ammoFilter)
        .and(options.getPartGenerationMethod() == PartGenerationMethod.MISHRA ? mishraFilter : p -> true);
  }

  boolean plannedCountsAreValid(List<PartsGenerationResult> pgrs, List<PartInUse> pius) {
    pius.sort(Comparator.comparing(o -> o.getPartToBuy().getAcquisitionName()));
    pgrs.sort(Comparator.comparing(o -> o.getPartToBuy().getAcquisitionName()));
    double multiplier;
    switch (options.getPartGenerationMethod()){
      case DOUBLE:
        multiplier = 2d;
      case TRIPLE:
        multiplier = 3d;
      case CUSTOM:
        multiplier = options.getCustomPartGeneratorOptions().getSparePartTargetMultiplier();
      case DISABLED:
      case WINDCHILD:
      case MISHRA:
      case SINGLE:
      default:
        multiplier = 1;
    }
    for (int i = 0; i < pgrs.size(); i++) {
      PartInUse piu = pius.get(i);
      PartsGenerationResult pgr = pgrs.get(i);
      int useCount = (int) Math.round(piu.getUseCount() * multiplier);
      if (useCount != pgr.getPlannedCount()) {
        StringBuilder sb = new StringBuilder();
        sb.append("PIU ").append(piu.toString()).append(" with use count ").append(piu.getUseCount())
                .append(" != PGR ").append(pgr.toString()).append(" with planned count ").append(pgr.getPlannedCount());
        System.out.println(sb.toString());
        return false;
      }
    }
    return true;
  }

  @Test
  void methodSingleGeneratorTest() {
    options.setPartGenerationMethod(PartGenerationMethod.SINGLE);
    options.setGenerateSpareAmmunition(false);
    options.setTargetArmourWeight(0);
    List<PartsGenerationResult> pgr = runGeneration(options);
    List<PartInUse> piu = getPartsInUseForCurrentOptions();
    assertEquals(piu.size(), pgr.size());
    assertTrue(plannedCountsAreValid(pgr, piu));
  }

  @Test
  void methodDoubleGeneratorTest() {
    options.setPartGenerationMethod(PartGenerationMethod.DOUBLE);
    options.setGenerateSpareAmmunition(false);
    options.setTargetArmourWeight(0);
    List<PartsGenerationResult> pgr = runGeneration(options);
    List<PartInUse> piu = getPartsInUseForCurrentOptions();
    assertEquals(piu.size(), pgr.size());
    plannedCountsAreValid(pgr, piu);
  }

  @Test
  void methodTripleGeneratorTest() {
    options.setPartGenerationMethod(PartGenerationMethod.TRIPLE);
    options.setGenerateSpareAmmunition(false);
    options.setTargetArmourWeight(0);
    List<PartsGenerationResult> pgr = runGeneration(options);
    List<PartInUse> piu = getPartsInUseForCurrentOptions();
    assertEquals(piu.size(), pgr.size());
    plannedCountsAreValid(pgr, piu);
  }

  @Test
  void methodMishraGeneratorTest() {
    options.setPartGenerationMethod(PartGenerationMethod.MISHRA);
    options.setGenerateSpareAmmunition(false);
    options.setTargetArmourWeight(0);
    List<PartsGenerationResult> pgr = runGeneration(options);
    List<PartInUse> piu = getPartsInUseForCurrentOptions();
    assertEquals(piu.size(), pgr.size());
    plannedCountsAreValid(pgr, piu);
  }
}