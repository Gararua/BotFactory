package botFarm;

import java.awt.Rectangle;
import java.util.function.BooleanSupplier;

import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.Player;
import org.osbot.rs07.api.model.RS2Object;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Spells;
import org.osbot.rs07.api.ui.Tab;
import org.osbot.rs07.input.mouse.EntityDestination;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class Fight extends Node {

	private NPC target;
	private int attempts, maxHit, foodHealAmount = 7, healthEat, failSafe = 0;
	private boolean debug = e.debug;
	private Area killArea;
	private BooleanSupplier stopCondition;

	public Fight(Script s, Environment e, Area killArea, BooleanSupplier stopCondition) {
		super(s, e);
		this.killArea = killArea;
		this.stopCondition = stopCondition;
		int strlvl = s.skills.getStatic(Skill.STRENGTH);
		maxHit = strlvl / 6 + 1;
		healthEat = Script.gRandom(e.eatLikelihood, 0.5, 7, s.skills.getStatic(Skill.HITPOINTS) - 12);
		if (debug) {
			s.log("Max hit: " + maxHit);
			s.log("Initial healtheat: " + healthEat);
		}
		changeStyle();
	}

	@Override
	public boolean validate() throws InterruptedException {
		if (stopCondition.getAsBoolean() || failSafe > 50) {
			((BotFactory) s).changeStateNodes();
			return false;
		}
		return !e.looting;
	}

	@Override
	public boolean execute() throws InterruptedException {
		if (!s.combat.isAutoRetaliateOn())
			s.combat.toggleAutoRetaliate(true);
		
		if (killArea.contains(s.myPosition())) {
			if (target != null && target.getHealthPercent() == 0){
				target = null;
			} else if (target != null && s.myPlayer().isMoving()) {
				if (checkAllPlayers()) {
					NPC chicken = findNPC(target);
					target = null;
					attackNPC(chicken);
				}
			} else if (target == null || s.myPlayer().getInteracting() == null || !s.myPlayer().isUnderAttack()) {
				if (!underAttack(Script.random(10) < e.reattackLikelihood)) {
					findAndKill();
				}
			} else if (s.myPlayer().isUnderAttack()) {
				Script.sleep(Script.random(500, 700));
				underAttack();
			}
			if (Script.random(20) == 2 && s.camera.getPitchAngle() < 54) {
				if (debug)
					s.log("Moving Camera");
				s.camera.toTop();
				s.camera.moveYaw(Script.random(300, 420) % 360);
			}
			if (!s.settings.isRunning() && s.settings.getRunEnergy() > 20) {
				if (Script.random(10) > 8)
					s.settings.setRunning(true);
			}
		} else if (killArea != Constants.fleshCrawlerArea) {
			travelToKillArea();
		}
		return true;
	}
	
	private void travelToKillArea(){
		if (Constants.grandExchangeArea.contains(s.myPosition())) {
			s.magic.castSpell(Spells.NormalSpells.HOME_TELEPORT);
			Constants.condSleep(2000, 300, () -> s.myPlayer().getAnimation() > 4000);
			if (s.myPlayer().getAnimation() > 4000) {
				Constants.condSleep(18000, 300, () -> s.map.canReach(new Position(3222, 3218, 0)));
			}
			s.tabs.open(Tab.INVENTORY);
		}
		s.walking.webWalk(killArea);
	}
	
	private void underAttack() throws InterruptedException{
		if (s.myPlayer().getInteracting().getInteracting() != s.myPlayer()) {
			if (!underAttack(true))
				findAndKill();
			inCombat(() -> !(target != null && target.getHealthPercent() > 0));
		} else {
			target = (NPC) s.myPlayer().getInteracting();
			int r = Script.random(20);
			if (r > e.rightClickLikelihood) {
				if (debug)
					s.log("I'm gonna right click");
				rightClickForNextKill();
			} else if (r > e.hoverLikelihood) {
				if (debug)
					s.log("I'm gonna hover");
				hoverForNextKill();
			} else {
				inCombat(() -> !(target != null && target.getHealthPercent() > 0));
			}
		}
	}

	private void findAndKill() throws InterruptedException {
		NPC nextNPC = findNPC();
		if (nextNPC == null) {
			Script.sleep(Script.random(600, 900));
		} else if (target != null && target.getHealthPercent() > 1) {
			attackNPC(target);
		} else {
			attackNPC(nextNPC);
		}
	}

	private void inCombat(BooleanSupplier expression) throws InterruptedException {
		new ConditionalSleep(20000, 3000) {
			@Override
			public boolean condition() throws InterruptedException {
				return expression.getAsBoolean() || getMyCurrentHealth() <= healthEat
						|| s.myPlayer().getInteracting() != target;
			}
		}.sleep();

		if (getMyCurrentHealth() <= healthEat) {
			Item food = getFoodPiece();
			if (food != null) {
				int healthBefore = getMyCurrentHealth();
				if (food != null) {
					if (debug)
						s.log("EATING IN MAIN EAT");
					food.interact("Eat");
				}
				Constants.condSleep(1000, 100, () -> getMyCurrentHealth() != healthBefore);
				if (foodHealAmount == 1)
					foodHealAmount = getMyCurrentHealth() - healthBefore;
				// likes to re-attack
				if (Script.random(1, 10) > e.reattackLikelihood) {
					attackNPC(target);
				}
				if (getFoodPiece() == null) {
					healthEat = 30;
				} else {
					healthEat = Script.gRandom(e.eatLikelihood, 0.5, 10, s.skills.getStatic(Skill.HITPOINTS) - 8);
				}
				if (debug)
					s.log("Next health eat: " + healthEat);
			} else {
				if (debug)
					s.log("Out of food");
				((BotFactory) s).changeStateNodes();
				return;
			}

			inCombat(expression);
		}

		if (target == null || foodHealAmount > 1
				&& s.skills.getStatic(Skill.HITPOINTS) - s.skills.getDynamic(Skill.HITPOINTS) > foodHealAmount
				&& (target.getHealthPercent() < 1 && Script.random(14) < e.eatUpLikelihood)) {
			Item food = getFoodPiece();
			while (food != null && (s.skills.getStatic(Skill.HITPOINTS) - getMyCurrentHealth() > foodHealAmount)) {
				int healthBefore = getMyCurrentHealth();
				food.interact("Eat");
				Constants.condSleep(1000, 100, () -> getMyCurrentHealth() != healthBefore);
				food = getFoodPiece();
			}
			if (food == null) {
				healthEat = 30;
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

	/**
	 * Hovers over another valid chicken. Only hovers when the current chicken
	 * can be killed by the next hit. If next chicken becomes an invalid target,
	 * another attempt is likely.
	 * 
	 * @throws InterruptedException
	 */
	private void hoverForNextKill() throws InterruptedException {
		inCombat(() -> !(target != null && (getMaxHp(target) * target.getHealthPercent()) / 100 > maxHit));

		NPC nextChicken = findNPC();
		if (nextChicken != null) {
			while (target != null && target.getHealthPercent() > 0 && nextChicken.getInteracting() == null
					&& nextChicken.isVisible()) {
				if (!s.getMouse().isOnCursor(nextChicken)) {
					nextChicken.hover();
				}
				Script.sleep(230);
			}
			if (!nextChicken.isUnderAttack() && (nextChicken.getHealthPercent() > 0)
					&& nextChicken.getInteracting() == null) {
				if (s.mouse.getEntitiesOnCursor().contains(nextChicken)) {
					s.mouse.click(false);
					failSafe = 0;
				} else {
					attackNPC(nextChicken);
				}
				target = nextChicken;
				attempts = 1;
				Script.sleep(400);
			} else {
				if (Script.random(0, 5) > attempts && target != null && target.getHealthPercent() > 0) {
					attempts++;
					hoverForNextKill();
				} else {
					if (Script.random(5) < 4)
						s.mouse.moveSlightly(100);
				}
			}
		}
	}

	/**
	 * Right clicks another chicken whilst in combat. Only right clicks when the
	 * current chicken can be killed by the next hit. If next chicken becomes an
	 * invalid target, another attempt is likely.
	 * 
	 * @throws InterruptedException
	 */
	private void rightClickForNextKill() throws InterruptedException {
		inCombat(() -> !(target != null && (getMaxHp(target) * target.getHealthPercent()) / 100 > maxHit));

		NPC nextChicken = findNPC();
		if (nextChicken != null && !s.map.canReach(nextChicken))
			nextChicken = findNPC(nextChicken);
		// Right click and hover over 'attack'
		int menuIndex = -1;
		boolean firstTime = true;
		while (menuIndex == -1 && nextChicken != null && nextChicken.isVisible() && s.map.canReach(nextChicken)) {
			if (!firstTime)
				s.mouse.moveRandomly(40);
			EntityDestination targetDest = new EntityDestination(s.getBot(), nextChicken);
			if (targetDest != null) {
				s.mouse.click(targetDest, true);
				Script.sleep(Script.gRandom(70, 150));
				menuIndex = s.menu.getMenuIndex(new String[] { nextChicken.getName() }, new String[] { "Attack" });
				Script.sleep(66);
				firstTime = false;
			}
		}
		Rectangle rekt = s.menu.getOptionRectangle(menuIndex);
		RectangleDestination attackOption = new RectangleDestination(s.getBot(), rekt);
		s.mouse.move(attackOption);
		Script.sleep(Script.gRandom(20, 150));

		NPC nextChickenTemp = nextChicken;
		inCombat(() -> !(target != null && target.getHealthPercent() > 0 && nextChickenTemp != null
				&& nextChickenTemp.getInteracting() == null));

		if (nextChicken != null && (nextChicken.getHealthPercent() > 0) && nextChicken.getInteracting() == null) {
			if (rekt.contains(s.mouse.getPosition())) {
				s.mouse.click(false);
				failSafe = 0;
				target = nextChicken;
				attempts = 1;
				Script.sleep(Script.random(890, 1400));
			}
		} else {
			s.mouse.moveRandomly();
			if (Script.random(0, 5) > attempts && target != null && target.getHealthPercent() > 0) {
				attempts++;
				rightClickForNextKill();
			}
		}
	}

	
	private boolean checkAllPlayers() {
		return checkAllPlayers(target);
	}
	/**
	 * Checks all local players to see if they're interacting with my target. If
	 * they are, chose a different target
	 * 
	 * @throws InterruptedException
	 */
	private boolean checkAllPlayers(NPC target){
		if (debug)
			s.log("Checking all players");
		for (Player p : s.getPlayers().getAll()) {
			if (p != s.myPlayer() && p.getInteracting() != null && p.getInteracting() == target) {
				if (p.getPosition().distance(target.getPosition()) < s.myPlayer().getPosition().distance(target) + 2) {
					if (debug)
						s.log("Abandon thread, chosing a different target");
					return true;
				} else if (debug)
					s.log("They're further away it's fine");

			}
		}
		return false;
	}

	/**
	 * Will attack the chicken who is attacking us.
	 * 
	 * @throws InterruptedException
	 */
	public boolean underAttack(boolean attack) throws InterruptedException {
		for (NPC npc : s.getNpcs().getAll()) {
			if (npc.getInteracting() == s.myPlayer() && s.myPlayer().getInteracting() != npc
					&& s.map.distance(npc) < 3) {
				if (debug)
					s.log("OOPS");
				target = npc;
				if (attack)
					attackNPC(target);
				Script.sleep(Script.random(500, 700));
				if (checkAllPlayers()) {
					NPC chicken = findNPC(target);
					target = null;
					attackNPC(chicken);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds and returns the nearest valid nextTarget (with a chance of the
	 * second nearest nextTarget)
	 * 
	 * @param exceptions
	 *            ignores the given nextTargets
	 * @return a near, valid nextTarget
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	private NPC findNPC(NPC exception) throws InterruptedException {
		failSafe++;
		NPC nextTarget = s.getNpcs().closest(new Filter<NPC>() {
			@Override
			public boolean match(NPC npc) {
				return e.npcs.contains(npc.getName()) && exception != npc && (npc.getHealthPercent() > 0)
						&& npc.isAttackable() && !npc.isUnderAttack() && !checkAllPlayers(npc);
			}
		});

		if (nextTarget != null && !s.map.canReach(nextTarget) && (killArea != Constants.fleshCrawlerArea)) {
			RS2Object door = s.objects.closest(new Filter<RS2Object>() {
				@Override
				public boolean match(RS2Object o) {
					return o.getName().equals("Gate") && !(o.getY() == 3284 || o.getY() == 3285);
				}
			});
			if (door != null)
				door.interact("Open");
			Script.sleep(Script.random(400, 700));
		}

		if (Script.random(1, 7) > 5) {
			if (debug)
				s.log("Finding second closest because fk u");
			return findNPC(nextTarget);
		}
		return nextTarget;
	}

	private NPC findNPC() throws InterruptedException {
		return findNPC(null);
	}

	public int getMaxHp(NPC npc) {
		if (npc.getName().equals("Chicken")) {
			return 3;
		} else
			return 25;
	}

	private void attackNPC(NPC nextTarget) throws InterruptedException {
		try {
			if (nextTarget != null) {
				if (s.map.canReach(nextTarget)) {
					if (nextTarget.isVisible()) {
						if (nextTarget.interact("Attack")) {
							failSafe = 0;
							target = nextTarget;
							Script.sleep(Script.random(650, 900));
							if (Script.random(e.afkLikelihood) == 1) {
								if (getMyCurrentHealth() < 0.65 * s.skills.getStatic(Skill.HITPOINTS)) {
									Item fd = getFoodPiece();
									if (fd != null) {
										fd.interact("Eat");
										Script.sleep(300);
										Constants.condSleep(3000, 300, () -> fd == null);
										if (getFoodPiece() == null)
											healthEat = 30;
									}
								}
								((BotFactory) s).afk();
							}
						} else {
							if (debug)
								s.log("We failed attacking");
							nullTarget();
						}
					} else {
						s.camera.toEntity(nextTarget);
					}
				} else {
					nullTarget();
				}
			}
		} catch (Exception e) {
			if (debug)
				s.log("WE FOUND A PROBLEM IN ATTACKNPC: " + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e);
		}
	}

	public void nullTarget() {
		target = null;
	}

	/**
	 * Checks we are allowed to train with the gived attack style
	 * 
	 * @param i
	 *            style ID
	 * @return
	 */
	public boolean checkStyle(int i) {
		switch (i) {
		case 0:
			return e.trainAttkLvl >= s.skills.getStatic(Skill.ATTACK);
		case 1:
			return e.trainStrLvl >= s.skills.getStatic(Skill.STRENGTH);
		case 2:
			if (s.equipment.isWearingItem(EquipmentSlot.WEAPON)) {
				return e.trainStrLvl >= s.skills.getStatic(Skill.STRENGTH);
			} else {
				return e.trainDefLvl >= s.skills.getStatic(Skill.DEFENCE);
			}
		case 3:
			return e.trainDefLvl >= s.skills.getStatic(Skill.DEFENCE);
		}
		return false;
	}

	/**
	 * If we are training something we aren't suppose to, it will change to a
	 * valid one. Has a chance of changing from a valid attack style to another
	 * (just to spice things up once in a while)
	 * 
	 * @return
	 */
	public boolean changeStyle() {
		int styleId = getFightingStyleId();
		if (debug)
			s.log("currently on style " + styleId + "(" + checkStyle(getFightingStyleId()) + ")");
		if (!checkStyle(styleId)) {
			if (e.trainAttkLvl > s.skills.getStatic(Skill.ATTACK))
				changeFightingStyle(0);
			else if (e.trainStrLvl > s.skills.getStatic(Skill.STRENGTH))
				changeFightingStyle(1);
			else if (e.trainDefLvl > s.skills.getStatic(Skill.DEFENCE)) {
				if (s.equipment.isWearingItem(EquipmentSlot.WEAPON)) {
					changeFightingStyle(3);
				} else {
					changeFightingStyle(2);
				}
			}
		} else if (!switchIfNeeded()) {
			if (Script.random(5) > e.changeStyleLikelihood && e.styles.size() > 1) {
				if (debug)
					s.log("Randomly changing styles");
				int st = (int) e.styles.toArray()[Script.random(0, e.styles.size() - 1)];
				if (checkStyle(st))
					changeFightingStyle(st);
				if (debug)
					s.log("Changing to " + st);
			}
		}
		return true;
	}

	private boolean switchIfNeeded() {
		int attkLvl = s.skills.getStatic(Skill.ATTACK);
		int strLvl = s.skills.getStatic(Skill.STRENGTH);
		int defLvl = s.skills.getStatic(Skill.DEFENCE);
		boolean needToTrainAttk = e.trainAttkLvl > attkLvl;
		boolean needToTrainStr = e.trainStrLvl > strLvl;
		boolean needToTrainDef = e.trainDefLvl > defLvl;
		if (!needToTrainAttk)
			attkLvl = 99;
		if (!needToTrainStr)
			strLvl = 99;
		if (!needToTrainDef)
			defLvl = 99;

		switch (getFightingStyleId()) {
		case 0:
			if (attkLvl - Math.min(strLvl, defLvl) > e.minLevelGap) {
				changeFightingStyle(Math.min(strLvl, defLvl) == strLvl ? 1 : 3);
				return true;
			}
			break;
		case 1:
			if (strLvl - Math.min(attkLvl, defLvl) > e.minLevelGap) {
				changeFightingStyle(Math.min(attkLvl, defLvl) == attkLvl ? 0 : 3);
				return true;
			}
			break;
		case 3:
			if (defLvl - Math.min(strLvl, attkLvl) > e.minLevelGap) {
				changeFightingStyle(Math.min(strLvl, attkLvl) == strLvl ? 1 : 0);
				return true;
			}
			break;
		}

		return false;
	}

	private int getFightingStyleId() {
		return s.configs.get(43);
	}

	/**
	 * changes to given style ID
	 * 
	 * @param id
	 */
	private void changeFightingStyle(int id) {
		if (debug)
			s.log("Changing combat styles");
		if (id == getFightingStyleId())
			return;
		if (checkStyle(id)) {
			Tab currentTab = s.tabs.getOpen();
			if (currentTab != Tab.ATTACK)
				s.tabs.open(Tab.ATTACK);
			switch (id) {
			case 0:
				RS2Widget first = s.getWidgets().get(593, 3);
				if (first != null)
					first.interact();
				break;
			case 1:
				RS2Widget second = s.getWidgets().get(593, 7);
				if (second != null)
					second.interact();
				break;
			case 2:
				RS2Widget third = s.getWidgets().get(593, 11);
				if (third != null)
					third.interact();
				break;
			case 3:
				RS2Widget fourth = s.getWidgets().get(593, 15);
				if (fourth != null)
					fourth.interact();
				break;
			}
			if (currentTab != Tab.ATTACK)
				s.tabs.open(currentTab);
		}
	}

	public int getMyCurrentHealth() {
		return s.skills.getDynamic(Skill.HITPOINTS);
	}

	public NPC getTarget() {
		return target;
	}

	/**
	 * Check we have at least one valid attack style to train.
	 * 
	 * @return
	 */
	public boolean checkAllStyles() {
		for (Integer i : e.styles) {
			if (checkStyle(i)) {
				return true;
			}
		}
		return false;
	}
}
