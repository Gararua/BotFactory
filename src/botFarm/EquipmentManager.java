package botFarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;

public class EquipmentManager extends Node {

	private boolean checkedAllItems = false;

	public EquipmentManager(Script s, Environment e) {
		super(s, e);
	}

	@Override
	public boolean validate() throws InterruptedException {
		if (s.npcs.closest("Banker") != null) {
			if (e.itemsToBuy.isEmpty()) {
				return true;
			} else {
				checkedAllItems = false;
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean execute() throws InterruptedException {
		if (!checkedAllItems) {
			bankAll();
		} else {
			if (!armouredUp()) {
				if (e.debug)
					s.log("Armouring up: " + e.itemsToEquip.toString());
				if (allGearOnPerson()) {
					if (e.debug)
						s.log("We have all gear on person");
					if (s.bank.isOpen()) {
						if (s.bank.close()) {
							Script.sleep(Script.random(400, 800));
						} else {
							return false;
						}
					}
					Item[] items = s.inventory.getItems();
					if (items != null) {
						for (Item item : items) {
							if (item != null) {
								if (e.itemsToEquip.values().contains(item.getName())) {
									s.mouse.click(s.inventory.getMouseDestination(s.inventory.getSlot(item.getName())));
									Script.sleep(Script.random(50, 120));
								}
							}
						}
					}
				} else if (openBank()) {
					if (e.debug)
						s.log("Deposit in armour up");
					s.bank.depositAllExcept(e.itemsToEquip.values().toArray(new String[e.itemsToEquip.size()]));
					Script.sleep(Script.random(400, 840));
					ArrayList<String> equips = new ArrayList<String>(e.itemsToEquip.values());
					Collections.shuffle(equips);
					for (String item : equips) {
						if (!s.inventory.contains(item) && !s.equipment.contains(item)) {
							s.bank.withdraw(item, 1);
							Script.sleep(Script.random(50, 210));
						}
					}
				}
			} else if (!allSuppliesOnPerson()) {
				if (e.debug)
					s.log("Supplying up");
				if (openBank()) {
					for (Item item : s.inventory.getItems()) {
						if (item != null && (item.isNote() || !e.itemsToBring.keySet().contains(item.getName()))) {
							if (e.debug)
								s.log("Deposit in supply up");
							s.bank.depositAll();
							Script.sleep(Script.random(600, 900));
							break;
						}
					}
					for (String item : e.itemsToBring.keySet()) {
						s.bank.withdraw(Constants.getId(item),
								(int) (e.itemsToBring.get(item) - s.inventory.getAmount(item)));
						Script.sleep(Script.random(400, 840));

					}
				}
			} else if (s.skills.getDynamic(Skill.HITPOINTS) < s.skills.getStatic(Skill.HITPOINTS) * 0.8) {
				s.bank.close();
				while (s.skills.getDynamic(Skill.HITPOINTS) < s.skills.getStatic(Skill.HITPOINTS) * 0.8) {
					Item food = getFoodPiece();
					if (food != null) {
						int healthBefore = s.skills.getDynamic(Skill.HITPOINTS);
						food.interact("Eat");
						Constants.condSleep(1200, 111, () -> s.skills.getDynamic(Skill.HITPOINTS) > healthBefore);
						Script.sleep(Script.random(640, 800));
					} else {
						break;
					}
				}
			} else {
				((BotFactory) s).changeStateNodes();
			}
		}
		return false;
	}

	private boolean allSuppliesOnPerson() {
		for (Item i : s.inventory.getItems()) {
			if (i != null && !e.itemsToBring.keySet().contains(i.getName())) {
				if (e.debug)
					s.log("Gear check failed, I shouldn't have " + i.getName() + " in my inventory");
				return false;
			}
		}
		for (String str : e.itemsToBring.keySet()) {
			if (s.inventory.getAmount(Constants.getId(str)) < e.itemsToBring.get(str)) {
				if (e.debug)
					s.log("Supply check failed, missing " + e.itemsToBring.get(str) + " number of " + str
							+ " (I only have " + s.inventory.getAmount(str) + ")");
				return false;
			}
		}
		return true;
	}

	private boolean allGearOnPerson() {
		for (EquipmentSlot slot : e.itemsToEquip.keySet()) {
			if (!s.inventory.contains(e.itemsToEquip.get(slot))
					&& !s.equipment.isWearingItem(slot, e.itemsToEquip.get(slot))) {
				return false;
			}
		}
		return true;
	}

	private boolean armouredUp() {
		for (EquipmentSlot slot : e.itemsToEquip.keySet()) {
			if (!s.equipment.isWearingItem(slot, e.itemsToEquip.get(slot))) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean openBank(Entity exception) {
		if (!s.bank.isOpen()) {
			Entity banker = s.objects.closest(new Filter<RS2Object>() {
				@Override
				public boolean match(RS2Object npc) {
					return npc != exception && npc.getName().equals("Bank booth") && npc.isVisible();
				}
			});
			if (banker == null) {
				banker = s.npcs.closest(new Filter<NPC>() {
					@Override
					public boolean match(NPC npc) {
						return npc != exception && npc.getName().equals("Banker");
					}
				});
			}
			if (banker != null) {
				banker.interact("Bank");
				Constants.condSleep(4000, 400, () -> s.bank.isOpen());
				if (!s.bank.isOpen()) {
					openBank(banker);
				}
			}
		}
		return s.bank.isOpen();
	}

	private boolean openBank() {
		return openBank(null);
	}

	@SuppressWarnings("unchecked")
	private void bankAll() throws InterruptedException {
		if (openBank()) {
			e.itemsToBuy = new HashMap<String, Integer>();
			for (String item : e.itemsToBring.keySet()) {
				e.itemsToBuy.put(item, e.itemsToBring.get(item));
			}

			for (String item : e.itemsToEquip.values()) {
				e.itemsToBuy.put(item, 1);
			}
			Script.sleep(Script.random(200, 400));
			if (!s.inventory.isEmpty()) {
				if (s.inventory.getEmptySlots() < Script.random(17, 22)) {
					if (e.debug)
						s.log("Deposit1 in bank all");
					s.bank.depositAll();
				} else {
					if (e.debug)
						s.log("Deposit2 in bank all");
					s.bank.depositAllExcept(new Filter<Item>() {
						@Override
						public boolean match(Item arg0) {
							return arg0.getName() == "Coins"
									|| !arg0.isNote() && e.itemsToBring.keySet().contains(arg0.getName());
						}
					});
				}
			}
			Script.sleep(Script.random(50, 150));
			if (!checkEquipment())
				s.bank.depositWornItems();
			ArrayList<String> itemsToNotBuy = new ArrayList<String>();

			for (Item i : s.equipment.getItems()) {
				if (i != null) {
					if (e.debug)
						s.log("I'm already wearing " + i.getName() + ", so I won't buy one");
					e.itemsToBuy.remove(i.getName());
				}
			}

			// there's a better way of doing this by using standard for loop
			for (String str : e.itemsToBuy.keySet()) {
				if (s.inventory.contains(str) || s.bank.contains(str)) {
					int amount = 0;
					if (s.inventory.contains(str)) {
						amount += s.inventory.getAmount(str);
					}
					if (s.bank.contains(str)) {
						amount += (int) s.bank.getAmount(str);
					}
					int buyAmount = e.itemsToBuy.get(str);
					if (e.debug)
						s.log("I already have " + amount + " of " + str + " in the bank, so I'll only buy "
								+ (buyAmount - amount));
					e.itemsToBuy.replace(str, buyAmount - amount);
					if (buyAmount - amount < 1)
						itemsToNotBuy.add(str);
				}
			}

			if (s.bank.contains("Cowhide")) {
				e.cowhideLoot = (int) s.bank.getAmount("Cowhide");
				if (e.debug)
					s.log("We have " + s.bank.getAmount("Cowhide") + " cowhides");
			}

			for (String st : itemsToNotBuy) {
				e.itemsToBuy.remove(st);
			}

			if (!e.itemsToBuy.isEmpty()) {
				if (e.debug)
					s.log("Items to buy not empty - I need " + e.itemsToBuy);
				if (s.bank.contains("Coins"))
					s.bank.withdrawAll("Coins");
				e.itemsToSell = new ArrayList<String>();
				for (String str : e.loot) {
					if (s.bank.contains(str)) {
						if (s.configs.get(115) != 1) {
							RS2Widget w = s.widgets.getWidgetContainingText("Note");
							if (w != null)
								w.interact("Note");
							Script.sleep(Script.random(220, 400));
						}
						s.bank.withdrawAll(str);
						e.itemsToSell.add(str);
					}
					Script.sleep(Script.random(60, 150));
				}
				if (e.debug)
					s.log("After checking all items, we still need to buy " + e.itemsToBuy);
			} else {
				checkedAllItems = true;
				if (e.debug)
					s.log("It's okay, we have everything");
			}
		}
	}

	private Item getFoodPiece() {
		Item itm = null;
		for (Item i : s.inventory.getItems()) {
			if (i != null) {
				for (String action : i.getActions()) {
					if (action != null && action.equals("Eat")) {
						if (itm != null && Script.random(7) > 5) {
							return itm;
						}
						itm = i;
					}
				}
			}
		}
		return itm;
	}

	private boolean checkEquipment() {
		for (EquipmentSlot slot : e.itemsToEquip.keySet()) {
			if (s.equipment.isWearingItem(slot) && !s.equipment.isWearingItem(slot, e.itemsToEquip.get(slot))) {
				return false;
			}
		}
		return true;
	}
}
