package quests;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.Condition;

import botFarm.BotFactory;
import botFarm.Constants;
import botFarm.Environment;
import botFarm.Node;

public class RomeoAndJuliet extends Node {

	public RomeoAndJuliet(Script s, Environment e) {
		super(s, e);
	}

	@Override
	public boolean validate() throws InterruptedException {
		return true;
	}

	@Override
	public boolean execute() throws InterruptedException {
		switch (s.configs.get(144)) {
		case 0:
			if (!s.inventory.contains("Cadava berries")) {
				pickBerries();
			} else {
				talkToNPC(new Position(3210, 3424, 0), "Romeo", "Yes, I have seen her actually!",
						"Yes, ok, I'll let her know.", "Ok, thanks.");
			}
			break;
		case 10:
			talkToNPC(new Position(3155, 3433, 1), "Juliet");
			break;
		case 20:
			talkToNPC(new Position(3210, 3424, 0), "Romeo");
			break;
		case 30:
			talkToNPC(new Position(3254, 3480, 0), "Father Lawrence");
			break;
		case 40:
			talkToNPC(new Position(3195, 3404, 0), "Apothecary", "Ok, thanks.");
			break;
		case 50:
			if (s.configs.get(1021) == 192) {
				s.dialogues.clickContinue();
				Script.sleep(Script.random(350, 600));
			} else if (!s.inventory.contains("Cadava potion")) {
				talkToNPC(new Position(3195, 3404, 0), "Apothecary");
			} else { // 1021, 192
				talkToNPC(new Position(3155, 3433, 1), "Juliet");
			}
			break;
		case 60:
			if (s.configs.get(1021) == 192) {
				if (s.dialogues.isPendingContinuation()) {
					s.dialogues.clickContinue();
				}
				Script.sleep(Script.random(350, 600));
			} else {
				talkToNPC(new Position(3210, 3424, 0), "Romeo");
			}
			break;
		case 100:
			((BotFactory) s).changeStateNodes();
			;
			break;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private void talkToNPC(Position p, String name, String... strs) throws InterruptedException {
		WebWalkEvent event = new WebWalkEvent(p);
		event.setBreakCondition(new Condition() {
			@Override
			public boolean evaluate() {
				return s.npcs.closest(name) != null && s.npcs.closest(name).isVisible();
			}
		});
		s.execute(event);

		NPC romeo = s.npcs.closest(name);
		if (romeo != null) {
			if (!s.map.canReach(romeo)) {
				RS2Object door = s.objects.closest(new Filter<RS2Object>(){
					@Override
					public boolean match(RS2Object arg0) {
						return arg0 != null && arg0.getId() == 11773 && arg0.getY() < 3434;
					}
				});
				if (door != null)
					door.interact("Open");
				Script.sleep(600);
			} else {
				romeo.interact("Talk-to");
				Constants.condSleep(4000, 1000, () -> s.dialogues.isPendingContinuation());
				Script.sleep(Script.random(300, 600));
				s.dialogues.completeDialogue(strs);
			}
		}
	}

	private void pickBerries() throws InterruptedException {
		WebWalkEvent event = new WebWalkEvent(new Position(3273, 3370, 0));
		s.execute(event);
		Script.sleep(Script.random(300, 600));
		RS2Object bush = s.objects.closest("Cadava bush");
		if (bush != null) {
			if (bush.interact("Pick-from"))
				Constants.condSleep(8000, 1000, () -> s.inventory.contains("Cadava berries"));
		}
	}

}
