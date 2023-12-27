package Util;

import org.osbot.rs07.api.def.ItemDefinition;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ScriptConstants {

    public static final String USE = "Use";
    public static final int SESSION_MEAN;
    public static final int SESSION_STD_DEV;
    /**
     * Inventory slots visual guide
     * 0  1  2  3
     * 4  5  6  7
     * 8  9  10 11
     * 12 13 14 15
     * 16 17 18 19
     * 20 21 22 23
     * 24 25 26 27
     */
    // For randomizing which item slots are used on each other.
    private static final Tuple<int[], Integer>[] _1_27_InvSlotPairs = new Tuple[]{
            new Tuple<>(new int[]{0, 1}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{1, 0}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{0, 4}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{4, 0}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{0, 5}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{5, 0}, ThreadLocalRandom.current().nextInt(1, 10))
    };
    private static final Tuple<int[], Integer>[] _14_14_InvSlotPairs = new Tuple[]{
            new Tuple<>(new int[]{12, 16}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{16, 12}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{13, 17}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{17, 13}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{10, 14}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{14, 10}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{11, 15}, ThreadLocalRandom.current().nextInt(1, 10)),
            new Tuple<>(new int[]{15, 11}, ThreadLocalRandom.current().nextInt(1, 10))
    };
    private static final Tuple<int[], Integer>[] _1_X_26_InvSlotPairs = new Tuple[]{
            new Tuple<>(new int[]{0, 4}, 1),
            new Tuple<>(new int[]{4, 0}, 1),
            new Tuple<>(new int[]{0, 5}, 1),
            new Tuple<>(new int[]{5, 0}, 1)
    };

    public static ItemDefinition itemA = null;
    public static ItemDefinition itemB = null;
    public static ItemDefinition itemC = null;
    public static CombinationType combinationType;

    static {
        ThreadLocalRandom current = ThreadLocalRandom.current();
        SESSION_MEAN = current.nextInt(3500, 6500);
        SESSION_STD_DEV = current.nextInt(1000, 2000);
    }

    public static int getItemC_Id() {
        return itemC != null ? itemC.getId() : -1;
    }

    public static int[] getInvSlotPair() {
        if (combinationType == null) {
            throw new NullPointerException("Must initialize combinationType");
        }

        Tuple<int[], Integer>[] slotPairArr;
        switch (combinationType) {
            case _1_27:
                slotPairArr = _1_27_InvSlotPairs;
                break;
            case _14_14:
                slotPairArr = _14_14_InvSlotPairs;
                break;
            case _1_X_26:
                slotPairArr = _1_X_26_InvSlotPairs;
                break;
            default:
                slotPairArr = null;
        }

        int weightingSum = Arrays.stream(slotPairArr).mapToInt(Tuple::getSecond).sum();
        int roll = ThreadLocalRandom.current().nextInt(weightingSum);
        int idx = 0;
        for (; idx < slotPairArr.length; idx++) {
            roll -= slotPairArr[idx].getSecond();
            if (roll < 0)
                break;
        }
        return slotPairArr[idx].getFirst();
    }

    public static int randomSessionGaussian() {
        return (int) Math.abs((new Random().nextGaussian() * SESSION_STD_DEV + SESSION_MEAN));
    }

    public static int randomGaussian(int mean, int std_dev) {
        return (int) Math.abs((new Random().nextGaussian() * std_dev + mean));
    }

    public enum CombinationType {
        _14_14,     // ex: longbow (u) + bow string
        _1_27,      // ex: knife + logs
        _1_X_26,    // ex: needle + thread + leather
    }
}
