package botFarm;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.function.BooleanSupplier;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.utility.ConditionalSleep;

public class Constants {
	public enum MYTABS {
		FIGHTSTYLE(new Rectangle(529, 169, 32, 36)), LEVEL(new Rectangle(560, 168, 32, 36)), QUEST(
				new Rectangle(595, 169, 32, 36)), INVENTORY(new Rectangle(627, 167, 30, 36)), ARMOUR(
						new Rectangle(659, 170, 32, 36)), PRAYER(new Rectangle(692, 168, 32, 36)), MAGIC(
								new Rectangle(726, 169, 32, 36)), CLAN(new Rectangle(525, 467, 32, 36)), FRIENDS(
										new Rectangle(561, 468, 32, 36)), IGNORE(
												new Rectangle(594, 468, 32, 36)), LOGOUT(
														new Rectangle(627, 470, 32, 36)), OPTIONS(
																new Rectangle(661, 467, 32, 36)), EMOTES(
																		new Rectangle(694, 469, 32, 36)), MUSIC(
																				new Rectangle(727, 469, 32, 36));

		private Rectangle value;

		MYTABS(Rectangle r) {
			this.setValue(r);
		}

		public Rectangle getValue() {
			return value;
		}

		public void setValue(Rectangle value) {
			this.value = value;
		}
	}

	public static final Area chickenArea = new Area(3224, 3309, 3241, 3287),
			chickenAreaCenter = new Area(new int[][] { { 3230, 3295 }, { 3228, 3295 }, { 3228, 3298 }, { 3233, 3298 },
					{ 3233, 3296 }, { 3235, 3296 }, { 3234, 3293 }, { 3231, 3293 } }),
					chickenArea2 = new Area(3168, 3308, 3187, 3287), chickenArea2Center = new Area(3174, 3296, 3180, 3292),
			grandExchangeArea = new Area(3159, 3494, 3171, 3485), strongholdArea = new Area(3077, 3424, 3086, 3416),
			sheepArea = new Area(3194, 3275, 3209, 3260), fleshCrawlerArea = new Area(2034, 5194, 2047, 5184),
			edgevilleBankArea = new Area(3091, 3495, 3096, 3489),
			westVarrockBankArea = new Area(3180, 3440, 3185, 3434), cowArea = new Area(
					new int[][]{
						{3266, 3299},
						{3266, 3255},
						{3253, 3255},
						{3253, 3262},
						{3250, 3262},
						{3250, 3268},
						{3253, 3271},
						{3240, 3284},
						{3240, 3299}
					}
				);
	static final Position lummyBankPos = new Position(3208, 3219, 2);

	public static void condSleep(int maximumMs, int deviationMs, BooleanSupplier condition) {
		new ConditionalSleep(maximumMs, deviationMs) {
			@Override
			public boolean condition() throws InterruptedException {
				return condition.getAsBoolean();
			}
		}.sleep();
	}
	
	public static boolean contains(String name, String[] strs) {
		for (String str : strs) {
			if (str == name)
				return true;
		}
		return false;
	}

	public static int getPrice(final String str) {
		return getField("GEPrice", str);
	}

	public static int getId(final String str) {
		return getField("GEDBID", str);
	}

	private static int getField(final String field, final String item) {
		try {
			final URL url = new URL("http://2007.runescape.wikia.com/wiki/Exchange:" + item.replaceAll(" ", "_"));
			BufferedReader file = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			String price = null;
			while ((line = file.readLine()) != null) {
				if (line.contains("id=\"" + field + "\"")) {
					int beginIndex = line.indexOf(field) + (field.length() + 2);
					price = line.substring(beginIndex, line.indexOf("<", beginIndex));
					break;
				}
			}
			file.close();
			return Integer.parseInt(price.replaceAll(",", ""));
		} catch (IOException e) {
			return 100;
		}
	}
}
