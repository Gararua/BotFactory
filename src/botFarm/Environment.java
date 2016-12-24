package botFarm;

import java.util.ArrayList;
import java.util.HashMap;

import org.osbot.rs07.api.ui.EquipmentSlot;

public class Environment {	
	public boolean lootBones, debug = false, questing, questOnly, defaultGear, looting;
	public int trainAttkLvl, trainStrLvl, trainDefLvl, lootLikelihood, rightClickLikelihood, hoverLikelihood,
			afkLikelihood, chickensAttkCutOff = 0, chickensStrCutOff = 0, chickensDefCutOff = 0, attkLvlCutOff, strLvlCutOff,
			defLvlCutOff, geOfferButtonsLikelihood, eatLikelihood, eatUpLikelihood, reattackLikelihood, preferredBank,
			changeStyleLikelihood, minLevelGap, cowsAttkCutOff, cowsStrCutOff, cowsDefCutOff, cowhideLoot = 0, cowhideLootCutOff;
	public String food;
	public ArrayList<Integer> styles;
	public ArrayList<String> loot, itemsToSell, npcs;
	public HashMap<String, Integer> itemsToBuy, itemsToBring;
	public HashMap<EquipmentSlot, String> itemsToEquip, customGear;
}
