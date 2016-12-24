package botFarm;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.EquipmentSlot;

public class GUI2 extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField trainAttkText;
	private JTextField trainStrText;
	private JTextField trainDefText;
	private JTextField trainAttkFieldChick;
	private JTextField trainStrFieldChick;
	private JTextField trainDefFieldChick;
	private JTextField txtFood;
	private JTextField txtRing, txtNecklace, txtShield, txtWeapon, txtHead, txtChest, txtLegs, txtBoots, txtCape;
	private JCheckBox chckBoxDefaultGear;
	private JCheckBox chckbxQuestMode;
	private JCheckBox chckbxGetqp;
	private HashMap<JTextField, EquipmentSlot> customGear;
	private JTextField trainDefFieldCow;
	private JTextField trainStrFieldCow;
	private JTextField trainAttkFieldCow;

	public GUI2(BotFactory s, Environment e) {
		customGear = new HashMap<JTextField, EquipmentSlot>();
		setResizable(false);
		setTitle("Bot Factory");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 407, 180);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JCheckBox chckbxAttack = new JCheckBox("Attack");
		chckbxAttack.setSelected(true);
		chckbxAttack.setBounds(22, 31, 97, 23);
		chckbxAttack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				trainAttkText.setEnabled(chckbxAttack.isSelected());
				trainAttkFieldChick.setEnabled(chckbxAttack.isSelected());
			}
		});
		contentPane.add(chckbxAttack);

		JLabel lblWhatToTrain = new JLabel("What to train");
		lblWhatToTrain.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblWhatToTrain.setBounds(124, 11, 197, 14);
		contentPane.add(lblWhatToTrain);

		JCheckBox chckbxStrength = new JCheckBox("Strength");
		chckbxStrength.setSelected(true);
		chckbxStrength.setBounds(169, 31, 97, 23);
		chckbxStrength.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				trainStrText.setEnabled(chckbxStrength.isSelected());
				trainStrFieldChick.setEnabled(chckbxStrength.isSelected());
			}
		});
		contentPane.add(chckbxStrength);

		JCheckBox chckbxDefense = new JCheckBox("Defense");
		chckbxDefense.setSelected(true);
		chckbxDefense.setBounds(309, 31, 97, 23);
		chckbxDefense.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				trainDefText.setEnabled(chckbxDefense.isSelected());
				trainDefFieldChick.setEnabled(chckbxDefense.isSelected());
			}
		});
		contentPane.add(chckbxDefense);

		trainAttkText = new JTextField();
		trainAttkText.setText("99");
		trainAttkText.setToolTipText("Level");
		trainAttkText.setBounds(22, 61, 86, 20);
		trainAttkText.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent k) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent k) {
				if (k.getKeyChar() == 'D') {
					s.log("Debug activated");
					e.debug = true;
				} else if (k.getKeyChar() == 'C') {
					s.log("Cows for money");
					e.cowhideLootCutOff = 99999;
					s.needMoreMoney = true;
				} 
			}
		});
		contentPane.add(trainAttkText);
		trainAttkText.setColumns(10);

		trainStrText = new JTextField();
		trainStrText.setText("99");
		trainStrText.setToolTipText("Level");
		trainStrText.setColumns(10);
		trainStrText.setBounds(169, 61, 86, 20);
		contentPane.add(trainStrText);

		trainDefText = new JTextField();
		trainDefText.setText("99");
		trainDefText.setToolTipText("Level");
		trainDefText.setColumns(10);
		trainDefText.setBounds(309, 61, 86, 20);
		contentPane.add(trainDefText);

		JLabel lblNoteLeaveLevel = new JLabel("");
		lblNoteLeaveLevel.setBounds(60, 107, 259, 14);
		contentPane.add(lblNoteLeaveLevel);

		JCheckBox chckbxBuryBones = new JCheckBox("Bury bones");
		chckbxBuryBones.setSelected(false);
		chckbxBuryBones.setBounds(169, 88, 145, 23);
		contentPane.add(chckbxBuryBones);

		JPanel settingsPanel = new JPanel();
		settingsPanel.setBounds(22, 155, 373, 261);
		contentPane.add(settingsPanel);
		settingsPanel.setLayout(null);

		JPanel panel_1 = new JPanel();
		panel_1.setLayout(null);
		panel_1.setBounds(0, 1, 373, 76);

		JLabel lblTrainAtChickens = new JLabel("Train at chickens until these stats");
		lblTrainAtChickens.setBounds(18, 41, 214, 22);
		panel_1.add(lblTrainAtChickens);

		trainAttkFieldChick = new JTextField();
		trainAttkFieldChick.setToolTipText("Level");
		trainAttkFieldChick.setText("15");
		trainAttkFieldChick.setColumns(10);
		trainAttkFieldChick.setBounds(18, 82, 86, 20);
		panel_1.add(trainAttkFieldChick);

		trainStrFieldChick = new JTextField();
		trainStrFieldChick.setToolTipText("Level");
		trainStrFieldChick.setText("15");
		trainStrFieldChick.setColumns(10);
		trainStrFieldChick.setBounds(137, 80, 86, 20);
		panel_1.add(trainStrFieldChick);

		trainDefFieldChick = new JTextField();
		trainDefFieldChick.setToolTipText("Level");
		trainDefFieldChick.setText("10");
		trainDefFieldChick.setColumns(10);
		trainDefFieldChick.setBounds(256, 80, 86, 20);
		panel_1.add(trainDefFieldChick);

		JPanel panel_2 = new JPanel();
		panel_2.setLayout(null);
		panel_2.setBounds(0, 154, 373, 236);
		// settingsPanel.add(panel_2);

		JLabel label_2 = new JLabel("Gear & Supplies");
		label_2.setFont(new Font("Tahoma", Font.BOLD, 18));
		label_2.setBounds(113, 5, 241, 22);
		panel_2.add(label_2);

		JPanel customGearPanel = new JPanel();
		customGearPanel.setLayout(null);
		customGearPanel.setVisible(false);
		customGearPanel.setBounds(5, 74, 358, 146);
		panel_2.add(customGearPanel);

		chckBoxDefaultGear = new JCheckBox("Use appropriate gear");
		chckBoxDefaultGear.setSelected(true);
		chckBoxDefaultGear.setBounds(6, 44, 145, 23);
		chckBoxDefaultGear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				customGearPanel.setVisible(!chckBoxDefaultGear.isSelected());
			}
		});
		panel_2.add(chckBoxDefaultGear);

		JLabel lblfleshCrawlers = new JLabel("(Flesh Crawlers & cows only)");
		lblfleshCrawlers.setBounds(113, 27, 193, 14);
		panel_2.add(lblfleshCrawlers);

		JLabel lblFoodName = new JLabel("Food");
		lblFoodName.setBounds(187, 48, 45, 14);
		panel_2.add(lblFoodName);

		JLabel label_11 = new JLabel("Legs");
		label_11.setBounds(3, 67, 45, 17);
		customGearPanel.add(label_11);

		JLabel label_12 = new JLabel("Boots");
		label_12.setBounds(3, 95, 45, 14);
		customGearPanel.add(label_12);

		JLabel label_5 = new JLabel("Ring");
		label_5.setBounds(185, 92, 45, 14);
		customGearPanel.add(label_5);

		JLabel label_6 = new JLabel("Necklace");
		label_6.setBounds(185, 67, 52, 17);
		customGearPanel.add(label_6);

		JLabel label_7 = new JLabel("Shield");
		label_7.setBounds(185, 39, 45, 14);
		customGearPanel.add(label_7);

		JLabel label_8 = new JLabel("Weapon");
		label_8.setBounds(185, 10, 45, 18);
		customGearPanel.add(label_8);

		JLabel label_9 = new JLabel("Head");
		label_9.setBounds(3, 11, 45, 14);
		customGearPanel.add(label_9);

		JLabel label_10 = new JLabel("Chest");
		label_10.setBounds(3, 39, 45, 14);
		customGearPanel.add(label_10);

		JLabel label_13 = new JLabel("Cape");
		label_13.setBounds(3, 123, 45, 14);
		customGearPanel.add(label_13);

		txtFood = new JTextField();
		txtFood.setText("Trout");
		txtFood.setColumns(10);
		txtFood.setBounds(242, 44, 86, 20);
		panel_2.add(txtFood);

		txtRing = new JTextField();
		txtRing.setText("none");
		txtRing.setColumns(10);
		txtRing.setBounds(236, 89, 116, 20);
		customGear.put(txtRing, EquipmentSlot.RING);
		customGearPanel.add(txtRing);

		txtNecklace = new JTextField();
		txtNecklace.setToolTipText("Level");
		txtNecklace.setText("default");
		txtNecklace.setColumns(10);
		txtNecklace.setBounds(236, 64, 116, 20);
		customGear.put(txtNecklace, EquipmentSlot.AMULET);
		customGearPanel.add(txtNecklace);

		txtShield = new JTextField();
		txtShield.setToolTipText("Level");
		txtShield.setText("default");
		txtShield.setColumns(10);
		txtShield.setBounds(236, 36, 116, 20);
		customGear.put(txtShield, EquipmentSlot.SHIELD);
		customGearPanel.add(txtShield);

		txtWeapon = new JTextField();
		txtWeapon.setText("default");
		txtWeapon.setColumns(10);
		txtWeapon.setBounds(236, 7, 116, 20);
		customGear.put(txtWeapon, EquipmentSlot.WEAPON);
		customGearPanel.add(txtWeapon);

		txtHead = new JTextField();
		txtHead.setText("default");
		txtHead.setColumns(10);
		txtHead.setBounds(47, 8, 116, 20);
		customGear.put(txtHead, EquipmentSlot.HAT);
		customGearPanel.add(txtHead);

		txtChest = new JTextField();
		txtChest.setText("default");
		txtChest.setColumns(10);
		txtChest.setBounds(47, 36, 116, 20);
		customGearPanel.add(txtChest);
		customGear.put(txtChest, EquipmentSlot.CHEST);

		txtLegs = new JTextField();
		txtLegs.setText("default");
		txtLegs.setColumns(10);
		txtLegs.setBounds(47, 64, 116, 20);
		customGear.put(txtLegs, EquipmentSlot.LEGS);
		customGearPanel.add(txtLegs);

		txtBoots = new JTextField();
		txtBoots.setToolTipText("Level");
		txtBoots.setText("none");
		txtBoots.setColumns(10);
		txtBoots.setBounds(47, 92, 116, 20);
		customGear.put(txtBoots, EquipmentSlot.FEET);
		customGearPanel.add(txtBoots);

		txtCape = new JTextField();
		txtCape.setText("none");
		txtCape.setColumns(10);
		txtCape.setBounds(47, 120, 116, 20);
		
		JButton btnNewButton_1 = new JButton("Load current gear");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EquipmentSlot[] slots = { EquipmentSlot.RING, EquipmentSlot.AMULET, EquipmentSlot.SHIELD,
						EquipmentSlot.WEAPON, EquipmentSlot.HAT, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
						EquipmentSlot.FEET, EquipmentSlot.CAPE };
				JTextField[] fields = {txtRing, txtNecklace, txtShield, txtWeapon, txtHead, txtChest, txtLegs, txtBoots, txtCape};
				for(int i = 0; i < 9; i++){
					Item it = s.equipment.getItemInSlot(slots[i].slot);
					fields[i].setText(it == null ? "none" : it.getName());
				}
			}
		});
		
		btnNewButton_1.setBounds(185, 117, 167, 23);
		customGearPanel.add(btnNewButton_1);

		customGear.put(txtCape, EquipmentSlot.CAPE);
		customGearPanel.add(txtCape);

		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBounds(0, 76, 373, 76);

		chckbxGetqp = new JCheckBox("Get 7 quest points");
		chckbxGetqp.setSelected(true);
		chckbxGetqp.setBounds(10, 40, 131, 23);
		panel.add(chckbxGetqp);

		chckbxQuestMode = new JCheckBox("Quest mode");
		chckbxQuestMode.setBounds(207, 40, 131, 23);
		panel.add(chckbxQuestMode);

		JLabel lblwillNotTrain = new JLabel("(Will not train combat)");
		lblwillNotTrain.setBounds(221, 62, 137, 14);
		panel.add(lblwillNotTrain);

		JLabel lblRequiredToUse = new JLabel("7 Quest Points is required to use GE, unless 24hr game time is exceeded");
		lblRequiredToUse.setBounds(10, 11, 368, 22);
		panel.add(lblRequiredToUse);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 373, 258);
		settingsPanel.add(tabbedPane);

		tabbedPane.addTab("Levels", null, panel_1, null);

		JLabel lblAttack = new JLabel("Attack");
		lblAttack.setBounds(18, 63, 68, 18);
		panel_1.add(lblAttack);

		JLabel lblStrength = new JLabel("Strength");
		lblStrength.setBounds(137, 60, 82, 21);
		panel_1.add(lblStrength);

		JLabel lblDefence = new JLabel("Defence");
		lblDefence.setBounds(256, 60, 82, 21);
		panel_1.add(lblDefence);
		
		trainDefFieldCow = new JTextField();
		trainDefFieldCow.setToolTipText("Level");
		trainDefFieldCow.setText("25");
		trainDefFieldCow.setColumns(10);
		trainDefFieldCow.setBounds(246, 151, 86, 20);
		panel_1.add(trainDefFieldCow);
		
		JLabel label = new JLabel("Defence");
		label.setBounds(246, 131, 82, 20);
		panel_1.add(label);
		
		JLabel label_1 = new JLabel("Strength");
		label_1.setBounds(138, 131, 82, 20);
		panel_1.add(label_1);
		
		trainStrFieldCow = new JTextField();
		trainStrFieldCow.setToolTipText("Level");
		trainStrFieldCow.setText("25");
		trainStrFieldCow.setColumns(10);
		trainStrFieldCow.setBounds(138, 151, 86, 20);
		panel_1.add(trainStrFieldCow);
		
		trainAttkFieldCow = new JTextField();
		trainAttkFieldCow.setToolTipText("Level");
		trainAttkFieldCow.setText("25");
		trainAttkFieldCow.setColumns(10);
		trainAttkFieldCow.setBounds(18, 151, 86, 20);
		panel_1.add(trainAttkFieldCow);
		
		JLabel label_3 = new JLabel("Attack");
		label_3.setBounds(18, 132, 68, 19);
		panel_1.add(label_3);
		
		JLabel label_4 = new JLabel("Train at cows until these stats");
		label_4.setBounds(18, 109, 214, 22);
		panel_1.add(label_4);
		
		JLabel lblChickensCows = new JLabel("Chickens & Cows");
		lblChickensCows.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblChickensCows.setBounds(101, 11, 241, 22);
		panel_1.add(lblChickensCows);
		
		JCheckBox chckbxCowMode = new JCheckBox("Cow mode (Will just train at cows and pick up more hides)");
		chckbxCowMode.setBounds(18, 178, 340, 23);
		panel_1.add(chckbxCowMode);
		
		JLabel lblWarningWillNot = new JLabel("Warning: Uses current gear - will not upgrade armour");
		lblWarningWillNot.setBounds(40, 197, 318, 22);
		panel_1.add(lblWarningWillNot);

		tabbedPane.addTab("Questing", null, panel, null);

		tabbedPane.addTab("Gear", null, panel_2, null);
		
		JLabel label_14 = new JLabel("Train at chickens until these stats");
		label_14.setBounds(23, 239, 214, 22);
		settingsPanel.add(label_14);
		settingsPanel.setVisible(false);

		JCheckBox chckbxDefaultSettings = new JCheckBox("Use default settings");
		chckbxDefaultSettings.setSelected(true);
		chckbxDefaultSettings.setBounds(22, 88, 145, 23);
		chckbxDefaultSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingsPanel.setVisible(!chckbxDefaultSettings.isSelected());
				setSize(441, chckbxDefaultSettings.isSelected() ? 180 : 450);
				trainAttkFieldChick.setText("15");
				trainStrFieldChick.setText("15");
				trainDefFieldChick.setText("15");
				chckBoxDefaultGear.setSelected(true);
				chckbxQuestMode.setSelected(false);
				chckbxGetqp.setSelected(true);
				txtFood.setText("Trout");
			}
		});
		contentPane.add(chckbxDefaultSettings);

		JButton btnNewButton = new JButton("Start");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean trainAttk = chckbxAttack.isSelected();
				boolean trainStr = chckbxStrength.isSelected();
				boolean trainDef = chckbxDefense.isSelected();
				if (!trainAttk && !trainDef && !trainStr) {
					lblNoteLeaveLevel.setText("Error: Please select at least 1 combat style");
					lblNoteLeaveLevel.setForeground(Color.RED);
				} else {
					try {
						if (trainAttk) {
							int attklvl = Integer.parseInt(trainAttkText.getText());
							e.attkLvlCutOff = attklvl;
							e.chickensAttkCutOff = Math.min(attklvl, Integer.parseInt(trainAttkFieldChick.getText()));
							e.cowsAttkCutOff = Math.min(attklvl, Integer.parseInt(trainAttkFieldCow.getText()));;
						} else {
							e.attkLvlCutOff = 0;
							e.chickensAttkCutOff = 0;
							e.cowsAttkCutOff = 0;
						}
						if (trainStr) {
							int strlvl = Integer.parseInt(trainStrText.getText());
							e.strLvlCutOff = strlvl;
							e.chickensStrCutOff = Math.min(strlvl, Integer.parseInt(trainStrFieldChick.getText()));
							e.cowsStrCutOff = Math.min(strlvl, Integer.parseInt(trainStrFieldCow.getText()));;
						} else {
							e.strLvlCutOff = 0;
							e.chickensStrCutOff = 0;
							e.cowsStrCutOff = 0;
						}
						if (trainDef) {
							int deflvl = Integer.parseInt(trainDefText.getText());
							e.defLvlCutOff = deflvl;
							e.chickensDefCutOff = Math.min(deflvl, Integer.parseInt(trainDefFieldChick.getText()));
							e.cowsDefCutOff =  Math.min(deflvl, Integer.parseInt(trainDefFieldCow.getText()));;
						} else {
							e.defLvlCutOff = 0;
							e.chickensDefCutOff = 0;
							e.cowsDefCutOff = 0;
						}
						e.lootBones = chckbxBuryBones.isSelected();
						e.food = txtFood.getText();
						e.questing = chckbxGetqp.isSelected();
						e.questOnly = chckbxQuestMode.isSelected();
						e.defaultGear = chckBoxDefaultGear.isSelected();
						if (!chckBoxDefaultGear.isSelected()) {
							e.customGear = new HashMap<EquipmentSlot, String>();
							for (JTextField field : customGear.keySet()) {
								String text = field.getText();
								if (!text.equalsIgnoreCase("default")) {
									e.customGear.put(customGear.get(field), field.getText());
								}
							}
						}
						if(chckbxCowMode.isSelected()) {
							e.cowhideLootCutOff = 99999;
							s.needMoreMoney = true;
						}
						dispose();
						s.start();
					} catch (Exception e) {
						lblNoteLeaveLevel.setText("Error: Please input numbers 0-99 where appropriate.");
						lblNoteLeaveLevel.setForeground(Color.RED);
					}
				}
			}
		});
		btnNewButton.setBounds(20, 121, 373, 23);
		contentPane.add(btnNewButton);
	}
}
