package mekhq.campaign.mission;

/**
 * A data structure containing metadata relevant to the effect that completing or failing an objective 
 * can have. 
 * @author NickAragua
 *
 */
public class ObjectiveEffect {
    
    public enum ObjectiveEffectType {
        ContractScoreUpdate,
        SupportPointUpdate,
        ContractMoraleUpdate,
        ContractVictory,
        ContractDefeat,
        BVBudgetUpdate
    }        
    
    public ObjectiveEffectType effectType;
    // whether the effect is scaled to the # of units or fixed in nature
    public boolean scaledEffect;
    // how much of the effect per unit, or how much of the effect fixed
    public int howMuch;
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(effectType.toString());
        sb.append(scaledEffect ? " - scaled -" : " - fixed -");
        sb.append(howMuch);
        return sb.toString();
    }
}
