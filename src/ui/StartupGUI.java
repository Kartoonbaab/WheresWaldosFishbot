package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class StartupGUI extends JFrame {
    private JComboBox<String> fishCombo, locationCombo;
    private JCheckBox bankingCheck;
    private JButton startButton;
    private boolean ready = false;

    public StartupGUI() {
        setTitle("Fishing Bot - Session Setup");
        setSize(350, 180);
        setLayout(new GridLayout(4, 2));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        add(new JLabel("Fish:"));
        fishCombo = new JComboBox<>(new String[]{
                "Shrimp", "Trout", "Salmon", "Lobster", "Swordfish", "Shark"
        });
        add(fishCombo);

        add(new JLabel("Location:"));
        locationCombo = new JComboBox<>(new String[]{
                "Lumbridge", "Barbarian Village", "Karamja", "Catherby", "Fishing Guild"
        });
        add(locationCombo);

        add(new JLabel("Banking:"));
        bankingCheck = new JCheckBox("Enable Banking", true);
        add(bankingCheck);

        add(new JLabel());
        startButton = new JButton("Start Bot");
        startButton.addActionListener((ActionEvent e) -> {
            ready = true;
            setVisible(false);
            dispose();
        });
        add(startButton);
    }

    public boolean isReady() {
        return ready;
    }

    public String getSelectedFish() {
        return fishCombo.getSelectedItem().toString();
    }

    public String getSelectedLocation() {
        return locationCombo.getSelectedItem().toString();
    }

    public boolean isBankingEnabled() {
        return bankingCheck.isSelected();
    }
}
