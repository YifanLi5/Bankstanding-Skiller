package Util;

import org.osbot.rs07.Bot;
import org.osbot.rs07.listener.GameTickListener;
import org.osbot.rs07.script.MethodProvider;

import java.util.concurrent.atomic.AtomicBoolean;

import static Util.ScriptConstants.itemB;

public class GameTickUtil extends MethodProvider implements GameTickListener {

    public static GameTickUtil globalRef;
    private final Bot bot;

    // Some animations such as stringing bows or making potions have downtime between cycles where the player's animation becomes -1
    // hasAnimatedRecently -> true if in the last 3 ticks the player has preformed an animation
    public boolean hasAnimatedRecently = false;
    public int animationCapacitor = 0;

    // Some combination actions don't have an animation, need to check if itemB's (the consumable) quantity decreased
    // inventoryHasChangedRecently -> true if # itemB decreased in the last 6 ticks
    public AtomicBoolean inventoryHasChangedRecently = new AtomicBoolean(false);
    public int inventoryChangeCapacitor = 0;
    public int lastItemBCount;

    private GameTickUtil(Bot bot) {
        this.bot = bot;
        exchangeContext(bot);
        bot.addGameTickListener(this);
        lastItemBCount = (int) inventory.getAmount(itemB.getId());
    }

    public static void createSingletonGlobalInstance(Bot bot) {
        if (globalRef == null) {
            globalRef = new GameTickUtil(bot);
        }
    }

    public void removeListener() {
        bot.removeGameTickListener(this);
    }

    @Override
    public void onGameTick() {
        if (myPlayer().isAnimating()) {
            hasAnimatedRecently = true;
            animationCapacitor = 3;
        } else {
            animationCapacitor -= 1;
        }
        if (animationCapacitor <= 0) {
            hasAnimatedRecently = false;
        }

        if (inventory.getAmount(itemB.getId()) < lastItemBCount) {

            inventoryChangeCapacitor = 6;
            inventoryHasChangedRecently.set(true);

        } else if (inventoryChangeCapacitor > 0) {
            inventoryChangeCapacitor -= 1;
        }
        lastItemBCount = (int) inventory.getAmount(itemB.getId());
        if (inventoryChangeCapacitor <= 0) {
            inventoryHasChangedRecently.set(false);
            lastItemBCount = (int) inventory.getAmount(itemB.getId());
        }
    }
}
