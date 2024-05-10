package Util;

import Paint.ScriptPaint;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.MethodProvider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static Util.ScriptConstants.*;

public class InventoryWatcher {

    static CountInventoryItemsProcessed task;
    private static ScheduledExecutorService scheduler;

    public static void startWatcher(MethodProvider methodProvider) {
        task = new CountInventoryItemsProcessed(methodProvider);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(task, 0, 500, TimeUnit.MILLISECONDS);
    }

    public static void shutdownWatcher() {
        scheduler.shutdown();
    }

    private static class CountInventoryItemsProcessed implements Runnable {
        private final MethodProvider methodProvider;
        private final Filter<Item> outputItemFilter = item ->
                item.getId() != itemA.getId() &&
                        item.getId() != itemB.getId() &&
                        item.getId() != getItemC_Id();
        private int lastCheck = 0;
        private int totalNumProcessed = 0;

        public CountInventoryItemsProcessed(MethodProvider methodProvider) {
            this.methodProvider = methodProvider;
        }

        @Override
        public void run() {
            int outputCount = (int) methodProvider.inventory.getAmount(outputItemFilter);
            if (outputCount >= lastCheck) {
                totalNumProcessed += outputCount - lastCheck;
                lastCheck = outputCount;
                ScriptPaint.setNumItemsProcessed(totalNumProcessed);
            } else {
                lastCheck = 0;
            }

        }
    }


}
