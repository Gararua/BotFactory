package quests;

import java.util.ArrayList;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.event.WebWalkEvent;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

import botFarm.BotFactory;
import botFarm.Constants;
import botFarm.Constants.MYTABS;
import botFarm.Environment;
import botFarm.Node;


public class IslandSolver extends Node {

	private boolean debug = false;
	private int id;

	public IslandSolver(Script s, Environment e) {
		super(s, e);
		debug = e.debug;
	}

	@Override
	public boolean validate() throws InterruptedException {
		return true;
	}

	@Override
	public boolean execute() throws InterruptedException {
		id = s.configs.get(281);
		if (debug)
			s.log("Checking id " + id);
		switch (id) {
		case 1000:
			straightOutaTutIsland();
			break;
		case 0:
		case 3:
		case 7:
		case 10:
			startRoom();
			break;
		case 20:
		case 30:
		case 40:
		case 50:
		case 60:
		case 70:
			chopAndFire();
			break;
		case 80:
		case 90:
		case 110:
		case 120:
			fishAndCook();
			break;
		case 130:
		case 140:
		case 150:
		case 160:
		case 170:
			masterChef();
			break;
		case 180:
		case 183:
		case 187:
		case 190:
		case 210:
			emotes();
			break;
		case 220:
		case 230:
		case 240:
		case 250:
			quests();
			break;
		case 260:
		case 270:
		case 280:
		case 290:
		case 300:
		case 310:
			miner();
			break;
		case 320:
		case 330:
		case 340:
		case 350:
		case 360:
			smith();
			break;
		case 370:
		case 390:
		case 400:
		case 405:
		case 410:
		case 420:
		case 430:
			combatIntro();
			break;
		case 440:
		case 450:
		case 460:
		case 470:
			ratMelee();
			break;
		case 480:
		case 490:
		case 500:
			ratRange();
			break;
		case 510:
		case 520:
		case 525:
			bankTutor();
			break;
		case 530:
		case 540:
			financialAdvisor();
			break;
		case 550:
		case 560:
		case 570:
		case 580:
		case 590:
		case 600:
		case 610:
			prayer();
			break;
		case 620:
		case 630:
		case 640:
		case 650:
		case 670:
			magic();
			break;
		}

		return false;
	}

	/**
	 * Wields sword and shield and sets tutorial mode off
	 * 
	 * @throws InterruptedException
	 */
	private void straightOutaTutIsland() throws InterruptedException {
		if (debug)
			s.log("I DID IT");
		s.inventory.interact("Wield", 1277);
		s.inventory.interact("Wield", 1171);
		Script.sleep(Script.random(200, 350));
		((BotFactory) s).setTutorial(false);
	}

	private void magic() throws InterruptedException {
		s.walking.walk(new Position(3139, 3087, 0));
		if (id == 620)
			talk("Magic Instructor");
		if (id == 630)
			clickTab(MYTABS.MAGIC);
		if (id == 640)
			talk("Magic Instructor");
		if (id == 650) {
			s.magic.castSpellOnEntity(Spells.NormalSpells.WIND_STRIKE, s.npcs.closest("Chicken"));
			Script.sleep(800);
		}
		if (id == 670) {
			keepClickingContinue();
			s.npcs.closest("Magic Instructor").interact("Talk-to");
			Constants.condSleep(8000, 300, () -> s.dialogues.isPendingContinuation());
			s.dialogues.completeDialogue("Yes");
		}
	}

	private void prayer() throws InterruptedException {
		if (id == 550) {
			talk("Brother Brace");
			s.walking.walk(new Position(3130, 3107, 0));
			if (!s.map.canReach(s.npcs.closest("Brother Brace"))) {
				s.objects.closest("Large door").interact("Open");
				Constants.condSleep(8500, 300, () -> s.map.canReach(s.npcs.closest("Brother Brace")));
			}
		}
		if (id == 560)
			clickTab(MYTABS.PRAYER);
		if (id == 570)
			talk("Brother Brace");
		if (id == 580)
			clickTab(MYTABS.FRIENDS);
		if (id == 590)
			clickTab(MYTABS.IGNORE);
		if (id == 600)
			talk("Brother Brace");
		if (id == 610) {
			s.objects.closest("Door").interact("Open");
			Constants.condSleep(3000, 400, () -> s.configs.get(281) != 610);
		}
	}

	private void financialAdvisor() throws InterruptedException {
		talk("Financial advisor");
		@SuppressWarnings("unchecked")
		RS2Object door = s.objects.closest(new Filter<RS2Object>() {
			@Override
			public boolean match(RS2Object d) {
				return d.getName().equals("Door") && d.getX() == 3130;
			}
		});
		if (door != null)
			door.interact("Open");
		Constants.condSleep(10000, 300, () -> s.myPosition().equals(new Position(3130, 3124, 0)));
	}

	private void bankTutor() throws InterruptedException {
		if (id == 510) {
			s.walking.walk(new Position(3122, 3122, 0));
			RS2Object o = s.objects.closest("Bank booth");
			if (o != null) {
				o.interact("Use");
				Script.sleep(400);
				Constants.condSleep(4000, 300, () -> s.dialogues.isPendingContinuation() || s.bank.isOpen());
				if (!s.bank.isOpen()) {
					s.dialogues.clickContinue();
					Constants.condSleep(1700, 500, () -> s.dialogues.isPendingOption());
					if (s.dialogues.isPendingOption())
						s.dialogues.selectOption(1);
				}
				Constants.condSleep(1000, 244, () -> s.bank.isOpen());
				Script.sleep(Script.random(400, 800));
			}
		}

		if (id == 520) {
			s.objects.closest("Poll booth").interact("Use");
			Constants.condSleep(4000, 300, () -> s.dialogues.isPendingContinuation());
			keepClickingContinue();
		}

		if (id == 525) {
			@SuppressWarnings("unchecked")
			RS2Object door = s.objects.closest(new Filter<RS2Object>() {
				@Override
				public boolean match(RS2Object d) {
					return d.getName().equals("Door") && d.getX() == 3125;
				}
			});
			if (door != null && door.interact("Open")) {
				Constants.condSleep(10000, 300, () -> s.myPosition().equals(new Position(3125, 3124, 0)));
			}
		}
	}

	private void ratRange() throws InterruptedException {
		if (s.inventory.contains(882)) {
			s.inventory.interact("Wield", 882);
			Script.sleep(400);
		}
		if (s.inventory.contains(841)) {
			s.inventory.interact("Wield", 841);
		}
		if (id == 490 || id == 480) {
			@SuppressWarnings("unchecked")
			NPC rat = s.getNpcs().closest(new Filter<NPC>() {
				@Override
				public boolean match(NPC npc) {
					return npc.getName().equals("Giant rat") && !npc.isUnderAttack() && (npc.getHealthPercent() > 0)
							&& npc.getInteracting() != s.myPlayer() && npc.getX() > 3105;
				}
			});
			if (rat != null) {
				rat.interact("Attack");

				Constants.condSleep(40000, 300, () -> rat.getHealthPercent() < 1);
				Script.sleep(800);
			}
		} else if (id == 500) {
			s.walking.walk(new Position(3111, 9525, 0));
			RS2Object ladder = s.objects.closest("Ladder");
			if (ladder != null) {
				ladder.interact("Climb-up");
			}
			Constants.condSleep(8500, 300, () -> new Area(3110, 3127, 3112, 3125).contains(s.myPlayer()));
		}
	}

	private void ratMelee() throws InterruptedException {
		NPC rat = s.npcs.closest("Giant rat");
		if (rat != null && !s.map.canReach(rat)) {
			s.walking.walk(new Position(3112, 9519, 0));
			s.objects.closest("Gate").interact("Open");
			new ConditionalSleep(8500, 300) {
				@Override
				public boolean condition() throws InterruptedException {
					return s.hintArrow.getNPC() != null && s.hintArrow.getNPC().getName().equals("Giant rat");
				}
			}.sleep();
		}

		while (s.hintArrow.getNPC() != null && s.hintArrow.getNPC().getName().equals("Giant rat")) {
			@SuppressWarnings("unchecked")
			NPC rat1 = s.getNpcs().closest(new Filter<NPC>() {

				@Override
				public boolean match(NPC npc) {
					return npc.getName().equals("Giant rat") && s.map.canReach(npc) && !npc.isUnderAttack()
							&& (npc.getHealthPercent() > 0) && npc.getInteracting() != s.myPlayer();
				}
			});
			if (rat1 != null) {
				rat1.interact("Attack");
				while (rat1.getHealthPercent() > 0) {
					if (s.skills.getDynamic(Skill.HITPOINTS) > 0 && s.skills.getDynamic(Skill.HITPOINTS) < 4) {

						Item bread = s.inventory.getItem("Bread");
						if (bread == null)
							bread = s.inventory.getItem("Shrimps");
						if (bread != null)

						{
							bread.interact("Eat");
						}

					}
					Script.sleep(400);
				}
				Script.sleep(800);
			}
		}
		if (s.hintArrow.getNPC() != null && s.hintArrow.getNPC().getName().equals("Combat Instructor")) {
			if (!s.map.canReach(s.npcs.closest("Combat Instructor"))) {
				s.objects.closest("Gate").interact("Open");
				Script.sleep(1000);
			}
			s.walking.walk(new Position(3106, 9509, 0));
			talk("Combat Instructor");
		}
	}

	private void combatIntro() throws InterruptedException {
		s.walking.walk(new Position(3106, 9509, 0));
		if (id == 370)
			talk("Combat Instructor");
		if (id == 390)
			clickTab(MYTABS.ARMOUR);
		Script.sleep(Script.random(200, 400));
		if (id > 390) {
			// Should really split this up into config checks
			ArrayList<RS2Widget> list = new ArrayList<RS2Widget>(
					s.widgets.containingActions(387, "View Equipment stats"));
			if (list.size() > 0) {
				list.get(0).interact("View equipment stats"); // 400
				Script.sleep(Script.random(200, 300));
				s.inventory.interact("Wield", 1205); // 405
				Script.sleep(Script.random(700, 950));
				s.widgets.closeOpenInterface();
				talk("Combat Instructor"); // 410
				s.inventory.interact("Wield", 1277); // 420
				s.inventory.interact("Wield", 1171);
				Script.sleep(800);
				clickTab(MYTABS.FIGHTSTYLE); // 430
			}
		}
	}

	private void smith() throws InterruptedException {
		if (s.inventory.contains("Tin ore")) {
			if (useWithObject("Tin ore", "Furnace")) {
				talk("Mining Instructor");
			}
		}
		if (s.inventory.contains("Bronze bar")) {
			if (useWithObject("Bronze bar", "Anvil")) {
				ArrayList<RS2Widget> list = new ArrayList<RS2Widget>(s.widgets.containingActions(312, "Smith 1"));
				if (list.size() > 0) {
					if (list.get(0).interact("Smith 1")) {
						Constants.condSleep(30000, 300, () -> s.inventory.contains("Bronze dagger"));
					}
				}
			}
		}
		if (s.inventory.contains("Bronze dagger")) {
			s.walking.walk(new Position(3093, 9502, 0));
			RS2Object gate = s.objects.closest("Gate");
			if (gate != null) {
				gate.interact("Open");
				Constants.condSleep(8500, 300, () -> s.myPosition().getX() == 3095);
			}
		}
	}

	private boolean useWithObject(String s1, String s2) throws InterruptedException {
		Item ore = s.inventory.getItem(s1);
		if (ore != null) {
			ore.interact("Use");
			Constants.condSleep(1000, 100, () -> s.getInventory().isItemSelected());
			if (s.getInventory().isItemSelected()) {
				s.objects.closest(s2).interact("use");
				Constants.condSleep(8500, 300, () -> !s.inventory.contains(s1) || s.widgets.isVisible(312));
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private void miner() throws InterruptedException {

		Filter<RS2Object> tinFilter = new Filter<RS2Object>() {
			@Override
			public boolean match(RS2Object ob) {
				return ob != null && ob.getName().equals("Rocks") && ob.getX() < 3079;
			}
		};
		Filter<RS2Object> copperFilter = new Filter<RS2Object>() {
			@Override
			public boolean match(RS2Object ob) {
				return ob != null && ob.getName().equals("Rocks") && ob.getX() > 3079;
			}
		};

		if (s.tabs.getOpen() != Tab.INVENTORY)
			s.tabs.open(Tab.INVENTORY);
		switch (id) {
		case 260:
			s.walking.walk(new Position(3081, 9509, 0));
			talk("Mining Instructor");
			break;
		case 270:
			RS2Object tinRock = s.objects.closest(tinFilter);
			if (tinRock != null)
				tinRock.interact("Prospect");
			Constants.condSleep(8500, 300, () -> s.getWidgets().getWidgetContainingText("contains tin") != null);
			Script.sleep(Script.random(300, 450));
			break;
		case 280:
			RS2Object copperRock = s.objects.closest(copperFilter);
			if (copperRock != null)
				copperRock.interact("Prospect");
			Constants.condSleep(8500, 300, () -> s.getWidgets().getWidgetContainingText("contains copper") != null);
			Script.sleep(Script.random(300, 450));
			break;
		case 290:
			talk("Mining Instructor");
			break;
		case 300:
			RS2Object tinRock2 = s.objects.closest(tinFilter);
			if (tinRock2 != null) {
				tinRock2.interact("Mine");
				Constants.condSleep(8500, 300, () -> s.inventory.contains("Tin ore") || !tinRock2.exists());
				Script.sleep(Script.random(300, 450));
			}
			break;
		case 310:
			RS2Object copperRock2 = s.objects.closest(copperFilter);
			if (copperRock2 != null) {
				copperRock2.interact("Mine");
				Constants.condSleep(8500, 300, () -> s.inventory.contains("Copper ore") || !copperRock2.exists());
				Script.sleep(Script.random(300, 450));
			}
			break;
		}
	}

	private void quests() throws InterruptedException {
		switch (id) {
		case 220:
			talk("Quest Guide");
			break;
		case 230:
			clickTab(MYTABS.QUEST);
			Script.sleep(Script.random(300, 600));
			s.tabs.open(Tab.INVENTORY);
			break;
		case 240:
			talk("Quest Guide");
			break;
		case 250:
			RS2Object ladder = s.objects.closest("Ladder");
			if (ladder != null) {
				if (!ladder.isVisible())
					s.camera.toEntity(ladder);
				ladder.interact("Climb-down");
				Constants.condSleep(10000, 300, () -> s.myPosition().equals(new Position(3088, 9520, 0)));
			}
			break;
		}
	}

	private void emotes() throws InterruptedException {
		if (s.npcs.closest("Master Chef") != null && s.map.canReach(s.npcs.closest("Master Chef"))) {
			s.walking.walk(new Position(3074, 3090, 0));
			@SuppressWarnings("unchecked")
			RS2Object door = s.objects.closest(new Filter<RS2Object>() {
				@Override
				public boolean match(RS2Object ob) {
					return ob != null && ob.getName().equals("Door") && ob.getY() == 3090;
				}
			});
			if (door != null) {
				door.interact("Open");
			}
			Constants.condSleep(10000, 300, () -> s.myPosition().equals(new Position(3072, 3090, 0)));
		}
		clickTab(MYTABS.EMOTES);
		Script.sleep(Script.random(90, 180));
		ArrayList<RS2Widget> list = new ArrayList<RS2Widget>(s.widgets.containingActions(216, "Jig"));
		if (list.size() > 0) {
			list.get(0).interact("Jig");
			Script.sleep(Script.random(90, 180));
			clickTab(MYTABS.OPTIONS);
			list = new ArrayList<RS2Widget>(s.widgets.containingActions(261, "Toggle Run"));
			if (list.size() > 0) {
				list.get(0).interact("Toggle Run");
				WebWalkEvent event = new WebWalkEvent(new Position(3086, 3128, 0));
				s.execute(event);
				if (s.objects.closest("Door").interact("Open"))
					Constants.condSleep(10000, 300, () -> s.myPosition().equals(new Position(3086, 3125, 0)));
			}
		} else {
			s.log("Doesn't exist m8");
		}
	}

	@SuppressWarnings("unchecked")
	private void masterChef() throws InterruptedException {
		if (!s.map.canReach(s.npcs.closest("Master Chef"))) {
			s.walking.walk(new Position(3080, 3084, 0));
			RS2Object door = s.objects.closest(new Filter<RS2Object>() {
				@Override
				public boolean match(RS2Object ob) {
					return ob != null && ob.getName().equals("Door") && ob.getY() == 3084;
				}
			});
			if (door != null) {
				door.interact("Open");
			}
			Constants.condSleep(10000, 300, () -> s.myPosition().equals(new Position(3078, 3084, 0)));
		} else {
			talk("Master Chef");
			if (!s.inventory.contains("Bread")) {
				if (!s.inventory.contains("Bread dough")) {
					Item flour = s.inventory.getItem("Pot of flour");
					Item water = s.inventory.getItem("Bucket of water");
					if (flour != null) {
						flour.interact("Use");
						Script.sleep(200);
						water.interact("Use");

						Constants.condSleep(30000, 300, () -> s.inventory.contains("Bread dough"));
						Script.sleep(500);
					}
				}
				if (s.inventory.contains("Bread dough")) {
					Item dough = s.inventory.getItem("Bread dough");
					if (dough != null) {
						dough.interact("Use");
						Script.sleep(500);
						if (s.getInventory().isItemSelected()) {
							s.objects.closest("Range").interact("use");
							Constants.condSleep(8500, 300, () -> s.inventory.contains("Bread", "Burnt bread"));
							if (!s.inventory.contains("Bread"))
								talk("Master Chef");
						}
					}
				}
			}
			Script.sleep(850);
			clickTab(MYTABS.MUSIC);
		}
	}

	private void chopAndFire() throws InterruptedException {
		switch (id) {
		case 20:
			s.settings.setRunning(true);
			Script.sleep(300);
			s.walking.walk(s.hintArrow.getEntity());
			talk("Survival expert");
			break;
		case 30:
			clickTab(MYTABS.INVENTORY);
			break;
		case 40:
			chopTree();
			break;
		case 50:
			makeFire();
			break;
		case 60:
			clickTab(MYTABS.LEVEL);
			Script.sleep(Script.random(300, 500));
			s.tabs.open(Tab.INVENTORY);
			break;
		case 70:
			talk("Survival expert");
			break;
		}
	}

	private void fishAndCook() throws InterruptedException {
		if (id != 120) {
			if (!s.inventory.contains("Raw shrimps"))
				fishShrimp();
			if (s.objects.closest("Fire") == null || !s.objects.closest("Fire").exists()) {
				chopTree();
				makeFire();
			}
			if (!s.inventory.contains("Shrimps"))
				cookShrimp();
		} else {
			s.walking.walk(new Position(3092, 3092, 0));
			if (s.objects.closest("Gate").interact("Open")) {
				Constants.condSleep(8500, 300, () -> s.myPosition().getX() == 3089);
			}
		}
	}

	private void cookShrimp() throws InterruptedException {
		Item shrimp = s.inventory.getItem("Raw shrimps");
		if (shrimp != null) {
			shrimp.interact("Use");
			Script.sleep(500);
			if (s.getInventory().isItemSelected()) {
				s.objects.closest("Fire").interact("use");
				Constants.condSleep(8500, 300, () -> !s.inventory.contains("Raw shrimps"));
				Script.sleep(500);
				if (!s.inventory.contains("Shrimps"))
					fishAndCook();
			}
		}
	}

	private void fishShrimp() throws InterruptedException {
		NPC spot = s.npcs.closest("Fishing spot");
		if (spot != null) {
			spot.interact("Net");
			Script.sleep(500);
			Constants.condSleep(8500, 300, () -> s.inventory.contains("Raw shrimps") || spot == null);
		}
		if (!s.inventory.contains("Raw shrimps"))
			fishShrimp();
	}

	private void makeFire() throws InterruptedException {
		Item logs = s.inventory.getItem("Logs");
		Item box = s.inventory.getItem("Tinderbox");
		Position p = s.myPosition();
		if (logs != null) {
			logs.interact("Use");
			Script.sleep(200);
			box.interact("Use");
			Script.sleep(400);
		}
		RS2Widget w = s.getWidgets().getWidgetContainingText("light a fire here");
		if (w != null) {
			s.walking.walk(new Area(3100, 3098, 3104, 3094));
			Script.sleep(200);
			makeFire();
		} else {
			Constants.condSleep(8500, 300, () -> !p.equals(s.myPosition()));
			Script.sleep(600);
		}
	}

	private void chopTree() {
		if (!s.inventory.contains("Logs")) {
			RS2Object tree = s.objects.closest("Tree");
			if (tree != null) {
				tree.interact("Chop down");
				Constants.condSleep(8500, 300, () -> s.inventory.contains("Logs") || !tree.exists());
			}
			if (!s.inventory.contains("Logs"))
				chopTree();
		}
	}

	private void startRoom() throws InterruptedException {
		switch (id) {
		case 0:
			if (s.widgets.isVisible(269)) {
				randomiseChar();
			}
			talk("RuneScape Guide");
			break;
		case 3:
			openOptions();
			break;
		case 7:
			talk("RuneScape Guide");
			break;
		case 10:
			openDoor();
			break;
		}

	}

	private void openDoor() throws InterruptedException {
		RS2Object gate = s.objects.closest("Door");
		if (gate != null) {
			if (gate.interact("Open"))
				Constants.condSleep(10000, 300, () -> s.myPosition().equals(new Position(3098, 3107, 0)));
		}
	}

	private void talk(String name) throws InterruptedException {
		NPC tutor = s.getNpcs().closest(name);
		if (tutor != null) {
			tutor.interact("Talk-to");
			Constants.condSleep(8500, 1000, () -> s.widgets.getWidgetContainingText("Click here to continue") != null
					|| s.widgets.getWidgetContainingText(162, "Click to continue") != null);
			keepClickingContinue();
		}
	}

	/**
	 * This method keeps clicking continue. The reason why
	 * dialogues.completeDialogue() cannot be used is that the tutorial island
	 * help boxes are considered to be still in a dialogue, so
	 * completeDialogue() will get stuck. Also the dialogue.clickContinue() will
	 * not work on the unique messages such as 'This rock contains Tin!', and so
	 * the bot will get stuck.
	 * 
	 * This is almost a messy work around with a timeout. Effectively it clicks
	 * 'Click to here continue' until it can't see another
	 * 
	 * @throws InterruptedException
	 */
	private void keepClickingContinue() throws InterruptedException {
		RS2Widget w = s.widgets.getWidgetContainingText("Click here to continue");
		int timeout = 0;
		while (timeout < 3) {
			if (w != null && w.isVisible()) {
				if (!w.getBounds().contains(s.mouse.getPosition())) {
					w.hover();
				}
				s.mouse.click(false);
				timeout = 0;
			}
			Script.sleep(800);
			w = s.widgets.getWidgetContainingText("Click here to continue");
			if (w == null || w.isHidden())
				// getWidgetContainingText() ignores the chatbox by default (for
				// obvious reasons), so you have to specify look in the chatbox
				// (i.e parent widget 162)
				w = s.widgets.getWidgetContainingText(162, "Click to continue");
			timeout++;
			if (s.myPlayer().isMoving())
				timeout = 0;
		}
	}

	private void openOptions() throws InterruptedException {
		clickTab(Constants.MYTABS.OPTIONS);
		ArrayList<RS2Widget> list = new ArrayList<RS2Widget>(s.widgets.containingActions(261, "Audio"));
		if (list.size() == 1) {
			list.get(0).interact("Audio");
			Script.sleep(Script.random(200, 300));
		}

		list = new ArrayList<RS2Widget>(s.widgets.containingActions(261, "Adjust Music Volume"));
		if (list.size() > 0) {
			list.get(0).interact("Adjust Music Volume");
			Script.sleep(Script.random(60, 180));
		}
		list = new ArrayList<RS2Widget>(s.widgets.containingActions(261, "Adjust Sound Effect Volume"));
		if (list.size() > 1) {
			list.get(0).interact("Adjust Sound Effect Volume");
			Script.sleep(Script.random(60, 180));
		}
		list = new ArrayList<RS2Widget>(s.widgets.containingActions(261, "Adjust Area Sound Effect Volume"));
		if (list.size() > 1) {
			list.get(0).interact("Adjust Area Sound Effect Volume");
			Script.sleep(Script.random(60, 180));
		}
	}

	private void randomiseChar() throws InterruptedException {
		if (debug)
			s.log("Randomising Character");

		String[] widgetStrings = new String[] { "Change head", "Change jaw", "Change torso", "Change arms",
				"Change hands", "Change legs", "Change feet", "Recolour hair", "Recolour torso", "Recolour legs",
				"Recolour feet", "Recolour skin" };
		for (String str : widgetStrings) {
			for (RS2Widget w : s.widgets.getWidgets(269)) {
				if (w.getToolTip().equals(str)) {
					if (w != null && Script.random(5) > 2) {
						w.hover();
						int j = Script.random(1, 13);
						for (int k = 0; k < j; k++) {
							s.mouse.click(false);
							// what I found my personal spam click speed to be
							Script.sleep(Script.gRandom(206, 14.0));
						}
						Script.sleep(Script.random(200, 500));
					}
				}
			}
		}
		Script.sleep(Script.random(600, 900));
		RS2Widget w = s.getWidgets().getWidgetContainingText("Accept");
		if (w != null)
			w.interact("Accept");
	}

	private void clickTab(Constants.MYTABS tab) throws InterruptedException {
		RectangleDestination dest = new RectangleDestination(s.bot, tab.getValue());
		s.mouse.click(dest);
		Script.sleep(Script.random(400, 600));
	}
}
