package botFarm;

import java.util.HashMap;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

public class SimpleBank extends Node {

	private Entity banker;
	private boolean doTheBestYouCan;// , debug = Constants.debug;

	public SimpleBank(Script s, Environment e, boolean doTheBestYouCan) {
		super(s, e);
		this.doTheBestYouCan = doTheBestYouCan;
	}

	@Override
	public boolean validate() throws InterruptedException {
		banker = s.objects.closest("Bank booth");
		if (banker == null) {
			banker = s.objects.closest("Bank chest");
			if (banker == null)
				banker = s.npcs.closest("Banker");

		}
		return banker != null && banker.exists();
	}

	@Override
	public boolean execute() throws InterruptedException {
		if (banker != null)
			banker.interact("Bank");
		Constants.condSleep(13000, 300, () -> s.bank.isOpen());
		Script.sleep(Script.random(200, 400));
		s.bank.depositAll();
		Script.sleep(Script.random(200, 400));

		if (doTheBestYouCan) {
			if (!s.equipment.isWearingItem(EquipmentSlot.WEAPON)) {
				Filter<Item> weaponFilter = new Filter<Item>() {
					@Override
					public boolean match(Item item) {
						String iName = item.getName();
						return iName.contains("scimitar") || iName.contains("sword");
					}
				};
				s.bank.withdraw(weaponFilter, 1);
				s.bank.withdraw(e.food, s.skills.getStatic(Skill.DEFENCE) < 5 ? Script.random(5, 7) : 2);
				s.bank.close();
				Script.sleep(Script.random(200, 400));
				for (Item i : s.inventory.getItems()) {
					if (i != null && i.hasAction("Wield"))
						i.interact("Wield");
				}
			} else {
				s.bank.withdraw(e.food, 2);
			}
		} else {
			for (String str : e.itemsToBring.keySet()) {
				if (s.bank.contains(str))
					s.bank.withdraw(str, e.itemsToBring.get(str));
				Script.sleep(Script.random(500, 800));
			}
		}
		e.itemsToBring = new HashMap<String, Integer>();
		((BotFactory) s).changeStateNodes();
		return true;
	}

}
