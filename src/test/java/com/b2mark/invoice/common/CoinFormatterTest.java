package com.b2mark.invoice.common;

import org.junit.Test;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static org.junit.Assert.*;

public class CoinFormatterTest {

    private final String USDIRR = "150000";
    private final String IRRUSD = "0.00000666";
    private final String BTCIRR = "500000000";
    private final String IRRBTC = "0.000000002";

    /**
     * I have 1 USD to get IRR  exchange rate 150,000 IRR
     */
    @Test
    public void amountDecimal0() {
        String xrate = USDIRR;
        String value = "1";
        BigDecimal resultDecimal = CoinFormatter.convertCurrency(value, xrate);
        String exp = formatter(new BigDecimal("150000"));
        String result = formatter(resultDecimal);

        assertEquals("value is: " + value + "xchange rate is: " + xrate, exp, result);
    }

    /**
     * I have 1 IRR to get USD  xchange rate (IRRUSD) is 0.0000066
     */
    @Test
    public void amountDecimal1() {
        String xrate = IRRUSD;
        String value = "150000";
        BigDecimal resultDecimal = CoinFormatter.convertCurrency(value, xrate);
        String exp = formatter(new BigDecimal("0.999"));
        String result = formatter(resultDecimal);

        assertEquals("value is: " + value + "xchange rate is: " + xrate, exp, result);
    }


    /**
     * I have 1500000 IRR xchange rate (IRRUSD) is 0.0000066
     */
    @Test
    public void amountDecimal2() {
        String xrate = IRRUSD;
        String value = "1500000";
        BigDecimal resultDecimal = CoinFormatter.convertCurrency(value, xrate);
        String exp = formatter(new BigDecimal("9.99"));
        String result = formatter(resultDecimal);

        assertEquals("value is: " + value + "xchange rate is: " + xrate, exp, result);

    }

    /**
     * I have 75000 IRR and will change to USD
     */
    @Test
    public void amountDecimal3() {
        String xrate = IRRUSD;
        String value = "75000";
        BigDecimal resultDecimal = CoinFormatter.convertCurrency(value, xrate);
        String exp = formatter(new BigDecimal("0.4995"));
        String result = formatter(resultDecimal);

        assertEquals("value is: " + value + "xchange rate is: " + xrate, exp, result);

    }


    /**
     * I have 10 USD and will change to IRR
     */
    @Test
    public void amountDecimal4() {
        String xrate = USDIRR;
        String value = "10";
        BigDecimal resultDecimal = CoinFormatter.convertCurrency(value, xrate);
        String exp = formatter(new BigDecimal("1500000"));
        String result = formatter(resultDecimal);
        assertEquals("value is: " + value + "xchange rate is: " + xrate, exp, result);

    }


    /**
     * i have 15000000 IRR and will change to USD
     */
    @Test
    public void amountDecimal5() {
        String xrate = IRRUSD;
        String value = "15000000";
        BigDecimal resultDecimal = CoinFormatter.convertCurrency(value, xrate);
        String exp = formatter(new BigDecimal("99.9"));
        String result = formatter(resultDecimal);

        assertEquals("value is: " + value + "xchange rate is: " + xrate, exp, result);
    }


    /**
     * I Have 1 bitcoin and will change to IRR iran rial.
     */
    @Test
    public void amountDecimal6() {
        String xrate = BTCIRR;
        String value = "1";

        BigDecimal resultDecimal = CoinFormatter.convertCurrency(value, xrate);

        String exp = formatter(new BigDecimal("500000000"));
        String result = formatter(resultDecimal);

        assertEquals("value is: " + value + "xchange rate is: " + xrate, exp, result);
    }


    @Test
    public void amountInteger() {
    }


    private String formatter(BigDecimal bigDecimal) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(18);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);
        return df.format(bigDecimal);
    }
}