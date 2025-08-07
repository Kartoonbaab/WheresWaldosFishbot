package handlers;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;
import util.HumanMouseMover;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class FishingHandler {
    // Track stats
    private long startTime = System.currentTimeMillis();
    private int startXP = Skills.getExperience(Skill.FISHING);
    private int startLvl = Skills.getRealLevel(Skill.FISHING);
    private int fishCaught = 0;
    private String currentFish = "None";
    private String currentLocation = "None";

    // Fish caught tracking fields
    private int lastFishCount = 0;
    private String[] lastFishNames = new String[0];

    private String status = "";

    // Areas
    private static final Area LUMBRIDGE = new Area(3242, 3150, 3246, 3157);
    private static final Area BARB_VILLAGE = new Area(3102, 3431, 3110, 3437);
    private static final Area KARAMJA = new Area(2918, 3177, 2927, 3182);
    private static final Area CATHERBY = new Area(2842, 3431, 2850, 3438);
    private static final Area FISHING_GUILD = new Area(2585, 3410, 2610, 3430);

    // fish type and required tools
    private static final Map<String, String[]> FISH_DATA = new HashMap<>();
    static {
        FISH_DATA.put("Shrimp", new String[]{"Small fishing net"});
        FISH_DATA.put("Trout", new String[]{"Fishing rod", "Feather"});
        FISH_DATA.put("Salmon", new String[]{"Fishing rod", "Feather"});
        FISH_DATA.put("Lobster", new String[]{"Lobster pot"});
        FISH_DATA.put("Swordfish", new String[]{"Harpoon"});
        FISH_DATA.put("Shark", new String[]{"Harpoon"});
    }

    public FishingHandler() {}

    public String getStatus() {
        return status;
    }

    public void onLoop(String selectedFish, String selectedLocation, boolean bankingEnabled) {
        currentFish = selectedFish;
        currentLocation = selectedLocation;

        if (!hasRequiredItems(currentFish)) {
            status = "Retrieving supplies from bank...";
            if (!withdrawRequiredItems(currentFish, bankingEnabled)) {
                status = "Missing required items, waiting...";
                return;
            }
        }

        Area fishingArea = getAreaForLocation(currentLocation);
        if (!fishingArea.contains(Players.getLocal())) {
            status = "Walking to " + currentLocation + " fishing area...";
            Walking.walk(fishingArea.getRandomTile());
            sleep(2500, 3000);
            return;
        }

        if (Inventory.isFull()) {
            if (bankingEnabled) {
                status = "Banking fish...";
                goBank();
            } else {
                status = "Dropping fish...";
                dropFish(currentFish);
            }
            return;
        }

        NPC fishingSpot = null;
        if (currentFish.equals("Swordfish")) {
            fishingSpot = NPCs.closest(n ->
                    n != null &&
                            n.getName().toLowerCase().contains("fishing spot") &&
                            n.hasAction("Harpoon") &&
                            n.hasAction("Cage"));
        } else if (currentFish.equals("Shark")) {
            fishingSpot = NPCs.closest(n ->
                    n != null &&
                            n.getName().toLowerCase().contains("fishing spot") &&
                            n.hasAction("Harpoon") &&
                            n.hasAction("Net"));
        } else {
            fishingSpot = NPCs.closest(n ->
                    n != null &&
                            n.getName().toLowerCase().contains("fishing spot") &&
                            n.hasAction(getFishAction(currentFish)));
        }

        if (fishingSpot != null && !Players.getLocal().isAnimating()) {
            status = "Fishing for " + currentFish + "...";
            Point spotPoint = fishingSpot.getClickablePoint();
            HumanMouseMover.moveMouse(spotPoint.x, spotPoint.y);
            fishingSpot.interact(getFishAction(currentFish));
            sleep(3000, 4000);
        } else {
            status = "Waiting for fishing spot...";
        }

        // Track fish caught
        String[] fishNames = getFishNames(currentFish);
        int totalNow = 0;
        for (String name : fishNames) {
            totalNow += Inventory.count(name);
        }
        if (!java.util.Arrays.equals(fishNames, lastFishNames)) {
            lastFishCount = totalNow;
            lastFishNames = fishNames;
        } else if (totalNow > lastFishCount) {
            fishCaught += (totalNow - lastFishCount);
            lastFishCount = totalNow;
        } else if (totalNow < lastFishCount) {
            lastFishCount = totalNow;
        }
    }

    private boolean hasRequiredItems(String fish) {
        String[] items = FISH_DATA.getOrDefault(fish, new String[]{});
        for (String item : items) {
            if (!Inventory.contains(item)) return false;
        }
        return true;
    }

    private boolean withdrawRequiredItems(String fish, boolean bankingEnabled) {
        if (!bankingEnabled) return false;

        String[] items = FISH_DATA.getOrDefault(fish, new String[]{});
        boolean missing = false;
        for (String item : items) {
            if (!Inventory.contains(item)) {
                missing = true;
                break;
            }
        }
        if (!missing) return true;

        BankLocation closest = BankLocation.getNearest(Players.getLocal().getTile());
        if (!closest.getArea(10).contains(Players.getLocal())) {
            Walking.walk(closest.getCenter());
            sleep(2500, 3500);
            return false;
        }
        if (Bank.open(Bank.getClosestBankLocation())) {
            sleepUntil(Bank::isOpen, 2500);
            boolean success = true;
            for (String item : items) {
                if (!Inventory.contains(item)) {
                    if (Bank.contains(item)) {
                        NPC bankNPC = NPCs.closest(bank -> bank != null && bank.getName().equalsIgnoreCase("Bank booth"));
                        if (bankNPC != null) {
                            Point bankPoint = bankNPC.getClickablePoint();
                            HumanMouseMover.moveMouse(bankPoint.x, bankPoint.y);
                        }
                        Bank.withdraw(item, 1);
                        sleep(400, 700);
                    } else {
                        System.out.println("Item not found in bank: " + item);
                        success = false;
                    }
                }
            }
            Bank.close();
            sleep(800, 1400);
            return success;
        }
        return false;
    }

    private void goBank() {
        BankLocation closest = BankLocation.getNearest(Players.getLocal().getTile());
        if (!closest.getArea(10).contains(Players.getLocal())) {
            Walking.walk(closest.getCenter());
            sleep(2500, 3500);
            return;
        }
        if (Bank.open(Bank.getClosestBankLocation())) {
            sleepUntil(Bank::isOpen, 2500);
            Bank.depositAllExcept(getFishingEquipment());
            Bank.close();
            sleep(1000, 1500);
        }
    }

    private void dropFish(String fish) {
        for (String f : getFishNames(fish)) {
            Inventory.dropAll(f);
        }
    }

    private String[] getFishNames(String fish) {
        switch (fish) {
            case "Shrimp": return new String[]{"Raw shrimp"};
            case "Trout": return new String[]{"Raw trout"};
            case "Salmon": return new String[]{"Raw salmon"};
            case "Lobster": return new String[]{"Raw lobster"};
            case "Swordfish": return new String[]{"Raw swordfish"};
            case "Shark": return new String[]{"Raw shark"};
        }
        return new String[0];
    }

    private String getFishAction(String fish) {
        switch (fish) {
            case "Shrimp": return "Net";
            case "Trout":
            case "Salmon": return "Lure";
            case "Lobster": return "Cage";
            case "Swordfish":
            case "Shark": return "Harpoon";
        }
        return "Net";
    }

    private Area getAreaForLocation(String location) {
        switch (location) {
            case "Lumbridge": return LUMBRIDGE;
            case "Barbarian Village": return BARB_VILLAGE;
            case "Karamja": return KARAMJA;
            case "Catherby": return CATHERBY;
            case "Fishing Guild": return FISHING_GUILD;
        }
        return LUMBRIDGE;
    }

    private String[] getFishingEquipment() {
        return new String[]{"Small fishing net", "Fishing rod", "Feather", "Lobster pot", "Harpoon"};
    }

    public long getStartTime() { return startTime; }
    public int getStartXP() { return startXP; }
    public int getStartLvl() { return startLvl; }
    public int getFishCaught() { return fishCaught; }
    public String getCurrentFish() { return currentFish; }
    public String getCurrentLocation() { return currentLocation; }

    private void sleep(int min, int max) {
        try {
            Thread.sleep(min + (int)(Math.random() * (max - min)));
        } catch (InterruptedException ignored) {}
    }

    private void sleepUntil(BooleanSupplier cond, int timeout) {
        long t = System.currentTimeMillis();
        while (!cond.getAsBoolean() && System.currentTimeMillis() - t < timeout) sleep(100, 200);
    }
}
