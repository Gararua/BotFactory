package botFarm;

import java.awt.Rectangle;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.input.mouse.InventorySlotDestination;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class Loot extends Node {

	private int lootAttempts = 0;
	private boolean looting;

	public Loot(Script s, Environment e) {
		super(s, e);
	}

	@Override
	public boolean validate() throws InterruptedException {
		if (looting)
			return true;
		if (lootAttempts > e.lootLikelihood || Script.random(lootAttempts, e.lootLikelihood) == lootAttempts) {
			if (s.inventory.isFull()) {
				if(e.lootBones && s.inventory.contains("Bones"))
					buryBones();
				return false;
			} else {
				lootAttempts = 0;
				looting = true;
				return true;
			}
		} else {
			lootAttempts++;
			return false;
		}
	}

	@Override
	public boolean execute() throws InterruptedException {
		if (e.debug)
			s.log("Looting");
		if (s.myPlayer().isUnderAttack()) {
			return false;
		} else {
			if(s.inventory.getEmptySlots() < 6)
				e.loot.remove("Bones");
			e.looting = true;
			if (!s.inventory.isFull()) {
				if (Script.random(8) == 7) {
					e.looting = false;
					looting = false;
					return false;
				}
				Constants.condSleep(2500, 100, () -> !s.myPlayer().isMoving());
				String[] lootArray = new String[e.loot.size()];
				GroundItem g1 = s.groundItems.closest(e.loot.toArray(lootArray));
				if (g1 == null || s.map.distance(g1) > (((BotFactory) s).needMoreMoney ? 10 : 5)) {
					e.looting = false;
					looting = false;
					return false;
				}
				GroundItem firstItem = s.groundItems.get(g1.getX(), g1.getY()).get(0);
				if (e.loot.contains(firstItem.getName())) {
					g1 = firstItem;
				}
				GroundItem g = g1;
				int amount = (int) s.inventory.getAmount(g.getId());
				boolean moreThanOne = false;
				if ((g != null) && (g.exists())) {
					for (GroundItem f : s.groundItems.get(g.getX(), g.getY())) {
						if (!f.getName().equals(g.getName())) {
							for (String i : e.loot) {
								if (f.getName().equals(i)) {
									moreThanOne = true;
									break;
								}
							}
						}
					}
					if (!s.map.canReach(g)) {
						openDoor();
					} else if (moreThanOne && Script.random(5) > 2) {
						g.interact(new String[] { "Take" });
						Script.sleep(Script.random(100, 500));
						EntityDestination targetDest = new EntityDestination(s.getBot(), g);
						if (targetDest.getArea().contains(s.mouse.getPosition())) {
							s.mouse.click(true);
						} else {
							s.mouse.click(targetDest, true);
						}
						Constants.condSleep(500, 40, () -> s.menu.isOpen());
						if (s.menu.isOpen()) {
							e.loot.remove(g.getName());
							lootArray = new String[e.loot.size()];
							int menuIndex = s.menu.getMenuIndex(e.loot.toArray(lootArray), new String[] { "Take" });
							e.loot.add(g.getName());
							Script.sleep(66);
							if (menuIndex != -1) {
								Rectangle rekt = s.menu.getOptionRectangle(menuIndex);
								s.mouse.move(new RectangleDestination(s.getBot(), rekt));
								new ConditionalSleep(2500, 100) {
									@Override
									public boolean condition() throws InterruptedException {
										return !s.myPlayer().isMoving() && amount < s.inventory.getAmount(g.getId()) || g == null;
									}
								}.sleep();
								s.mouse.click(false);
							}
							Script.sleep(Script.random(100, 400));
						}
					} else {
						int invcount = (int) s.inventory.getAmount(g.getName());
						g.interact(new String[] { "Take" });
						Constants.condSleep(3500, 100, () -> ((int) s.inventory.getAmount(g.getName()) > invcount) || g == null);
					}
				}
			} else {
				if (e.lootBones) {
					buryBones();
				}
				e.looting = false;
				looting = false;
			}
			if(s.inventory.getEmptySlots() < 6)
				e.loot.add("Bones");
			return true;
		}
	}

	private void buryBones() throws IllegalArgumentException, InterruptedException {
		if (s.inventory.contains("Bones")) {
			Item bone = s.inventory.getItem(new String[] { "Bones" });
			bone.interact(new String[] { "Bury" });
			while (s.inventory.contains("Bones")) {
				Item bone2 = getNextBone(bone);
				InventorySlotDestination dest = new InventorySlotDestination(s.getBot(), s.inventory.getSlot(bone2));
				if (!dest.getArea().contains(s.mouse.getPosition())) {
					bone2.hover();
				}
				condSleep(bone);
				s.mouse.click(false);
				bone = bone2;
				Script.sleep(Script.gRandom(150, 250, 40));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Item getNextBone(Item i) {
		return s.inventory.getItem(new Filter<Item>() {
			@Override
			public boolean match(Item arg0) {
				return arg0 != i && arg0.getName().equals("Bones");
			}
		});
	}

	private void condSleep(Item i) {
		new ConditionalSleep(600, 50) {
			@Override
			public boolean condition() throws InterruptedException {
				return i != null;
			}
		}.sleep();
	}

	private void openDoor() throws InterruptedException {
		RS2Object gate = s.getObjects().closest("Gate");
		if (gate != null) {
			gate.interact("open");
			Script.sleep(400);
		}
	}

}
