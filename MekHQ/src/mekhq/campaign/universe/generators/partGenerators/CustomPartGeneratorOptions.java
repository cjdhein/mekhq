package mekhq.campaign.universe.generators.partGenerators;

/**
 * @author cjdhein
 */
public class CustomPartGeneratorOptions {
  //region Variable Declarations
  private double sparePartTargetMultiplier;
  //endregion Variable Declarations

  //region Constructors
  public CustomPartGeneratorOptions() {
    setSparePartTargetMultiplier(1d);
  }
  //endregion Constructors

  //region Getters/Setters
  public double getSparePartTargetMultiplier() {
    return sparePartTargetMultiplier;
  }

  public void setSparePartTargetMultiplier(double sparePartTargetMultiplier) {
    this.sparePartTargetMultiplier = sparePartTargetMultiplier;
  }
  //endregion Getters/Setters
}
