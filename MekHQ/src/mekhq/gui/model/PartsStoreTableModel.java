package mekhq.gui.model;

import megamek.common.TargetRoll;
import megamek.common.annotations.Nullable;
import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Money;
import mekhq.campaign.parts.Part;
import mekhq.campaign.parts.PartInventory;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.work.IAcquisitionWork;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A table model for displaying parts - similar to the one in CampaignGUI, but not exactly
 */
public class PartsStoreTableModel extends PartsTableModel {
  protected String[] columnNames;
  protected ArrayList<PartProxy> data;
  private Campaign campaign;
  private Person logisticsPerson;

  public final static int COL_NAME = 0;
  public final static int COL_DETAIL = 1;
  public final static int COL_TECH_BASE = 2;
  public final static int COL_COST = 3;
  public final static int COL_TON = 4;
  public final static int COL_TARGET = 5;
  public final static int COL_SUPPLY = 6;
  public final static int COL_TRANSIT = 7;
  public final static int COL_QUEUE = 8;
  public final static int N_COL = 9;


  public Campaign getCampaign() {
    return campaign;
  }

  private void setCampaign(Campaign c) {
    this.campaign = c;
  }

  /**
   * Provides a lazy view to a {@link TargetRoll} for use in a UI (e.g. sorting in a table).
   */
  public static class TargetProxy implements Comparable<TargetProxy> {
    private TargetRoll target;
    private String details;
    private String description;

    /**
     * Creates a new proxy object for a {@link TargetRoll}.
     *
     * @param t The {@link TargetRoll} to be proxied. May be null.
     */
    public TargetProxy(@Nullable TargetRoll t) {
      target = t;
    }

    /**
     * Gets the target roll.
     *
     * @return The target roll.
     */
    public TargetRoll getTargetRoll() {
      return target;
    }

    /**
     * Gets a description of the target roll.
     *
     * @return A description of the target roll.
     */
    @Nullable
    public String getDescription() {
      if (null == target) {
        return null;
      }
      if (null == description) {
        description = target.getDesc();
      }
      return description;
    }

    /**
     * Gets a string representation of a {@link TargetRoll}.
     *
     * @return A string representation of a {@link TargetRoll}.
     */
    @Override
    public String toString() {
      if (null == target) {
        return "-";
      }

      if (null == details) {
        details = target.getValueAsString();
        if (target.getValue() != TargetRoll.IMPOSSIBLE &&
            target.getValue() != TargetRoll.AUTOMATIC_SUCCESS &&
            target.getValue() != TargetRoll.AUTOMATIC_FAIL) {
          details += "+";
        }
      }

      return details;
    }

    /**
     * Converts a {@link TargetRoll} into an integer for comparisons.
     *
     * @return An integer representation of the {@link TargetRoll}.
     */
    private int coerceTargetRoll() {
      int r = target.getValue();
      if (r == TargetRoll.IMPOSSIBLE) {
        return Integer.MAX_VALUE;
      } else if (r == TargetRoll.AUTOMATIC_FAIL) {
        return Integer.MAX_VALUE - 1;
      } else if (r == TargetRoll.AUTOMATIC_SUCCESS) {
        return Integer.MIN_VALUE;
      }
      return r;
    }

    /**
     * {@inheritDoc}
     *
     * @param o The {@link TargetProxy} to compare this instance to.
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(TargetProxy o) {
      return Integer.compare(coerceTargetRoll(), o.coerceTargetRoll());
    }
  }

  /**
   * Provides a container for a value formatted for display and the
   * value itself for sorting.
   */
  public static class FormattedValue<T extends Comparable<T>> implements Comparable<FormattedValue<T>> {
    private T value;
    private String formatted;

    /**
     * Creates a wrapper around a value and a
     * formatted string representing the value.
     */
    public FormattedValue(T v, String f) {
      value = v;
      formatted = f;
    }

    /**
     * Gets the wrapped value.
     *
     * @return The value.
     */
    public T getValue() {
      return value;
    }

    /**
     * Gets the formatted value.
     *
     * @return The formatted value.
     */
    @Override
    public String toString() {
      return formatted;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(FormattedValue<T> o) {
      if (null == o) {
        return -1;
      }
      return getValue().compareTo(o.getValue());
    }
  }

  public PartsStoreTableModel(Campaign c) {
    setCampaign(c);
    ArrayList<Part> inventory = campaign.getPartsStore().getInventory();
    data = new ArrayList<>(inventory.size());
    for (Part part : inventory) {
      data.add(new PartProxy(this, part));
    }
  }

  @Override
  public int getRowCount() {
    return data.size();
  }

  @Override
  public int getColumnCount() {
    return N_COL;
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case COL_NAME:
        return "Name";
      case COL_DETAIL:
        return "Detail";
      case COL_COST:
        return "Cost";
      case COL_TON:
        return "Ton";
      case COL_TECH_BASE:
        return "Tech";
      case COL_TARGET:
        return "Target";
      case COL_QUEUE:
        return "# Ordered";
      case COL_SUPPLY:
        return "# Supply";
      case COL_TRANSIT:
        return "# Transit";
      default:
        return "?";
    }
  }

  @Override
  public Object getValueAt(int row, int col) {
    PartProxy part;
    if (data.isEmpty()) {
      return "";
    } else {
      part = data.get(row);
    }
    if (col == COL_NAME) {
      return part.getName();
    }
    if (col == COL_DETAIL) {
      return part.getDetails();
    }
    if (col == COL_COST) {
      return part.getCost();
    }
    if (col == COL_TON) {
      return Math.round(part.getTonnage() * 100) / 100.0;
    }
    if (col == COL_TECH_BASE) {
      return part.getTechBase();
    }
    if (col == COL_TARGET) {
      return part.getTarget();
    }
    if (col == COL_SUPPLY) {
      return part.getSupply();
    }
    if (col == COL_TRANSIT) {
      return part.getTransit();
    }
    if (col == COL_QUEUE) {
      return part.getOrdered();
    }
    return "?";
  }

  @Override
  public boolean isCellEditable(int row, int col) {
    return false;
  }

  @Override
  public Class<?> getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

  public PartProxy getPartProxyAt(int row) {
    return data.get(row);
  }

  public Part getPartAt(int row) {
    return data.get(row).getPart();
  }

  public Person getLogisticsPerson() {
    if (null == logisticsPerson) {
      logisticsPerson = campaign.getLogisticsPerson();
    }
    return logisticsPerson;
  }

  public Part[] getPartstAt(int[] rows) {
    Part[] parts = new Part[rows.length];
    for (int i = 0; i < rows.length; i++) {
      int row = rows[i];
      parts[i] = data.get(row).getPart();
    }
    return parts;
  }

  public int getColumnWidth(int c) {
    switch (c) {
      case COL_NAME:
      case COL_DETAIL:
        return 100;
      case COL_COST:
      case COL_TARGET:
        return 40;
      case COL_SUPPLY:
      case COL_TRANSIT:
      case COL_QUEUE:
        return 30;
      default:
        return 15;
    }
  }

  public int getAlignment(int col) {
    switch (col) {
      case COL_COST:
      case COL_TON:
        return SwingConstants.RIGHT;
      case COL_TARGET:
        return SwingConstants.CENTER;
      default:
        return SwingConstants.LEFT;
    }
  }

  @Override
  public String getTooltip(int row, int col) {
    PartProxy part;
    if (data.isEmpty()) {
      return null;
    } else {
      part = data.get(row);
    }
    if (col == COL_TARGET) {
      return part.getTarget().getDescription();
    }
    return null;
  }

  /**
   * Provides a lazy view to a {@link Part} for use in a UI (e.g. sorting in a table).
   */
  public static class PartProxy {
    private final PartsStoreTableModel partsStoreTableModel;
    private Part part;
    private String details;
    private TargetProxy targetProxy;
    private FormattedValue<Money> cost;
    private PartInventory inventories;
    private FormattedValue<Integer> ordered;
    private FormattedValue<Integer> supply;
    private FormattedValue<Integer> transit;

    /**
     * Initializes a new of the class to provide a proxy view into
     * a part.
     *
     * @param p The part to proxy. Must not be null.
     */
    public PartProxy(PartsStoreTableModel partsStoreTableModel, Part p) {
      this.partsStoreTableModel = partsStoreTableModel;
      part = Objects.requireNonNull(p);
    }

    /**
     * Updates the proxied view of the properties which
     * changed outside the proxy.
     */
    public void updateTargetAndInventories() {
      targetProxy = null;
      inventories = null;
      ordered = null;
      supply = null;
      transit = null;
    }

    /**
     * Gets the part being proxied.
     *
     * @return The part being proxied.
     */
    public Part getPart() {
      return part;
    }

    /**
     * Gets the part's name.
     *
     * @return The part's name.
     */
    public String getName() {
      return part.getName();
    }

    /**
     * Gets the part's details.
     *
     * @return The part's detailed.
     */
    public String getDetails() {
      if (null == details) {
        details = part.getDetails();
      }

      return details;
    }

    /**
     * Gets the part's cost, suitable for use in a UI element
     * which requires both a display value and a sortable value.
     *
     * @return The part's cost as a {@link FormattedValue}
     */
    public FormattedValue<Money> getCost() {
      if (null == cost) {
        Money actualValue = part.getActualValue();
        cost = new FormattedValue<>(actualValue, actualValue.toAmountAndSymbolString());
      }
      return cost;
    }

    /**
     * Gets the part's tonnage.
     *
     * @return The part's tonnage.
     */
    public double getTonnage() {
      return Math.round(part.getTonnage() * 100) / 100.0;
    }

    /**
     * Gets the part's tech base.
     *
     * @return The part's tech base.
     */
    public String getTechBase() {
      return part.getTechBaseName();
    }

    /**
     * Gets the part's {@link TargetRoll}.
     *
     * @return A {@link TargetProxy} representing the target
     * roll for the part.
     */
    public TargetProxy getTarget() {
      if (null == targetProxy) {
        IAcquisitionWork shoppingItem = part.getMissingPart();
        if (null == shoppingItem && part instanceof IAcquisitionWork) {
          shoppingItem = (IAcquisitionWork) part;
        }
        if (null != shoppingItem) {
          TargetRoll target = partsStoreTableModel.getCampaign().getTargetForAcquisition(shoppingItem, partsStoreTableModel.getLogisticsPerson(), true);
          targetProxy = new TargetProxy(target);
        } else {
          targetProxy = new TargetProxy(null);
        }
      }

      return targetProxy;
    }

    /**
     * Gets the part's quantity on order, suitable for use in a UI element
     * which requires both a display value and a sortable value.
     *
     * @return The part's quantity on order as a {@link FormattedValue}
     */
    public FormattedValue<Integer> getOrdered() {
      if (null == inventories) {
        inventories = partsStoreTableModel.getCampaign().getPartInventory(part);
      }
      if (null == ordered) {
        ordered = new FormattedValue<>(inventories.getOrdered(), inventories.orderedAsString());
      }
      return ordered;
    }

    /**
     * Gets the part's quantity on hand, suitable for use in a UI element
     * which requires both a display value and a sortable value.
     *
     * @return The part's quantity on hand as a {@link FormattedValue}
     */
    public FormattedValue<Integer> getSupply() {
      if (null == inventories) {
        inventories = partsStoreTableModel.getCampaign().getPartInventory(part);
      }
      if (null == supply) {
        supply = new FormattedValue<>(inventories.getSupply(), inventories.supplyAsString());
      }
      return supply;
    }

    /**
     * Gets the part's quantity in transit, suitable for use in a UI element
     * which requires both a display value and a sortable value.
     *
     * @return The part's quantity in transit as a {@link FormattedValue}
     */
    public FormattedValue<Integer> getTransit() {
      if (null == inventories) {
        inventories = partsStoreTableModel.getCampaign().getPartInventory(part);
      }
      if (null == transit) {
        transit = new FormattedValue<>(inventories.getTransit(), inventories.transitAsString());
      }
      return transit;
    }

  }
}
