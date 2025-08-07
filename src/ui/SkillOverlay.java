package ui;

import handlers.FishingHandler;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import java.awt.*;

public class SkillOverlay {
    private final FishingHandler handler;

    public SkillOverlay(FishingHandler handler) {
        this.handler = handler;
    }

    public void onPaint(Graphics g) {
        int x = 8, y = 30;
        int boxWidth = 240;
        int boxHeight = 180;

        // Stats box
        g.setColor(new Color(25, 35, 55, 220));
        g.fillRoundRect(x - 4, y - 22, boxWidth, boxHeight, 16, 16);

        g.setColor(Color.WHITE);
        g.drawString("Fishing Bot v1.0", x, y);
        y += 22;
        g.drawString("Fish: " + handler.getCurrentFish(), x, y);
        y += 20;
        g.drawString("Location: " + handler.getCurrentLocation(), x, y);
        y += 20;

        int xpGained = Skills.getExperience(Skill.FISHING) - handler.getStartXP();
        g.drawString("XP Gained: " + xpGained, x, y);
        y += 20;

        // current level levels gained
        int startLevel = handler.getStartLvl();
        int currentLevel = Skills.getRealLevel(Skill.FISHING);
        int levelsGained = currentLevel - startLevel;
        String levelText = "Level: " + currentLevel + (levelsGained > 0 ? "  (+" + levelsGained + ")" : "");
        g.drawString(levelText, x, y);
        y += 20;

        g.drawString("Fish Caught: " + handler.getFishCaught(), x, y);
        y += 20;

        long elapsed = (System.currentTimeMillis() - handler.getStartTime()) / 1000;
        String runTime = String.format("Run Time: %d min %02d sec", (elapsed / 60), (elapsed % 60));
        g.drawString(runTime, x, y);

        y += 25;
        g.setColor(Color.YELLOW);
        g.drawString("Status: " + handler.getStatus(), x, y);
    }
}
