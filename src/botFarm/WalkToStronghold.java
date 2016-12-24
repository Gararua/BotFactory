package botFarm;

import java.util.HashMap;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.event.WalkingEvent;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.Condition;

public class WalkToStronghold extends Node {

	private final Area firstFloorFirstRoom = new Area(1856, 5245, 1866, 5238),
			firstFloorTreasureRoom = new Area(1900, 5229, 1916, 5215),
			secondFloorFirstRoom = new Area(2040, 5246, 2047, 5240),
			secondFloorFirstRoomIntermediate = new Area(2044, 5239, 2045, 5237),
			secondFloorSecondRoom = new Area(2036, 5236, 2046, 5204),
			secondFloorSecondRoomIntermediate = new Area(2036, 5203, 2037, 5201),
			secondFloorThirdRoom = new Area(2047, 5198, 2035, 5203),
			secondFloorThirdRoomIntermediate = new Area(2045, 5197, 2046, 5195);
	private HashMap<String, Integer> strongholdQuestions;
	private boolean reversed;
	private Area destination;

	public WalkToStronghold(Script s, Environment e, boolean reversed, Area walkToArea) {
		super(s, e);
		this.reversed = reversed;
		this.destination = walkToArea;
		addQuestions();
	}

	public WalkToStronghold(Script s, Environment e) {
		super(s, e);
		addQuestions();
	}

	private void addQuestions() {
		strongholdQuestions = new HashMap<String, Integer>();
		strongholdQuestions.put("out of the room", 2);
		strongholdQuestions.put("great add-on", 1);
		strongholdQuestions.put("Who can I give my<br>password to", 3);
		strongholdQuestions.put("from saying my PIN in game", 2);
		strongholdQuestions.put("answers to make you a player moderator", 1);
		strongholdQuestions.put("Lottery", 3);
		strongholdQuestions.put("moderator asks me for my account details", 3);
		strongholdQuestions.put("How do I set a", 1);
		strongholdQuestions.put("difficult quest", 2);
		strongholdQuestions.put("member for free", 3);
		strongholdQuestions.put("good bank PIN", 3);
		strongholdQuestions.put("share my account", 3);
		strongholdQuestions.put("cheats for RuneScape", 3);
		strongholdQuestions.put("enter my RuneScape password?", 2);
		strongholdQuestions.put("can become a player moderator by giving them my", 1);
		strongholdQuestions.put("keylogger or virus", 1);
		strongholdQuestions.put("Where is it safe", 2);
		strongholdQuestions.put("hijacker", 3);
		strongholdQuestions.put("security step you can take", 1);
		strongholdQuestions.put("account is compromised", 2);
	}

	@Override
	public boolean validate() throws InterruptedException {
		if (reversed) {
			NPC banker = s.npcs.closest("Banker");
			if (banker != null && banker.isVisible())
				return false;
		}
		return reversed ? !destination.contains(s.myPosition()) : !Constants.fleshCrawlerArea.contains(s.myPosition());
	}

	@Override
	public boolean execute() throws InterruptedException {
		if (reversed && Constants.fleshCrawlerArea.contains(s.myPosition())) {
			secondFloorThirdRoom();
		} else if (secondFloorFirstRoom.contains(s.myPosition())) {
			if (e.debug)
				s.log("Second floor first room");
			secondFloorFirstRoom();
		} else if (secondFloorFirstRoomIntermediate.contains(s.myPosition())) {
			if (e.debug)
				s.log("Second floor first room intermediate");
			secondFloorFirstRoomIntermediate();
		} else if (secondFloorSecondRoom.contains(s.myPosition())) {
			if (e.debug)
				s.log("Second floor second room");
			secondFloorSecondRoom();
		} else if (secondFloorSecondRoomIntermediate.contains(s.myPosition())) {
			if (e.debug)
				s.log("Second floor second room intermediate");
			if (reversed)
				openSecurityDoor(5203);
			else
				openSecurityDoor(5201);
		} else if (secondFloorThirdRoomIntermediate.contains(s.myPosition())) {
			if (e.debug)
				s.log("Second floor third room intermediate");
			secondFloorThirdRoomIntermediate();
		} else if (secondFloorThirdRoom.contains(s.myPosition())) {
			if (e.debug)
				s.log("Second floor third room");
			if (reversed)
				openSecurityDoor(5201);
			else
				secondFloorThirdRoom();
		} else if (s.objects.closest("Rickety door") != null) {
			if (e.debug)
				s.log("Don't know where I am - defaulting by opening door");
			openSecurityDoor(0);
		} else if (firstFloorFirstRoom.contains(s.myPosition())) {
			firstFloorFirstRoom();
		} else if (firstFloorTreasureRoom.contains(s.myPosition())) {
			firstFloorTreasureRoom();
		} else if (!reversed) {
			walkToFirstFloor();
		} else {
			WebWalkEvent event = new WebWalkEvent(destination);
			s.execute(event);
		}

		return false;
	}

	private void secondFloorThirdRoomIntermediate() throws InterruptedException {
		if (reversed ? openSecurityDoor(5197) : openSecurityDoor(5195))
			;
	}

	private void secondFloorThirdRoom() throws InterruptedException {
		if (reversed ? openSecurityDoor(5195) : openSecurityDoor(5197))
			;

	}

	private void secondFloorSecondRoom() throws InterruptedException {
		int yvalue = reversed ? 5237 : 5203;
		RS2Object door = findDoor(yvalue);
		if (door == null || !door.isVisible()) {
			// s.walking.walk(new Position(2037, 5206, 0));
			WalkingEvent event = new WalkingEvent(reversed ? new Position(2045, 5223, 0) : new Position(2037, 5206, 0));
			event.setBreakCondition(new Condition() {
				@Override
				public boolean evaluate() {
					RS2Object door = findDoor(yvalue);
					return door != null && door.isVisible();
				}
			});
			if (e.debug)
				s.log("Walking");
			s.execute(event);
		}
		if (door != null) {
			openSecurityDoor(yvalue);
		}
	}

	private void secondFloorFirstRoomIntermediate() throws InterruptedException {
		if (reversed)
			openSecurityDoor(5239);
		else
			openSecurityDoor(5237);
	}

	private void secondFloorFirstRoom() throws InterruptedException {
		if (reversed) {
			RS2Object ladder = s.objects.closest("Ladder");
			if (ladder != null) {
				ladder.interact("Climb-up");
				Constants.condSleep(4600, 200, () -> firstFloorFirstRoom.contains(s.myPosition()));
			}
		} else
			openSecurityDoor(5239);
	}

	private void firstFloorTreasureRoom() throws InterruptedException {
		WalkingEvent event = new WalkingEvent(new Position(1902, 5221, 0));
		s.execute(event);
		@SuppressWarnings("unchecked")
		RS2Object ladder = s.objects.closest(new Filter<RS2Object>() {
			@Override
			public boolean match(RS2Object o) {
				return o != null && o.getName().equals("Ladder") && o.getX() < 1905;
			}
		});
		if (ladder != null) {
			ladder.interact("Climb-down");
			Constants.condSleep(4000, 750,
					() -> s.widgets.get(579, 17) != null || secondFloorFirstRoom.contains(s.myPosition()));
			Script.sleep(Script.random(400, 800));
			RS2Widget w = s.widgets.get(579, 17);
			if (w != null) {
				w.interact("Yes");
				Constants.condSleep(3400, 400, () -> secondFloorFirstRoom.contains(s.myPosition()));
			}
		}
	}

	private void firstFloorFirstRoom() {
		if (reversed) {
			RS2Object ladder = s.objects.closest("Ladder");
			if (ladder != null) {
				ladder.interact("Climb-up");
				Constants.condSleep(4600, 200, () -> !firstFloorFirstRoom.contains(s.myPosition()));
			}
		} else {
			RS2Object portal = s.objects.closest("Portal");
			if (portal != null) {
				portal.interact("Use");
				Constants.condSleep(5000, 755, () -> firstFloorTreasureRoom.contains(s.myPosition()));
			}
		}
	}

	private void walkToFirstFloor() {
		WebWalkEvent event = new WebWalkEvent(Constants.strongholdArea);
		event.setBreakCondition(new Condition() {
			@Override
			public boolean evaluate() {
				RS2Object ladder = s.objects.closest("Entrance");
				return ladder != null && s.map.distance(ladder.getPosition()) < 5;
			}
		});
		s.execute(event);
		RS2Object ladder = s.objects.closest("Entrance");
		if (ladder != null && s.map.canReach(ladder)) {
			ladder.interact("Climb-down");
			Constants.condSleep(4000, 700, () -> firstFloorFirstRoom.contains(s.myPosition()));
		}
	}

	private boolean openSecurityDoor(int y) throws InterruptedException {
		RS2Object door = findDoor(y);
		if (door != null) {
			if (!s.dialogues.isPendingContinuation()) {
				door.interact("Open");
				Constants.condSleep(6000, 880,
						() -> s.dialogues.isPendingContinuation() || s.myPlayer().getAnimation() == 4282);
				if (s.myPlayer().getAnimation() == 4282) {
					int tempY = s.myPlayer().getY();
					Constants.condSleep(1200, 300, () -> s.myPlayer().getY() != tempY);
					Script.sleep(Script.random(450, 700));
					if (e.debug)
						s.log("Musta been one of those non thingy ones");
					return true;
				}
			}
			RS2Widget w = s.widgets.get(231, 3);
			if (w != null && w.isVisible()) {
				String message = w.getMessage();
				if (message.contains("Don't forget to")) {
					if (e.debug)
						s.log("Skipping this to get to the real shit");
					s.dialogues.clickContinue();
					Script.sleep(Script.random(600, 1200));
					w = s.widgets.get(231, 3);
					if (w != null)
						message = w.getMessage();
				}
				int optionChoice = 0;
				for (String str : strongholdQuestions.keySet()) {
					if (message.contains(str)) {
						optionChoice = strongholdQuestions.get(str);
						if (e.debug)
							s.log("The message contains: " + str + ". So I'm chosing option " + optionChoice);
						break;
					}
				}
				if (optionChoice > 0) {
					s.dialogues.clickContinue();
					Constants.condSleep(3000, 400, () -> s.dialogues.isPendingOption());
					Script.sleep(Script.random(600, 1200));
					if (s.dialogues.isPendingOption()) {
						s.dialogues.selectOption(optionChoice);
						Constants.condSleep(3000, 200, () -> s.dialogues.isPendingContinuation());
						Script.sleep(Script.random(400, 700));
						if (s.dialogues.isPendingContinuation()) {
							int tempY = s.myPlayer().getY();
							s.dialogues.completeDialogue();
							Constants.condSleep(1200, 300, () -> s.myPlayer().getY() != tempY);
							Script.sleep(Script.random(750, 1200));
							return true;
						}
					}
				} else {
					if (e.debug)
						s.log("Don't know the answer to this one: " + message);
					s.dialogues.clickContinue();
					Script.sleep(Script.random(400, 800));
					openSecurityDoor(y);
				}
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private RS2Object findDoor(int y) {
		return s.objects.closest(new Filter<RS2Object>() {
			@Override
			public boolean match(RS2Object o) {
				return (o.getId() == 17100 || o.getId() == 17009) && (y > 0 ? o.getY() == y : true);
			}
		});
	}

}