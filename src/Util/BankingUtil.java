package Util;

import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.script.MethodProvider;

public class BankingUtil {

    private static final int BANK_ROOT_ID = 12;
    private static final int SELECTED_BANK_QUANTITY_WIDGET_ID = 1666;
    private static final int X_AMOUNT_CONFIG_ID = 304;
    private static final int X_AMOUNT_CONFIG_VALUE_14 = 28;
    public enum BankingQuantityWidgetOptions {
        ONE(0), FIVE(4), TEN(8), X(12), ALL(16);

        final int configValue;
        BankingQuantityWidgetOptions(int configValue) {
            this.configValue = configValue;
        }
    }
    public static boolean setBankingQuantityOption(MethodProvider methods, BankingQuantityWidgetOptions bankingWidgetOption) throws InterruptedException {
        if(!methods.bank.isOpen()) {
            methods.warn("bank is not open");
            return false;
        }
        boolean result = methods.configs.isSet(SELECTED_BANK_QUANTITY_WIDGET_ID, bankingWidgetOption.configValue);
        if(result) {
            return true;
        }

        RS2Widget bankQuantityWidget = null;
        switch(bankingWidgetOption) {
            case ONE:
                bankQuantityWidget = methods.widgets.getWidgetContainingText(BANK_ROOT_ID, "1");
                break;
            case FIVE:
                bankQuantityWidget = methods.widgets.getWidgetContainingText(BANK_ROOT_ID, "5");
                break;
            case TEN:
                bankQuantityWidget = methods.widgets.getWidgetContainingText(BANK_ROOT_ID, "10");
                break;
            case X:
                bankQuantityWidget = methods.widgets.getWidgetContainingText(BANK_ROOT_ID, "X");
                break;
            case ALL:
                bankQuantityWidget = methods.widgets.getWidgetContainingText(BANK_ROOT_ID, "ALL");
                break;
        }

        if (bankQuantityWidget == null) {
            methods.warn(String.format("Unable to find widget for banking quantity (%s)", bankingWidgetOption.name()));
            return false;
        }

        WidgetDestination widgetDestination = new WidgetDestination(methods.bot, bankQuantityWidget);
        return RetryUtil.retry(() -> methods.mouse.click(widgetDestination), 3, 1000)
                && RetryUtil.retry(() -> methods.configs.isSet(SELECTED_BANK_QUANTITY_WIDGET_ID, bankingWidgetOption.configValue), 5, 1000);
    }

}
