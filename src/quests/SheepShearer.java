package quests;

import org.osbot.rs07.api.Quests.Quest;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.api.ui.RS2Widget;

import botFarm.Constants;
import botFarm.Environment;
import botFarm.Node;

public class SheepShearer extends Node {

	public SheepShearer(Script s, Environment e) {
		super(s, e);
	}

	@Override
	public boolean validate() throws InterruptedException {
		return true;
	}

	@Override
	public boolean execute() throws InterruptedException {
		if (s.quests.isComplete(Quest.SHEEP_SHEARER)) {
			s.log("changing nodes");
			((botFarm.BotFactory) s).changeStateNodes();
		} else if (s.configs.get(179) < 1) {
			talkToFred(false);
		} else if (!s.inventory.contains("Shears")) {
			acquireShears();
		} else if (s.inventory.getAmount("Wool") + s.inventory.getAmount("Ball of wool") < 20) {
			shearSheep();
		} else if (s.inventory.getAmount("Ball of wool") < 20) {
			spinWool();
		} else {
			talkToFred(true);
		}
		return false;
	}

	private void spinWool() throws InterruptedException {
		RS2Object wheel = s.objects.closest("Spinning wheel");
		if (wheel != null) {
			wheel.interact("Spin");
			Constants.condSleep(8500, 1000, () -> s.widgets.isVisible(459, 101));
			Script.sleep(Script.random(200, 400));
			RS2Widget w = s.widgets.get(459, 101);
			w = s.widgets.getWidgetContainingText("Ball of Wool");
			if (w != null && w.isVisible()) {
				if (w.interact("Make X")) {
					Constants.condSleep(2000, 100,
							() -> s.widgets.getWidgetContainingText(162, "Enter amount") != null);
					if (s.widgets.getWidgetContainingText(162, "Enter amount") != null) {
						s.keyboard.typeString("20", true);
						Constants.condSleep(60000, 300, () -> s.inventory.getAmount("Ball of Wool") > 19);
					}
				}
			}
		} else {
			WebWalkEvent event = new WebWalkEvent(new Position(3209, 3214, 1));
			s.execute(event);
		}
	}

	private void shearSheep() throws InterruptedException {
		if (s.settings.getRunEnergy() > 9)
			s.settings.setRunning(true);
		@SuppressWarnings("unchecked")
		NPC sheep = s.getNpcs().closest(new Filter<NPC>() {
			@Override
			public boolean match(NPC npc) {
				return Constants.sheepArea.contains(npc) && npc.getName().equals("Sheep") && npc.hasAction("Shear")
						&& npc.getId() != 731;
			}
		});
		if (sheep != null) {
			if (!s.map.canReach(sheep))
				walkTo(Constants.sheepArea);
			sheep.interact("Shear");
			Constants.condSleep(5000, 600, () -> s.myPlayer().getAnimation() == 893 || !sheep.hasAction("Shear"));
			Script.sleep(1000);
		} else {
			walkTo(Constants.sheepArea);
		}
	}

	private void acquireShears() {
		GroundItem shears = s.groundItems.closest("Shears");
		if (shears != null) {
			if (!s.map.canReach(shears)) {
				RS2Object door = s.objects.closest(13001);
				if (door != null) {
					door.interact("Open");
					Constants.condSleep(8500, 1000, () -> s.map.canReach(shears));
				}
			}
			shears.interact("Take");
			Constants.condSleep(4000, 1000, () -> s.inventory.contains("Shears"));
		}

	}

	private void talkToFred(boolean finish) throws InterruptedException {
		WebWalkEvent event = new WebWalkEvent(new Position(3185, 3271, 0));
		event.setBreakCondition(new Condition() {
			@Override
			public boolean evaluate() {
				NPC farmer = s.npcs.closest("Fred the Farmer");
				return farmer != null && s.map.canReach(farmer);
			}

		});
		s.execute(event);
		NPC fred = s.npcs.closest("Fred the Farmer");
		if (fred != null) {
			fred.interact("Talk-to");
			Constants.condSleep(8500, 1000, () -> s.dialogues.isPendingContinuation());
			Script.sleep(420);
			if (finish) {
				s.dialogues.completeDialogue("I'm back!");
				Constants.condSleep(8500, 1000, () -> s.widgets.get(277, 15) != null);
				RS2Widget w = s.widgets.get(277, 15);
				if (w != null) {
					w.interact("Close");
					Constants.condSleep(2000, 222, () -> s.dialogues.isPendingContinuation());
					if (s.dialogues.isPendingContinuation())
						s.dialogues.clickContinue();
				}
			} else {
				s.dialogues.completeDialogue("I'm looking for a quest.", "Yes okay. I can do that.", "Of course!");
			}
		}
	}

	private void walkTo(Area area) throws InterruptedException {
		WebWalkEvent event = new WebWalkEvent(area);
		s.execute(event);
	}
}
