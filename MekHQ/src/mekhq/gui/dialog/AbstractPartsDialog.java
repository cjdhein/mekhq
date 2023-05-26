package mekhq.gui.dialog;

import megamek.client.ui.preferences.JComboBoxPreference;
import megamek.client.ui.preferences.JTablePreference;
import megamek.client.ui.preferences.JWindowPreference;
import megamek.client.ui.preferences.PreferencesNode;
import megamek.common.MiscType;
import megamek.common.TargetRoll;
import megamek.common.WeaponType;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.*;
import mekhq.campaign.parts.equipment.EquipmentPart;
import mekhq.gui.CampaignGUI;
import mekhq.gui.model.PartsStoreTableModel;
import mekhq.gui.model.PartsTableModel;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;

/**
 * @author cjdhein
 */
public abstract class AbstractPartsDialog extends JDialog {
  //region Variable Declarations
  // parts filter groups
  protected static final int SG_ALL = 0;
  protected static final int SG_ARMOR = 1;
  protected static final int SG_SYSTEM = 2;
  protected static final int SG_EQUIP = 3;
  protected static final int SG_LOC = 4;
  protected static final int SG_WEAP = 5;
  protected static final int SG_AMMO = 6;
  protected static final int SG_MISC = 7;
  protected static final int SG_ENGINE = 8;
  protected static final int SG_GYRO = 9;
  protected static final int SG_ACT = 10;
  protected static final int SG_COCKPIT = 11;
  protected static final int SG_BA_SUIT = 12;
  protected static final int SG_OMNI_POD = 13;
  protected static final int SG_NUM = 14;
  protected final transient ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.PartsStoreDialog",
      MekHQ.getMHQOptions().getLocale());
  protected Campaign campaign;
  protected CampaignGUI campaignGUI;
  protected PartsStoreTableModel partsModel;
  protected Part selectedPart;
  protected TableRowSorter<PartsTableModel> partsSorter;
  protected JTable partsTable;
  protected JTextField txtFilter;
  protected JComboBox<String> choiceParts;
  protected Predicate<RowFilter.Entry<? extends PartsTableModel, ? extends Integer>> filterPredicate;

  public AbstractPartsDialog(Frame owner, boolean modal, final CampaignGUI gui) {
    super(owner, modal);
    this.campaignGUI = gui;
    this.campaign = gui.getCampaign();
    partsModel = new PartsStoreTableModel(campaign);
    selectedPart = null;
    filterPredicate = part -> txtFilterTest(part) && typeFilterTest(part);
  }

  public static String getPartsGroupName(int group) {
    switch (group) {
      case SG_ALL:
        return "All Parts";
      case SG_ARMOR:
        return "Armor";
      case SG_SYSTEM:
        return "System Components";
      case SG_EQUIP:
        return "Equipment";
      case SG_LOC:
        return "Locations";
      case SG_WEAP:
        return "Weapons";
      case SG_AMMO:
        return "Ammunition";
      case SG_MISC:
        return "Miscellaneous Equipment";
      case SG_ENGINE:
        return "Engines";
      case SG_GYRO:
        return "Gyros";
      case SG_ACT:
        return "Actuators";
      case SG_COCKPIT:
        return "Cockpits";
      case SG_BA_SUIT:
        return "Battle Armor Suits";
      case SG_OMNI_POD:
        return "Empty OmniPods";
      default:
        return "?";
    }
  }

  protected abstract void initComponents();

  @Deprecated // These need to be migrated to the Suite Constants / Suite Options Setup
  protected void setUserPreferences() {
    try {
      PreferencesNode preferences = MekHQ.getMHQPreferences().forClass(PartsStoreDialog.class);

      choiceParts.setName("partsType");
      preferences.manage(new JComboBoxPreference(choiceParts));

      partsTable.setName("partsTable");
      preferences.manage(new JTablePreference(partsTable));

      this.setName("dialog");
      preferences.manage(new JWindowPreference(this));
    } catch (Exception ex) {
      LogManager.getLogger().error("Failed to set user preferences", ex);
    }
  }
  public void filterParts() {
    partsSorter.setRowFilter(new RowFilter<>() {
      @Override
      public boolean include(Entry<? extends PartsTableModel, ? extends Integer> entry) {
        Boolean testVal = filterPredicate.test(entry);
        return testVal;
      }
    });
  }

  protected boolean txtFilterTest(RowFilter.Entry<? extends PartsTableModel, ? extends Integer> entry) {
    PartsTableModel partsModel = entry.getModel();
    Part part = partsModel.getPartAt(entry.getIdentifier());
    if (!txtFilter.getText().isBlank()
        && !part.getName().toLowerCase().contains(txtFilter.getText().toLowerCase())
        && !part.getDetails().toLowerCase().contains(txtFilter.getText().toLowerCase())) {
      return false;
    } else if (((part.getTechBase() == Part.T_CLAN) || part.isClan())
        && !campaign.getCampaignOptions().isAllowClanPurchases()) {
      return false;
    } else if ((part.getTechBase() == Part.T_IS)
        && !campaign.getCampaignOptions().isAllowISPurchases()
        // Hack to allow Clan access to SL tech but not post-Exodus tech
        // until 3050.
        && !(campaign.useClanTechBase() && (part.getIntroductionDate() > 2787)
        && (part.getIntroductionDate() < 3050))) {
      return false;
    } else if (!campaign.isLegal(part)) {
      return false;
    } else {
      return true;
    }
  }

  protected boolean typeFilterTest(RowFilter.Entry<? extends PartsTableModel, ? extends Integer> entry) {
    PartsTableModel partsModel = entry.getModel();
    Part part = partsModel.getPartAt(entry.getIdentifier());
    final int nGroup = choiceParts.getSelectedIndex();
    if (nGroup == SG_ALL) {
      return true;
    } else if (nGroup == SG_ARMOR) {
      return part instanceof Armor; // ProtoMekAmor and BaArmor are derived from Armor
    } else if (nGroup == SG_SYSTEM) {
      return (part instanceof MekLifeSupport)
          || (part instanceof MekSensor)
          || (part instanceof LandingGear)
          || (part instanceof Avionics)
          || (part instanceof FireControlSystem)
          || (part instanceof AeroSensor)
          || (part instanceof KfBoom)
          || (part instanceof DropshipDockingCollar)
          || (part instanceof JumpshipDockingCollar)
          || (part instanceof BayDoor)
          || (part instanceof Cubicle)
          || (part instanceof GravDeck)
          || (part instanceof VeeSensor)
          || (part instanceof VeeStabiliser)
          || (part instanceof ProtomekSensor);
    } else if (nGroup == SG_EQUIP) {
      return (part instanceof EquipmentPart) || (part instanceof ProtomekJumpJet);
    } else if (nGroup == SG_LOC) {
      return (part instanceof MekLocation) || (part instanceof TankLocation)
          || (part instanceof ProtomekLocation);
    } else if (nGroup == SG_WEAP) {
      return (part instanceof EquipmentPart)
          && (((EquipmentPart) part).getType() instanceof WeaponType);
    } else if (nGroup == SG_AMMO) {
      boolean b = part instanceof AmmoStorage;
      return b;
    } else if (nGroup == SG_MISC) {
      return ((part instanceof EquipmentPart)
          && (((EquipmentPart) part).getType() instanceof MiscType)
          || (part instanceof ProtomekJumpJet));
    } else if (nGroup == SG_ENGINE) {
      return part instanceof EnginePart;
    } else if (nGroup == SG_GYRO) {
      return part instanceof MekGyro;
    } else if (nGroup == SG_ACT) {
      return ((part instanceof MekActuator) || (part instanceof ProtomekArmActuator)
          || (part instanceof ProtomekLegActuator));
    } else if (nGroup == SG_COCKPIT) {
      return part instanceof MekCockpit;
    } else if (nGroup == SG_BA_SUIT) {
      return part instanceof BattleArmorSuit;
    } else if (nGroup == SG_OMNI_POD) {
      return part instanceof OmniPod;
    } else {
      return false;
    }
  }

  protected void setSelectedPart() {
    int row = partsTable.getSelectedRow();
    if (row < 0) {
      return;
    }
    selectedPart = partsModel.getPartAt(partsTable.convertRowIndexToModel(row));
  }

  public Part getPart() {
    return selectedPart;
  }

}
