/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.common;

import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.exception.PublicException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Formatter;

public class CoinFormatter {


    public static String getQrcode(Coin payCoin, String address, BigDecimal amount) {
        String qrCodeStr;
        Coin payerCoin = payCoin;
        BigDecimal scaled;
        switch (payerCoin) {
            case BITCOIN:
                qrCodeStr = "%s:%s?amount=%s";
                scaled = getScaled(payerCoin,amount);
                break;
            case TBITCOIN:
                qrCodeStr = "%s:%s?amount=%s";
                payerCoin = Coin.BITCOIN;
                scaled = getScaled(payerCoin,amount);
                break;
            case ETHEREUM:
                qrCodeStr = "%s:%s?amount=%s";
                scaled = getScaled(payerCoin,amount);
                break;
            default:
                throw new PublicException(ExceptionsDictionary.UNSUPPORTEDCOIN, "this coin is not support");
        }

        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        System.out.println("JEUSDEBUG:=>BTC AFTER FORMAT:" + scaled.toString());
        fmt.format(qrCodeStr, payerCoin.getName().toLowerCase(), address, scaled.toString());
        return sbuf.toString();
    }


    public static BigDecimal amountDecimal(Coin payerCoin, String sellAmount, String price) {
        BigDecimal bigDecimalAmount = new BigDecimal(sellAmount);
        BigDecimal priceDecimal = new BigDecimal(price);
        BigDecimal rialPercent = priceDecimal.divide(bigDecimalAmount, RoundingMode.UP);
        BigDecimal unitPricePayer = new BigDecimal(1);
        BigDecimal bigDecimal = unitPricePayer.divide(rialPercent, MathContext.DECIMAL128);
        return bigDecimal;
    }


    public static BigInteger amountInteger(Coin payerCoin, String sellAmount, String price) {
        BigDecimal bigDecimal = amountDecimal(payerCoin, sellAmount, price);
        BigInteger bigIntegerAmount = amountInteger(payerCoin, bigDecimal);
        return bigIntegerAmount;
    }


    public static BigInteger amountInteger(Coin payerCoin, BigDecimal bigDecimal) {
        BigDecimal bdminUnit = new BigDecimal(Math.pow(10, payerCoin.getMinUnit()), MathContext.DECIMAL64);
        BigInteger bigIntegerAmount = bigDecimal.setScale(payerCoin.getMinUnit(), RoundingMode.UP).multiply(bdminUnit).toBigInteger();
        return bigIntegerAmount;
    }

    public static BigDecimal getScaled(Coin payerCoin, BigDecimal bigDecimal) {
        return bigDecimal.setScale(payerCoin.getMinUnit(), RoundingMode.UP);
    }


}
