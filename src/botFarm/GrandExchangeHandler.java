package botFarm;

import java.util.ArrayList;

import org.osbot.rs07.api.GrandExchange.Box;
import org.osbot.rs07.api.GrandExchange.Status;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.RectangleDestination;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class GrandExchangeHandler extends Node {

	private boolean debug = e.debug, checkedMoney = false;
	private int attempts = 2, timeoutAttempts = 0;

	public GrandExchangeHandler(Script s, Environment e) {
		super(s, e);
	}

	@Override
	public boolean validate() throws InterruptedException {
		return !e.itemsToBuy.isEmpty();
	}

	@Override
	public boolean execute() throws InterruptedException {
		if (!Constants.grandExchangeArea.contains(s.myPlayer())) {
			(new WalkToStronghold(s, e, true, Constants.grandExchangeArea)).execute();
		}
		if (!e.itemsToSell.isEmpty()) {
			sellItem((String) e.itemsToSell.toArray()[0]);
		} else if (!e.itemsToBuy.isEmpty()) {
			if (!checkedMoney) {
				checkEnoughMoney();
			} else {
				String itemToBuy = (String) e.itemsToBuy.keySet().toArray()[0];
				if (e.itemsToBuy.get(itemToBuy) > 0) {
					buyItem(itemToBuy, e.itemsToBuy.get(itemToBuy));
				} else {
					e.itemsToBuy.remove(itemToBuy);
				}
			}
		} else {
			((BotFactory) s).changeStateNodes();
		}
		return false;
	}

	private boolean checkEnoughMoney() throws InterruptedException {
		int balance = (int) s.inventory.getAmount("Coins");
		String badItem = "";
		int badItemPrice = 0, badItemQuantity = 0;
		for (String str : e.itemsToBuy.keySet()) {
			badItemPrice = Constants.getPrice(str);
			int cost = badItemPrice * e.itemsToBuy.get(str);
			balance -= cost;
			if (debug)
				s.log("Cost of " + e.itemsToBuy.get(str) + "x" + str + " = " + cost);
			if (balance < 0) {
				badItem = str;
				badItemQuantity = e.itemsToBuy.get(str);
				break;
			}
		}
		if (balance < 0) {
			if (replaceRune()) {
				e.itemsToBuy.clear();
			} else if (!makeAdjustments()) {
				notEnoughMonz(badItem, badItemQuantity, badItemPrice);
			}
			return false;
		} else {
			if (debug)
				s.log("It's ok, we gucci with money: " + balance);
			checkedMoney = true;
			return true;
		}
	}

	/**
	 * In the case of not enough money, we shall remove a low priority item.
	 * 
	 * @return if an adjustment has been made
	 */
	private boolean makeAdjustments() {
		if (debug)
			s.log("Let's try and make some adjustments...");
		String toRemove = null;
		String[] armours = new String[] { "kiteshield", "platelegs", "platebody", "full" };
		for (String armourString : armours) {
			for (String item : e.itemsToBuy.keySet()) {
				if (item.contains(armourString)) {
					toRemove = item;
					break;
				}
			}
		}
		if (toRemove != null) {
			if (debug)
				s.log("Removing " + toRemove + " from our buy list");
			e.itemsToBuy.remove(toRemove);
			for (EquipmentSlot slot : e.itemsToEquip.keySet()) {
				if (e.itemsToEquip.get(slot).equalsIgnoreCase(toRemove)) {
					e.itemsToEquip.remove(slot);
					break;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * replaces the first occurance of a rune item in itemsToBuy with the
	 * adamant version of it.
	 * 
	 * @return true if successful
	 */
	private boolean replaceRune() {
		if (debug)
			s.log("Let's try and make some adjustments...");
		String toRemove = null;
		for (String str : e.itemsToBuy.keySet()) {
			if (str.contains("Rune")) {
				toRemove = str;
				break;
			}
		}
		if (toRemove != null) {
			if (debug)
				s.log("Too poor for dat swag rune, replacing " + toRemove + " with "
						+ toRemove.replace("Rune", "Adamant"));
			e.itemsToBuy.remove(toRemove);
			e.itemsToBuy.put(toRemove.replace("Rune", "Adamant"), 1);
			for (EquipmentSlot slot : e.itemsToEquip.keySet()) {
				if (e.itemsToEquip.get(slot).equalsIgnoreCase(toRemove)) {
					e.itemsToEquip.replace(slot, toRemove.replace("Rune", "Adamant"));
				}
			}
			return true;
		}
		return false;
	}

	private void sellItem(String str) throws InterruptedException {
		if (s.inventory.contains(str)) {
			if (debug)
				s.log("Selling " + str);
			if (openGE()) {
				Box theBox = getFirstEmptyBox();
				if (debug)
					s.log("First empty box: " + theBox);

				if (theBox != null) {

					for (Item item : s.inventory.getItems()) {
						if (item != null && item.getName().equals(str)) {
							item.interact("Offer");
							break;
						}
					}

					Constants.condSleep(7000, 300, () -> s.grandExchange.isSellOfferOpen());
					for (int i = 0; i < attempts; i++)
						minusFivePercent();
					Script.sleep(Script.random(400, 650));
					s.grandExchange.confirm();

					Constants.condSleep(5000, 1000,
							() -> s.grandExchange.getStatus(theBox).equals(Status.FINISHED_SALE));

					if (s.grandExchange.getStatus(theBox).equals(Status.FINISHED_SALE)) {
						s.grandExchange.collect();
						e.itemsToSell.remove(str);
						attempts = 0;
					} else {
						abort(theBox);
						attempts += Script.random(1, 3);
					}
				} else {
					if (debug)
						s.log("No available boxes");
				}
			}
		} else {
			if (debug)
				s.log("We don't have any " + str + " to sell");
			e.itemsToSell.remove(str);
		}
	}

	private Box getFirstEmptyBox() {
		int i = 0;
		for (Box box : Box.values()) {
			if (s.grandExchange.getStatus(box) == Status.EMPTY) {
				if (e.debug)
					s.log("Box " + i + " is empty");
				break;
			}
			i++;
		}

		if (i < 3) {
			return Box.values()[i];
		} else {
			return null;
		}
	}

	private void abort(Box box) throws InterruptedException {
		if (debug)
			s.log("Aborting");
		ArrayList<RS2Widget> list = new ArrayList<RS2Widget>(s.widgets.containingActions(465, "Abort offer"));
		if (list.size() > 0) {
			try {
				RS2Widget w = list.get(box.ordinal());
				if (w != null) {
					Status status = s.grandExchange.getStatus(box);
					if (w.interact("Abort offer")) {
						Constants.condSleep(4000, 1000, () -> s.grandExchange.getStatus(box) != status);
						if (debug)
							s.log("Status after: " + s.grandExchange.getStatus(box));
						Script.sleep(640);
						Constants.condSleep(3000, 1000, () -> s.widgets.getWidgetContainingText("Collect") != null
								&& s.widgets.getWidgetContainingText("Collect").isVisible());
						s.grandExchange.collect();
						Script.sleep(Script.random(400, 750));
					}
				}
			} catch (Exception e) {
				s.log("OUT OF RANGE BOI");
				s.grandExchange.collect();
				s.log(e.getMessage());
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void buyItem(String str, int number) throws InterruptedException {
		if (debug)
			s.log("Buying " + number + " " + str);
		if (openGE()) {
			Box theBox = getFirstEmptyBox();
			if (!s.grandExchange.isBuyOfferOpen()) {
				s.grandExchange.buyItems(theBox);
				Constants.condSleep(1000, 400, () -> s.grandExchange.isBuyOfferOpen());
			}

			RS2Widget w = null;
			if (s.widgets.getWidgetContainingText(162, "What would you like to buy") != null) {
				Script.sleep(Script.random(500, 700));
				s.keyboard.typeString(
						str.substring(0, str.length() > 10 ? str.length() - Script.random(4) : str.length()), false);
				Constants.condSleep(4000, 300, () -> s.widgets.getWidgetContainingText(162, str) != null
						&& s.widgets.getWidgetContainingText(162, str).getMessage().length() <= str.length());
				Script.sleep(Script.random(150, 300));
				w = s.widgets.singleFilter(162, new Filter<RS2Widget>() {
					@Override
					public boolean match(RS2Widget wid) {
						return wid != null && wid.getMessage().length() <= str.length()
								&& wid.getMessage().contains(str);
					}
				});
			}

			try {
				if (w != null) {
					timeoutAttempts = 0;
					if (s.mouse.click(new RectangleDestination(s.getBot(), w.getAbsX(), w.getAbsY(), w.getWidth(),
							w.getHeight()))) {
						Script.sleep(Script.random(400, 800));
						Constants.condSleep(6000, 1000,
								() -> s.widgets.getWidgetContainingText("Choose an item...") == null);

						int price = Constants.getPrice(str);
						int balance = (int) s.inventory.getAmount("Coins");
						if (balance - (price * number) > 0) {
							Script.sleep(Script.random(400, 650));
							int quantity = 10;
							if (number > 1) {
								if (number > 4) {
									if (Script.random(1, 9) > e.geOfferButtonsLikelihood) {
										s.mouse.click(new RectangleDestination(s.getBot(),
												s.widgets.getWidgetContainingText("+10").getRectangle()));
										for (int i = 0; i < Script.gRandom(2, 10); i++) {
											quantity += 10;
											Script.sleep(Script.random(180, 240));
											if (balance - (price * quantity) > 0) {
												s.mouse.click(false);
											}
										}
									} else {
										quantity = Script.random(3, 10) * 10;
										if (balance - (price * quantity) < 0)
											quantity = number;
										s.grandExchange.setOfferQuantity(quantity);
									}
								} else {
									int randNumber = Script.random(2, 5) * 5;
									if (balance - (price * randNumber) > 0)
										s.grandExchange.setOfferQuantity(randNumber);
									else
										s.grandExchange.setOfferQuantity(number);
								}
							}
							if (attempts > 8) {
								s.grandExchange.setOfferPrice((balance - Script.random(0, 100)) / number);
							} else {
								for (int i = 0; i < attempts; i++) {
									///// MORE CHECKS PLS 465,24,39
									RS2Widget w2 = s.widgets.getWidgetContainingText("coins");
									if (w2 != null) {
										int pricew2 = Integer
												.parseInt(w2.getMessage().replace(",", "").replace(" coins", ""));
										if (!(pricew2 * 1.05 > balance)) {
											addFivePercent();
											Script.sleep(Script.random(350, 490));
										} else if (attempts > 3) {
											notEnoughMonz(str, number, price);
										}
									}
								}
							}
							Script.sleep(Script.random(300, 600));
							s.grandExchange.confirm();
							Constants.condSleep(11000, 1000, () -> !s.grandExchange.isBuyOfferOpen());
							Constants.condSleep(8000, 1000,
									() -> s.grandExchange.getStatus(theBox).equals(Status.FINISHED_BUY));

							if (s.grandExchange.getStatus(theBox).equals(Status.FINISHED_BUY)) {
								s.grandExchange.collect();
								e.itemsToBuy.remove(str);
								attempts = Script.random(1, 3);
							} else if (s.grandExchange.isBuyOfferOpen()) {
								s.grandExchange.goBack();
							} else {
								abort(theBox);
								if (attempts > 8) {
									notEnoughMonz(str, number, price);
								} else {
									Script.sleep(Script.random(500, 1200));
									attempts += Script.random(2, 4);
									buyItem(str, number - (int) s.inventory.getAmount(str));
								}
							}
						} else {
							notEnoughMonz(str, number, price);
						}
					}
				} else if (timeoutAttempts > 4) {
					s.log("Item " + str + " doesn't seem to exist, are you sure you entered it in correctly?");
					e.itemsToBuy.remove(str);
				} else if (s.widgets.getWidgetContainingText(162, "What would you like to") != null && s.widgets
						.getWidgetContainingText(162, "What would you like to").getMessage().length() > 47) {
					ArrayList<RS2Widget> list = new ArrayList<RS2Widget>(
							s.widgets.containingActions(465, "Choose item"));
					if (list.size() > 0) {
						timeoutAttempts++;
						list.get(0).interact("Choose item");
					}
				}

			} catch (Exception e) {
				s.log(e);
				s.log(e.getMessage());
				s.log("Something went wrong with buying");
			}
		}

	}

	/**
	 * For having not enough money on the fly.
	 * 
	 * @param str
	 *            item name
	 * @param number
	 *            item quantity
	 * @param price
	 *            item price
	 * @throws InterruptedException
	 */
	private void notEnoughMonz(String str, int number, int price) throws InterruptedException {
		if (number > 1 || str.contains("scimitar")) {
			((BotFactory) s).notEnoughMoney((int) (price * number * 1.1));
		} else {
			s.log("Not enough money to buy gear piece " + str);
			e.itemsToBuy.remove(str);
			attempts = Script.random(1, 3);
			for (EquipmentSlot slot : e.itemsToEquip.keySet()) {
				if (e.itemsToEquip.get(slot).equalsIgnoreCase(str))
					e.itemsToEquip.remove(slot);
			}
			s.grandExchange.goBack();
		}
	}

	private void minusFivePercent() throws InterruptedException {
		ArrayList<RS2Widget> list = new ArrayList<RS2Widget>(s.widgets.containingActions(465, "-5%"));
		if (list.size() > 0) {
			RS2Widget w = list.get(0);
			if (w.getRectangle().contains(s.mouse.getPosition())) {
				s.mouse.click(false);
			} else {
				w.interact("-5%");
			}
		}
		Script.sleep(Script.random(180, 250));
	}

	private void addFivePercent() throws InterruptedException {
		ArrayList<RS2Widget> list = new ArrayList<RS2Widget>(s.widgets.containingActions(465, "+5%"));
		if (list.size() > 0) {
			RS2Widget w = list.get(0);
			if (w.getRectangle().contains(s.mouse.getPosition())) {
				s.mouse.click(false);
			} else {
				w.interact("+5%");
			}
		}
		Script.sleep(Script.random(180, 250));
	}

	private boolean openGE() {
		if (!s.grandExchange.isOpen()) {
			NPC clerk = s.npcs.closest("Grand Exchange Clerk");
			if (clerk != null) {
				if (clerk.interact("Exchange")) {
					new ConditionalSleep(8000, 300) {
						@Override
						public boolean condition() throws InterruptedException {
							return s.grandExchange.isOpen();
						}
					}.sleep();
					return true;
				}
			}
			return false;
		}
		return true;
	}
}
