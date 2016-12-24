package quests;

import org.osbot.rs07.api.Quests.Quest;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.Condition;

import botFarm.Constants;
import botFarm.Environment;
import botFarm.Node;

public class CooksAssistant extends Node {

	boolean debug = e.debug;

	public CooksAssistant(Script s, Environment e) {
		super(s, e);
	}

	@Override
	public boolean validate() throws InterruptedException {
		return true; // !s.quests.isComplete(Quest.COOKS_ASSISTANT);
	}

	@Override
	public boolean execute() throws InterruptedException {
		if (s.quests.isComplete(Quest.COOKS_ASSISTANT)) {
			((botFarm.BotFactory) s).changeStateNodes();
		} else if (!s.inventory.contains("Egg")) {
			acquireEgg();
		} else if (!s.inventory.contains("Bucket of milk")) {
			acquireMilk();
		} else if (!s.inventory.contains("Pot of flour")) {
			acquireFlour();
		} else {
			completeQuest();
		}
		return true;
	}

	private void completeQuest() throws InterruptedException {
		walkTo(new Area(3205, 3214, 3210, 3209));
		NPC cook = s.npcs.closest("Cook");
		if (cook != null) {
			if (s.configs.get(29) < 1) {
				cook.interact("Talk-to");
				Constants.condSleep(8500, 1000, () -> s.dialogues.isPendingContinuation());
				s.dialogues.completeDialogue("What's wrong?", "I'm always happy to help a cook in distress.",
						"Actually, I know where to find this stuff.");
				Script.sleep(500);
			}
			cook.interact("Talk-to");
			Constants.condSleep(8500, 1000, () -> s.dialogues.isPendingContinuation());
			s.dialogues.completeDialogue();
			Constants.condSleep(8500, 1000, () -> s.widgets.get(277, 15) != null);
			RS2Widget w = s.widgets.get(277, 15);
			if(w != null) {
				w.interact("Close");
				Constants.condSleep(2000, 222, () -> s.dialogues.isPendingContinuation());
				if(s.dialogues.isPendingContinuation())
					s.dialogues.clickContinue();
			}
			
		}
	}

	private void acquireEgg() throws InterruptedException {
		if (new Area(3228, 3298, 3230, 3296).contains(s.myPosition())) {
			GroundItem egg = s.groundItems.closest("Egg");
			if (egg != null && s.map.canReach(egg)) {
				egg.interact("Take");
				Constants.condSleep(8500, 300, () -> s.inventory.contains("Egg"));
			} else {
				Script.sleep(2000);
			}
		} else {
			walkTo(new Area(3228, 3298, 3230, 3296));
		}
	}

	private void acquireMilk() throws InterruptedException {
		walkTo(new Area(3254, 3269, 3258, 3264));
		RS2Object cow = s.objects.closest(8689);
		if (cow != null) {
			if (s.map.canReach(cow)) {
				cow.interact("Milk");
				Constants.condSleep(8500, 1000, () -> s.inventory.contains("Bucket of Milk"));
			} else {
				RS2Object gate = s.objects.closest("Gate");
				if (gate != null)
					gate.interact("Open");
				Script.sleep(450);
			}
		}
	}

	private void acquireFlour() throws InterruptedException {
		if (s.configs.get(695) != 1) {
			if (!s.inventory.contains("Grain")) {
				walkTo(new Area(3161, 3293, 3163, 3291));
				RS2Object wheat = s.objects.closest("Wheat");
				if (wheat != null) {
					if (s.map.canReach(wheat)) {
						wheat.interact("Pick");
						Constants.condSleep(8500, 1000, () -> s.inventory.contains("Grain"));
					} else {
						s.objects.closest("Gate").interact("Open");
						Script.sleep(400);
					}
				}
			}

			if (s.inventory.contains("Grain")) {
				WebWalkEvent event = new WebWalkEvent(new Position(3164, 3306, 2));
				s.execute(event);
				RS2Object hopper = s.objects.closest("Hopper");
				if (hopper != null) {
					s.inventory.getItem("Grain").interact("Use");
					Constants.condSleep(8500, 1000, () -> s.inventory.isItemSelected());
					hopper.interact("use");
					Constants.condSleep(8500, 1000, () -> !s.inventory.contains("Grain"));
					Script.sleep(1500);

					RS2Object controls = s.objects.closest("Hopper controls");
					if (controls != null) {
						controls.interact("Operate");
						Constants.condSleep(8000, 500, () -> s.configs.get(695) == 1);
						Script.sleep(450);
					}
				}
			}
		} else {
			WebWalkEvent event = new WebWalkEvent(new Position(3164, 3306, 0));
			event.setBreakCondition(new Condition(){
				@Override
				public boolean evaluate() {
					RS2Object bin = s.objects.closest("Flour bin");
					return bin != null && bin.isVisible();
				}
			});
			s.execute(event);

			RS2Object bin = s.objects.closest("Flour bin");
			if (bin != null) {
				bin.interact("Empty");
				Constants.condSleep(8500, 1000, () -> s.inventory.contains("Pot of flour"));
			}
		}

	}

	private void walkTo(Area area) throws InterruptedException {
		WebWalkEvent event = new WebWalkEvent(area);
		event.setBreakCondition(new Condition() {
			@Override
			public boolean evaluate() {
				return area.contains(s.myPosition());
			}
		});
		s.execute(event);
	}
}
