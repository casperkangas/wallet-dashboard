package utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class CurrencyFormatter {
    public static String format(BigDecimal amount, String currencyCode) {
        if (amount == null) return "";
        if (currencyCode == null || currencyCode.isEmpty()) {
            currencyCode = "EUR"; // default fallback
        }
        
        try {
            NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
            format.setCurrency(Currency.getInstance(currencyCode));
            return format.format(amount);
        } catch (Exception e) {
            // Fallback if currency code is invalid
            return amount.toString() + " " + currencyCode;
        }
    }
}
