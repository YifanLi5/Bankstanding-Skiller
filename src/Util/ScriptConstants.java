package Util;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ScriptConstants {

    // if combinationType is _1_27, itemA is the tool (Knife), itemB is the consumable (logs).
    // if combinationType is _1_X_26, item A is the tool (needle), itemB is the consumable (leather), itemC is the secondary consumable (thread)
    public static int itemA_id = -1;
    public static int itemB_id = -1;

    public static int itemC_id = -1;

    public static int getItemA_id() {
        return itemA_id;
    }

    public static void setItemA_id(int itemA_id) {
        ScriptConstants.itemA_id = itemA_id;
    }

    public static int getItemB_id() {
        return itemB_id;
    }

    public static void setItemB_id(int itemB_id) {
        ScriptConstants.itemB_id = itemB_id;
    }

    public enum CombinationType {
        _14_14,     // ex: longbow (u) + bow string
        _1_27,      // ex: knife + logs
        _1_X_26,    // ex: needle + thread + leather
    }

    public static CombinationType combinationType;

    /**
     * Inventory slots visual guide
     * 0  1  2  3
     * 4  5  6  7
     * 8  9 10 11
     * 12 13 14 15
     * 16 17 18 19
     * 20 21 22 23
     * 24 25 26 27
     */
    private static final Tuple<int[], Integer>[] _1_27_InvSlotPairs = new Tuple[]{
            new Tuple<>(new int[]{0, 1}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{0, 4}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{0, 5}, ThreadLocalRandom.current().nextInt(1, 10))
    };

    private static final Tuple<int[], Integer>[] _14_14_InvSlotPairs = new Tuple[]{
            new Tuple<>(new int[]{12, 16}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{13, 17}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{10, 14}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{11, 15}, ThreadLocalRandom.current().nextInt(1, 10))
    };

    private static final Tuple<int[], Integer>[] _1_X_26_InvSlotPairs = new Tuple[]{
            new Tuple<>(new int[]{0, 4}, 1),
    };

    public static int[] getInvSlotPair() {
        if(combinationType == null) {
            throw new NullPointerException("Must initialize combinationType");
        }
        Tuple<int[], Integer>[] slotPairArr = (combinationType == CombinationType._14_14) ?
                _14_14_InvSlotPairs :
                _1_27_InvSlotPairs;

        int weightingSum = Arrays.stream(slotPairArr).mapToInt(Tuple::getSecond).sum();
        int roll = ThreadLocalRandom.current().nextInt(weightingSum);
        int idx = 0;
        for (; idx < slotPairArr.length; idx++) {
            roll -= slotPairArr[idx].getSecond();
            if (roll < 0) {
                break;
            }
        }
        return slotPairArr[idx].getFirst();
    }


    public static final String USE = "Use";

    public static final int SESSION_MEAN;

    public static final int SESSION_STD_DEV;


    static {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        SESSION_MEAN = current.nextInt(3500, 6500);
        SESSION_STD_DEV = current.nextInt(1000, 2000);
    }

    public static int randomSessionGaussian() {
        return (int) Math.abs((new Random().nextGaussian() * SESSION_STD_DEV + SESSION_MEAN));
    }

    public static int randomGaussian(int mean, int std_dev) {
        return (int) Math.abs((new Random().nextGaussian() * std_dev + mean));
    }
}
