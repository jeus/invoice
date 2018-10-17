package com.b2mark.invoice.common;

import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.exception.PublicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Formatter;


/**
 * <h1>ConinFormatter.</h1>
 * This class formatting coin by float nunber for create QR_Code
 * Format QR_Code e.g "bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00021095"
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
public class CoinFormatter {
    private static final Logger LOG = LoggerFactory.getLogger(CoinFormatter.class);


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
        fmt.format(qrCodeStr, payerCoin.getName().toLowerCase(), address, scaled.toString());
        return sbuf.toString();
    }


    public static BigDecimal amountDecimal(Coin payerCoin, String sellAmount, String price) {
        BigDecimal bigDecimalAmount = new BigDecimal(sellAmount);
        BigDecimal priceDecimal = new BigDecimal(price);
        BigDecimal rialPercent = priceDecimal.divide(bigDecimalAmount, RoundingMode.UP);
        BigDecimal unitPricePayer = new BigDecimal(1);
        return unitPricePayer.divide(rialPercent, MathContext.DECIMAL128);

    }


    public static BigInteger amountInteger(Coin payerCoin, String sellAmount, String price) {
        BigDecimal bigDecimal = amountDecimal(payerCoin, sellAmount, price);
       return amountInteger(payerCoin, bigDecimal);

    }


    public static BigInteger amountInteger(Coin payerCoin, BigDecimal bigDecimal) {
        BigDecimal bdminUnit = new BigDecimal(Math.pow(10, payerCoin.getMinUnit()), MathContext.DECIMAL64);
        return bigDecimal.setScale(payerCoin.getMinUnit(), RoundingMode.UP).multiply(bdminUnit).toBigInteger();

    }

    private static BigDecimal getScaled(Coin payerCoin, BigDecimal bigDecimal) {
        return bigDecimal.setScale(payerCoin.getMinUnit(), RoundingMode.UP);
    }


}
