package main;

import handlers.FishingHandler;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import ui.SkillOverlay;
import ui.StartupGUI;
import util.HumanMouseMover;

import javax.swing.SwingUtilities;
import java.awt.*;

@ScriptManifest(
        author = "Wanky Danky Waldo",
        name = "Where Waldos Fish Bot",
        version = 1.0,
        description = "Simple OSRS Fishing Bot with overlay and settings",
        category = Category.FISHING
)
public class Main extends AbstractScript {
    private FishingHandler fishingHandler;
    private SkillOverlay skillOverlay;
    private StartupGUI startupGUI;

    public static String selectedFish = "AI";
    public static String selectedLocation = "AI";
    public static boolean bankingEnabled = true;

    private long nextMouseWiggle = 0;
    private long nextFakeRightClick = 0;
    private long nextOffScreen = 0;
    private long nextTabHover = 0;
    private long nextFakeMisclick = 0;
    private long nextCameraRotate = 0;

    @Override
    public void onStart() {
        final Object lock = new Object();

        synchronized (lock) {
            // Create and show GUI on EDT
            SwingUtilities.invokeLater(() -> {
                startupGUI = new StartupGUI();
                startupGUI.setVisible(true);

                synchronized (lock) {
                    lock.notify();
                }
            });

            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log("Waiting for user input in the Startup GUI...");
        while (!startupGUI.isReady()) {
            sleep(200);
        }

        selectedFish = startupGUI.getSelectedFish();
        selectedLocation = startupGUI.getSelectedLocation();
        bankingEnabled = startupGUI.isBankingEnabled();

        log("Selected: " + selectedFish + " at " + selectedLocation + " | Banking: " + bankingEnabled);

        fishingHandler = new FishingHandler();
        skillOverlay = new SkillOverlay(fishingHandler);
        Tabs.open(Tab.INVENTORY);

        nextMouseWiggle = System.currentTimeMillis() + randomBetween(15_000, 60_000);
        nextFakeRightClick = System.currentTimeMillis() + randomBetween(40_000, 120_000);
        nextOffScreen = System.currentTimeMillis() + randomBetween(70_000, 200_000);
        nextTabHover = System.currentTimeMillis() + randomBetween(35_000, 90_000);
        nextFakeMisclick = System.currentTimeMillis() + randomBetween(60_000, 150_000);
        nextCameraRotate = System.currentTimeMillis() + randomBetween(75_000, 200_000);
    }

    @Override
    public int onLoop() {
        fishingHandler.onLoop(selectedFish, selectedLocation, bankingEnabled);

        long now = System.currentTimeMillis();

        if (now > nextMouseWiggle) {
            Point currentPos = org.dreambot.api.input.Mouse.getPosition();
            int wiggleX = currentPos.x + randomBetween(-50, 50);
            int wiggleY = currentPos.y + randomBetween(-30, 30);
            HumanMouseMover.moveMouse(new Point(wiggleX, wiggleY));
            log("Anti-ban: Mouse wiggle.");
            sleep(randomBetween(140, 400));
            nextMouseWiggle = now + randomBetween(15_000, 60_000);
        }

        if (now > nextFakeRightClick) {
            int clickX = randomBetween(100, 500);
            int clickY = randomBetween(100, 350);
            HumanMouseMover.moveMouse(new Point(clickX, clickY));
            sleep(randomBetween(90, 220));
            org.dreambot.api.input.Mouse.click(true);
            log("Anti-ban: Fake right-click menu.");
            sleep(randomBetween(140, 300));
            nextFakeRightClick = now + randomBetween(40_000, 120_000);
        }

        if (now > nextOffScreen) {
            int offScreenX = -200 + randomBetween(-60, 40);
            int offScreenY = -200 + randomBetween(-40, 40);
            HumanMouseMover.moveMouse(new Point(offScreenX, offScreenY));
            log("Anti-ban: Mouse moved off screen.");
            sleep(randomBetween(1000, 3500));
            nextOffScreen = now + randomBetween(70_000, 200_000);
        }

        if (now > nextTabHover) {
            int[] tabXs = {570, 610, 650, 690};
            int tabY = randomBetween(295, 320);
            int idx = randomBetween(0, tabXs.length - 1);
            HumanMouseMover.moveMouse(new Point(tabXs[idx], tabY));
            log("Anti-ban: Hovered a game tab.");
            sleep(randomBetween(200, 550));
            nextTabHover = now + randomBetween(35_000, 90_000);
        }

        if (now > nextFakeMisclick) {
            int nearX = randomBetween(250, 350);
            int nearY = randomBetween(220, 360);
            HumanMouseMover.moveMouse(new Point(nearX, nearY));
            log("Anti-ban: Fake misclick (near fishing spot)");
            sleep(randomBetween(90, 200));
            int correctX = nearX + randomBetween(-15, 15);
            int correctY = nearY + randomBetween(-15, 15);
            HumanMouseMover.moveMouse(new Point(correctX, correctY));
            log("Anti-ban: Adjusted after fake misclick");
            sleep(randomBetween(70, 120));
            org.dreambot.api.input.Mouse.click(false);
            nextFakeMisclick = now + randomBetween(60_000, 150_000);
        }

        if (now > nextCameraRotate) {
            int yaw = randomBetween(-180, 180);
            int pitch = randomBetween(30, 90);
            Camera.rotateToYaw(yaw);
            sleep(randomBetween(180, 400));
            Camera.rotateToPitch(pitch);
            log("Anti-ban: Camera rotated (API, humanized).");
            nextCameraRotate = now + randomBetween(75_000, 200_000);
        }

        sleep(randomBetween(150, 350));
        return 200;
    }

    @Override
    public void onPaint(Graphics g) {
        if (skillOverlay != null)
            skillOverlay.onPaint(g);
    }

    private int randomBetween(int min, int max) {
        return min + (int)(Math.random() * (max - min + 1));
    }
}
