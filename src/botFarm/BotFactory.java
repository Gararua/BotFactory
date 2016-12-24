package botFarm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

import javax.imageio.ImageIO;

import org.osbot.rs07.api.Quests.Quest;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import quests.CooksAssistant;
import quests.IslandSolver;
import quests.RomeoAndJuliet;
import quests.SheepShearer;

@ScriptManifest(author = "Auron", info = "A combat account starter, get required stats from tutorial island without having to lift a finger!", logo = "http://i.imgur.com/v8mU8rd.png", name = "Bot Factory", version = 1.0)
public class BotFactory extends Script {

	private double cursorCount, alpha;
	private final int radius = 6, TRAIL_SIZE = 50;
	private int index = 0, roomForQuestingAttempts = 0;
	private Point[] points;
	private String status = "";
	private ArrayList<Node> nodes;
	private boolean afk, started = false;
	public boolean needMoreMoney = false;
	private Fight fight;
	public boolean hidePaint = false, tutorial = false;
	private long startTime;
	private GUI2 gui;
	private Environment e;
	private BooleanSupplier chickensQuitCondition, cowsQuitCondition;

	@Override
	public void onStart() {
		e = new Environment();
		e.itemsToSell = new ArrayList<String>();
		e.itemsToBuy = new HashMap<String, Integer>();
		gui = new GUI2(this, e);
		gui.setVisible(true);
		startTime = System.currentTimeMillis();
		points = new Point[50];
		bot.addMouseListener(new Bml());
		reprofile();
	}

	public void changeStateNodes() {
		e.looting = false;
		status = "no status";
		nodes = new ArrayList<Node>();
		if (configs.get(281) < 1000) {
			nodes.add(new IslandSolver(this, e));
			tutorial = true;
			status = "Completing tutorial";
		} else if (e.questOnly) {
			if (quests.getQuestPoints() < 7)
				addQuestNodes();
			else {
				log("7 quest points reached, terminating script");
				stop();
			}
		} else if (!ultimateCutOff()) {
			e.lootLikelihood = needMoreMoney ? Script.random(5, 8) : Script.random(17, 24);
			if (!chickensQuitCondition.getAsBoolean()) {
				addChickenNodes();
			} else if (e.questing && quests.getQuestPoints() < 7) {
				addQuestNodes();
			} else if (needMoreMoney) {
				addCowsForMoneyNodes();
			} else if (!cowsQuitCondition.getAsBoolean()) {
				addCowNodes();
			} else {
				addFleshCrawlerNodes();
			}
		} else {
			log("All goal levels reached, terminating script");
			stop();
		}
	}

	private BooleanSupplier getQuitCondition(int attk, int str, int def) {
		e.trainAttkLvl = attk;
		e.trainStrLvl = str;
		e.trainDefLvl = def;
		return () -> skills.getStatic(Skill.ATTACK) >= attk && skills.getStatic(Skill.STRENGTH) >= str
				&& skills.getStatic(Skill.DEFENCE) >= def;
	}

	private void addCowsForMoneyNodes() {
		if (inventory.isFull() || !equipment.isWearingItem(EquipmentSlot.WEAPON)) {
			status = "Gearing up for cows";
			if (npcs.closest("Banker") == null) {
				nodes.add(new Walk(this, e, Constants.lummyBankPos));
			}
			nodes.add(new SimpleBank(this, e, true));
		} else if (e.cowhideLoot < e.cowhideLootCutOff) {
			if (e.debug)
				log("We don't have enough cowhides yet, we only have " + e.cowhideLoot + " (need " + e.cowhideLootCutOff
						+ ")");
			if (!e.loot.contains("Cowhide"))
				e.loot.add("Cowhide");
			e.npcs.clear();
			e.npcs.add("Cow calf");
			e.npcs.add("Cow");
			fight = new Fight(this, e, Constants.cowArea,
					() -> getQuitCondition(e.attkLvlCutOff, e.strLvlCutOff, e.defLvlCutOff).getAsBoolean()
							|| inventory.isFull() && (!inventory.contains("Bones") || !e.lootBones));
			status = "Killing Cows for money";
			nodes.add(fight);
			nodes.add(new Loot(this, e));
		} else {
			needMoreMoney = false;
			changeStateNodes();
		}
	}

	private void addCowNodes() {
		e.npcs.clear();
		e.npcs.add("Cow calf");
		e.npcs.add("Cow");
		addGear(true);
		if (checkGear() && !inventory.isFull() && equipment.isWearingItem(EquipmentSlot.WEAPON)) {
			status = "Killing Cows";
			if (e.cowhideLoot < e.cowhideLootCutOff) {
				if (e.debug)
					log("We don't have enough cowhides yet");
				fight = new Fight(this, e, Constants.cowArea,
						() -> getQuitCondition(e.cowsAttkCutOff, e.cowsStrCutOff, e.cowsDefCutOff).getAsBoolean()
								|| inventory.isFull() && (!inventory.contains("Bones") || !e.lootBones));
				nodes.add(new Loot(this, e));
			} else {
				if (e.debug)
					log("We have enough cowhides, let's just kill cows for xp");
				fight = new Fight(this, e, Constants.cowArea,
						() -> getQuitCondition(e.cowsAttkCutOff, e.cowsStrCutOff, e.cowsDefCutOff).getAsBoolean());
			}
			nodes.add(fight);
		} else {
			if (npcs.closest("Banker") == null) {
				if (checkEquipment())
					nodes.add(new Walk(this, e, Constants.lummyBankPos));
				else
					nodes.add(new Walk(this, e, Constants.grandExchangeArea));
			}
			status = "Gearing up for cows";
			nodes.add(new GrandExchangeHandler(this, e));
			nodes.add(new EquipmentManager(this, e));
		}

	}

	private void addChickenNodes() {
		status = "Killing chickens";
		e.lootLikelihood = Script.random(28, 40);
		e.loot.remove("Cowhide");
		e.npcs.clear();
		e.npcs.add("Chicken");
		Area tempArea;
		if (Constants.chickenArea.contains(myPosition())) {
			tempArea = Constants.chickenArea;
		} else if (Constants.chickenArea2.contains(myPosition())) {
			tempArea = Constants.chickenArea2;
		} else {
			tempArea = Script.random(10) > 4 ? Constants.chickenArea : Constants.chickenArea2;
		}
		fight = new Fight(this, e, tempArea,
				getQuitCondition(e.chickensAttkCutOff, e.chickensStrCutOff, e.chickensDefCutOff));
		nodes.add(fight);
		nodes.add(new Loot(this, e));
	}

	private void addFleshCrawlerNodes() {
		e.npcs.clear();
		e.npcs.add("Flesh Crawler");
		addGear(false);
		if (checkGear() && !Constants.cowArea.contains(myPosition())) {
			status = "Killing Flesh crawlers";
			nodes.add(new WalkToStronghold(this, e));
			fight = new Fight(this, e, Constants.fleshCrawlerArea,
					getQuitCondition(e.attkLvlCutOff, e.strLvlCutOff, e.defLvlCutOff));
			nodes.add(fight);
			nodes.add(new Loot(this, e));
		} else {
			if (npcs.closest("Banker") == null) {
				nodes.add(new WalkToStronghold(this, e, true,
						checkEquipment()
								? (e.preferredBank > 4 ? Constants.edgevilleBankArea : Constants.westVarrockBankArea)
								: Constants.grandExchangeArea));
			}
			status = "Gearing up for crawlers";
			nodes.add(new GrandExchangeHandler(this, e));
			nodes.add(new EquipmentManager(this, e));
		}
	}

	private void addQuestNodes() {
		log("Initialising Questing module");
		if (!quests.isComplete(Quest.COOKS_ASSISTANT)) {
			if ((inventory.contains("Bucket") || inventory.contains("Bucket of milk"))
					&& (inventory.contains("Pot") || inventory.contains("Pot of flour"))
					&& inventory.getEmptySlots() > 19) {
				nodes.add(new CooksAssistant(this, e));
				status = "Completing Cook's Assistant";
			} else {
				if (roomForQuestingAttempts > 3) {
					log("Pot or bucket cannot be found. Please ensure these are either on person, or in the bank! Exiting now...");
					stop();
				} else {
					roomForQuestingAttempts++;
					status = "Making room for questing";
					setTutorial(false);
				}
			}
		} else if (!quests.isComplete(Quest.SHEEP_SHEARER)) {
			nodes.add(new SheepShearer(this, e));
			status = "Completing Sheep Shearer";
		} else if (!quests.isComplete(Quest.ROMEO_JULIET)) {
			nodes.add(new RomeoAndJuliet(this, e));
			status = "Completing Romeo and Juliet";
		}
	}

	@Override
	public int onLoop() throws InterruptedException {
		if (started) {
			for (Node n : nodes) {
				if (n.validate()) {
					n.execute();
				}
			}
		}
		return 400;
	}

	public void reprofile() {
		e.lootLikelihood = Script.random(17, 23);
		e.rightClickLikelihood = Script.random(13, 21);
		e.hoverLikelihood = Script.random(5, 15);
		e.afkLikelihood = Script.random(17, 35);
		e.geOfferButtonsLikelihood = Script.random(1, 10);
		double eatL = skills.getStatic(Skill.HITPOINTS) * (Script.random(4, 7) * 0.1);
		e.eatLikelihood = (int) eatL;
		e.eatUpLikelihood = Script.random(10);
		e.reattackLikelihood = Script.random(9);
		e.preferredBank = Script.random(10);
		e.changeStyleLikelihood = Script.random(2, 5);
		e.minLevelGap = Script.gRandom(4, 1.25, 2, 8);
		if (!needMoreMoney)
			e.cowhideLootCutOff = Script.random(180, 230);
		if (e.debug) {
			log("Loot chance: " + e.lootLikelihood);
			log("Right click chance: " + e.rightClickLikelihood);
			log("Hover chance: " + e.hoverLikelihood);
			log("Eat chance: " + e.eatLikelihood);
		}
	}

	private boolean ultimateCutOff() {
		if (skills.getStatic(Skill.ATTACK) >= e.attkLvlCutOff) {
			if (skills.getStatic(Skill.STRENGTH) >= e.strLvlCutOff) {
				if (skills.getStatic(Skill.DEFENCE) >= e.defLvlCutOff) {
					return true;
				}
			}
		}
		return false;
	}

	public void afk() throws InterruptedException {
		String temp = status;
		status = "simulating afk";
		afk = true;
		if (random(5) > 1) {
			mouse.moveOutsideScreen();
		}
		int l = gRandom(8500, 10000, 5000, 80000);
		if (e.debug)
			log("Sleeping for " + l);
		log("Simulating AFK");
		sleep(l);
		status = temp;
		afk = false;
		// Mouse always comes on screen at 0,0 - this changes that
		if (!mouse.isOnScreen()) {
			if (Script.random(3) > 2) {
				bot.getMouseEventHandler().generateBotMouseEvent(MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
						Script.random(0, 750), -1, 0, false, 0, true);
			} else {
				bot.getMouseEventHandler().generateBotMouseEvent(MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0,
						-1, Script.random(0, 490), 0, false, 0, true);
			}
		}
	}

	/**
	 * Only checks supplies, because checking gear isn't essential.
	 *
	 * @return True if we have supplies on us
	 */
	private boolean checkGear() {
		for (String str : e.itemsToBring.keySet()) {
			if (!inventory.contains(Constants.getId(str))) {
				if (e.debug)
					log("Gear check failed, I don't have" + str);
				return false;
			}
		}
		return true;
	}

	private void addGear(boolean cows) {
		e.itemsToEquip = new HashMap<EquipmentSlot, String>();
		e.itemsToBring = new HashMap<String, Integer>();
		e.itemsToBuy = new HashMap<String, Integer>();
		String[] metals = { "Iron", "Steel", "Mithril", "Adamant", "Rune", "Rune", "Rune", "Rune", "Rune", "Rune",
				"Rune" };

		int level = getSkills().getStatic(Skill.ATTACK);
		e.itemsToEquip.put(EquipmentSlot.WEAPON,
				((level > 4 && cows && !needMoreMoney) ? "Steel" : metals[level / 10]) + " scimitar");
		if (!cows || needMoreMoney)
			e.itemsToEquip.put(EquipmentSlot.AMULET, "Amulet of strength");

		level = getSkills().getStatic(Skill.DEFENCE);
		String armourMetal = level > 4 && cows && !needMoreMoney ? "Steel" : metals[level / 10];
		if (armourMetal == "Adamant" || armourMetal == "Rune")
			armourMetal = "Mithril";
		String[] armour = { " full helm", " platebody", " platelegs", " kiteshield" };
		EquipmentSlot[] slots = { EquipmentSlot.HAT, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.SHIELD };
		for (int i = 0; i < 4; i++) {
			EquipmentSlot slot = slots[i];
			Item currentEquipped = equipment.getItemInSlot(slot.slot);
			if (currentEquipped != null) {
				String curName = currentEquipped.getName();
				if (curName.contains("Adamant")) {
					e.itemsToEquip.put(slot, "Adamant" + armour[i]);
				} else if (curName.contains("Rune")) {
					e.itemsToEquip.put(slot, "Rune" + armour[i]);
				} else {
					e.itemsToEquip.put(slot, armourMetal + armour[i]);
				}
			} else {
				e.itemsToEquip.put(slot, armourMetal + armour[i]);
			}
		}

		if (!e.defaultGear) {
			for (EquipmentSlot slot : e.customGear.keySet()) {
				String item = e.customGear.get(slot);
				if (item.equalsIgnoreCase("none")) {
					e.itemsToEquip.remove(slot);
				} else {
					if (e.itemsToEquip.replace(slot, item) == null) {
						e.itemsToEquip.put(slot, item);
					}
				}
			}
		}

		if (!needMoreMoney)
			e.itemsToBring.put(e.food, cows ? Script.random(2, 3) : 28);

		if (e.debug) {
			log("Items to equip: " + e.itemsToEquip);
			log("Items to bring: " + e.itemsToBring);
		}

	}

	public void start() throws InterruptedException {
		chickensQuitCondition = () -> skills.getStatic(Skill.ATTACK) >= e.chickensAttkCutOff
				&& skills.getStatic(Skill.STRENGTH) >= e.chickensStrCutOff
				&& skills.getStatic(Skill.DEFENCE) >= e.chickensDefCutOff;
		cowsQuitCondition = () -> skills.getStatic(Skill.ATTACK) >= e.cowsAttkCutOff
				&& skills.getStatic(Skill.STRENGTH) >= e.cowsStrCutOff
				&& skills.getStatic(Skill.DEFENCE) >= e.cowsDefCutOff;

		try {
			for (Skill skill : new Skill[] { Skill.ATTACK, Skill.STRENGTH, Skill.HITPOINTS, Skill.DEFENCE,
					Skill.PRAYER }) {
				getExperienceTracker().start(skill);
			}
			e.styles = new ArrayList<Integer>();
			e.npcs = new ArrayList<String>();
			e.npcs.add("Chicken");
			e.npcs.add("Cow");
			e.npcs.add("Cow calf");
			e.npcs.add("Flesh Crawler");
			e.loot = new ArrayList<String>();
			if (e.lootBones)
				e.loot.add("Bones");
			e.loot.add("Feather");
			e.loot.add("Cowhide");
			e.loot.add("Iron ore");
			e.loot.add("Silver bar");
			e.loot.add("Uncut sapphire");
			e.loot.add("Uncut emerald");
			e.loot.add("Uncut diamond");
			if (fight != null)
				fight.changeStyle();
			if (e.attkLvlCutOff > 1) {
				log("Training attack to " + e.attkLvlCutOff);
				e.styles.add(new Integer(0));
			}
			if (e.strLvlCutOff > 1) {
				log("Training strength to " + e.strLvlCutOff);
				e.styles.add(new Integer(1));
			}
			if (e.defLvlCutOff > 1) {
				log("Training defence to " + e.defLvlCutOff);
				e.styles.add(new Integer(3));
			}
			changeStateNodes();
			startTime = System.currentTimeMillis();
			started = true;
		} catch (Exception e) {
			log("Caught Exception in start");
			log(e.getLocalizedMessage());
			log(e.getStackTrace());
		}
	}

	/**
	 * Only called when we don't have enough money for supplies. Goes to kill
	 * cows until we have enough money
	 *
	 * @param m
	 *            how much gp is needed
	 */
	public void notEnoughMoney(int m) {
		needMoreMoney = true;
		int monz = (m < 4500) ? 4500 : m;
		e.cowhideLootCutOff = (monz / Constants.getPrice("Cowhide"));
		e.cowhideLoot = 0;
		log("I don't have enough money for either supplies or a weapon, which is crucial!\nNeed to kill cows for at least "
				+ e.cowhideLootCutOff + " cowhides");
		changeStateNodes();
	}

	/**
	 * Called once tutorial is completed, sorts out inventory.
	 *
	 * @param b
	 */
	public void setTutorial(boolean b) {
		tutorial = b;
		if (inventory.contains("Bronze sword"))
			inventory.interact("Wield", "Bronze sword");
		if (inventory.contains("Wooden shield"))
			inventory.interact("Wield", "Wooden shield");

		e.itemsToBring = new HashMap<String, Integer>();
		e.itemsToBring.put("Pot", new Integer(1));
		e.itemsToBring.put("Bucket", new Integer(1));
		nodes = new ArrayList<Node>();
		nodes.add(new SimpleBank(this, e, false));
		nodes.add(new Walk(this, e, Constants.lummyBankPos));
	}

	@Override
	public void onMessage(Message arg0) throws InterruptedException {
		if (!tutorial && fight != null) {
			String message = arg0.getMessage();
			if (message.contains("Oh dear")) {
				log("Well, this is embarrassing, we died.");
				Constants.condSleep(8000, 1000, () -> widgets.closeOpenInterface());
				changeStateNodes();
			} else if (message.contains("else")) {
				fight.nullTarget();
				fight.execute();
			} else if (message.contains("under")) {
				if (e.debug)
					log("What am I doooing");
				fight.underAttack(Script.random(6) < e.reattackLikelihood);
			} else if (message.contains("just advanced") && fight != null) {
				if (!chickensQuitCondition.getAsBoolean()) {
					Area tempArea = null;
					if (Constants.chickenArea.contains(myPosition()) && Script.random(13) > 11
							&& this.players.getAll().size() > 7) {
						tempArea = Constants.chickenArea;
					} else if (Constants.chickenArea2.contains(myPosition()) && Script.random(13) > 11
							&& this.players.getAll().size() > 13) {
						tempArea = Constants.chickenArea2;
					}
					if (tempArea != null) {
						nodes = new ArrayList<Node>();
						fight = new Fight(this, e, tempArea, chickensQuitCondition);
						nodes.add(fight);
						nodes.add(new Loot(this, e));
					}
				}
				fight.changeStyle();
			} else if (message.contains("You are not of sufficient experience to take the shortcut")) {
				log("Due to a recent update, pathing through the first floor is not currently available. "
						+ "Consider either completing the stronghold or going back to cows");
				e.cowsAttkCutOff = e.attkLvlCutOff;
				e.cowsStrCutOff = e.strLvlCutOff;
				e.cowsDefCutOff = e.defLvlCutOff;
				RS2Object ladder = objects.closest("Ladder");
				if (ladder != null) {
					ladder.interact("Climb-up");
					Constants.condSleep(4000, 300, () -> Constants.fleshCrawlerArea.contains(myPosition()));
					changeStateNodes();
				}
			}
		}
	}

	private final Color color1 = new Color(0, 0, 0);
	private final Color color2 = new Color(92, 0, 23);

	private final BasicStroke stroke1 = new BasicStroke(2);

	private final Font font2 = new Font("Calibri", 0, 27);
	private final Font font3 = new Font("Calibri", 0, 19);
	private final Font font4 = new Font("Calibri", 1, 15);

	private final Image img1 = getImage("http://i.imgur.com/UAqvzCv.png");

	@Override
	public void onPaint(Graphics2D g2) {
		g2.setColor(new Color(31, 31, 20, 45));
		if (afk)
			g2.fillRect(0, 0, 764, 502);
		if (fight != null && fight.getTarget() != null && fight.getTarget().getHealthPercent() > 0) {
			g2.setColor(new Color(255, 0, 0, 100));
			g2.fillPolygon(fight.getTarget().getPosition().getPolygon(getBot()));
			/*
			 * g2.drawString("Target exists: " + fight.target.exists(), 100,
			 * 120); g2.drawString("Target height: " + fight.target.getHeight(),
			 * 100, 140); g2.drawString("Target actions: ", 100, 160); int off =
			 * 0; try { String[] actions = fight.target.getActions(); for
			 * (String str : actions) { g2.drawString(str == null ? "null" :
			 * str, 100, 180 + off * 20); off++; } } catch (Exception e) {
			 * g2.drawString("null", 100, 180); }
			 */
		}
		Point pos = mouse.getPosition();
		points[index++] = pos;
		index %= TRAIL_SIZE;
		alpha = 0;
		for (int i = index; i != (index == 0 ? TRAIL_SIZE - 1 : index - 1); i = (i + 1) % TRAIL_SIZE) {
			if (points[i] != null && points[(i + 1) % TRAIL_SIZE] != null) {
				g2.setColor(new Color(255, 255, 255, (int) alpha));
				g2.drawLine(points[i].x, points[i].y, points[(i + 1) % TRAIL_SIZE].x, points[(i + 1) % TRAIL_SIZE].y);

				alpha += 4;
			}
		}
		g2.setColor(Color.RED);
		g2.drawOval(pos.x - radius, pos.y - radius, 12, 12);
		g2.setColor(Color.WHITE);
		int x1 = pos.x + (int) (radius * Math.cos(cursorCount));
		int y1 = pos.y + (int) (radius * Math.sin(cursorCount));
		int x2 = pos.x + (int) (radius * Math.cos(Math.PI + cursorCount));
		int y2 = pos.y + (int) (radius * Math.sin(Math.PI + cursorCount));
		g2.drawLine(x1, y1, x2, y2);
		int x3 = pos.x + (int) (radius * Math.cos(cursorCount + Math.PI / 2));
		int y3 = pos.y + (int) (radius * Math.sin(cursorCount + Math.PI / 2));
		int x4 = pos.x + (int) (radius * Math.cos(Math.PI * (1.5) + cursorCount));
		int y4 = pos.y + (int) (radius * Math.sin(Math.PI * (1.5) + cursorCount));
		g2.drawLine(x3, y3, x4, y4);
		cursorCount += 0.08D;

		long runTime = System.currentTimeMillis() - startTime;
		if (!hidePaint) {
			g2.drawImage(img1, 0, 277, null);
			g2.setFont(font2);
			g2.setColor(color1);
			g2.drawString(formatTime(runTime), 369, 464);
			if (status.length() > 20)
				g2.setFont(new Font("Calibri", 0, 20));
			g2.drawString("Status: " + status, 8, 369);

			g2.setFont(font4);
			g2.setStroke(stroke1);
			int i = 0;
			if (e.attkLvlCutOff > 1) {
				drawExperienceBar(g2, Skill.ATTACK, new Color(204, 0, 0), i);
				i++;
			}
			if (e.strLvlCutOff > 1) {
				drawExperienceBar(g2, Skill.STRENGTH, new Color(0, 153, 0), i);
				i++;
			}
			if (e.defLvlCutOff > 1) {
				drawExperienceBar(g2, Skill.DEFENCE, new Color(0, 102, 255), i);
				i++;
			}
			drawExperienceBar(g2, Skill.HITPOINTS, Color.WHITE, i);
		}
		g2.setFont(font3);
		g2.setColor(color2);
		g2.fillRoundRect(402, 479, 115, 22, 16, 16);
		g2.setColor(color1);
		g2.drawRoundRect(402, 479, 115, 22, 16, 16);
		g2.drawString(hidePaint ? "Show paint" : "Hide paint", 420, 497);
	}

	private void drawExperienceBar(Graphics2D g2, Skill skill, Color c, int number) {
		g2.setColor(c);
		g2.fillRoundRect(6, 377 + 25 * number, (int) (306 * (percentToNextLevel(skill) / 100)), 20, 16, 16);

		g2.setColor(color1);
		g2.drawRoundRect(6, 377 + 25 * number, 310, 20, 16, 16);

		g2.setColor(color1);
		g2.drawString((int) percentToNextLevel(skill) + "% to level " + (getSkills().getStatic(skill) + 1) + " ("
				+ getExperienceTracker().getGainedLevels(skill) + " levels gained)", 72, 392 + 25 * number);

	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}

	public final double percentToNextLevel(final Skill skill) {
		int curLvl = getSkills().getStatic(skill), curXP = getSkills().getExperience(skill),
				xpCurLvl = getSkills().getExperienceForLevel(curLvl),
				xpNextLvl = getSkills().getExperienceForLevel(curLvl + 1);

		return (((curXP - xpCurLvl) * 100) / (xpNextLvl - xpCurLvl));
	}

	public String formatTime(long ms) {
		long s = ms / 1000, m = s / 60, h = m / 60;
		s %= 60;
		m %= 60;
		h %= 24;
		return String.format("%02d:%02d:%02d", h, m, s);
	}

	private boolean checkEquipment() {
		for (EquipmentSlot slot : e.itemsToEquip.keySet()) {
			if (!equipment.isWearingItem(slot) || !equipment.isWearingItem(slot, e.itemsToEquip.get(slot))) {
				return false;
			}
		}
		return true;
	}

	private class Bml implements BotMouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			Rectangle rekt = new Rectangle(402, 479, 115, 22);
			if (rekt.contains(e.getPoint()))
				hidePaint = !hidePaint;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public boolean blockInput(Point arg0) {
			return false;
		}

	}
}
