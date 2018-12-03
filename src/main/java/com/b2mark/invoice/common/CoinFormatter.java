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

    /**
     * convert value to other value
     * e.g USDIRR, you have USD and will know how much IRR
     * USD(a),USD_IRR(b),AMOUNT(c),RESULT(r = b x c)
     *  1, 100000, 10, 100000 x 10 = 1000000 IRR 
     *  1, 100000, 20, 100000 x 20 = 2000000 IRR 
     * USD(a),USD_IRR(b),AMOUNT(c),RESULT(r = b x c)
     *  1, 0.00001, 10, 0.00001 x 10 = 0.00010
     * @param payValue paycoin value
     * @param exRate exchange rate.
     * @return get coin value.
     */
    public static BigDecimal convertCurrency(String payValue, String exRate) {

        BigDecimal exRateDecimal = new BigDecimal(exRate);
        BigDecimal payerValueDecimal = new BigDecimal(payValue);

        BigDecimal result = payerValueDecimal.multiply(exRateDecimal, MathContext.DECIMAL128);

        return result;

    }


    public static BigInteger amountInteger(Coin payerCoin, String sellAmount, String price) {
        BigDecimal bigDecimal = convertCurrency( sellAmount, price);
       return convrtDecimalToInt(payerCoin, bigDecimal);

    }


    public static BigInteger convrtDecimalToInt(Coin payerCoin, BigDecimal bigDecimal) {
        BigDecimal bdminUnit = new BigDecimal(Math.pow(10, payerCoin.getMinUnit()), MathContext.DECIMAL64);
        return bigDecimal.setScale(payerCoin.getMinUnit(), RoundingMode.UP).multiply(bdminUnit).toBigInteger();

    }

    private static BigDecimal getScaled(Coin payerCoin, BigDecimal bigDecimal) {
        return bigDecimal.setScale(payerCoin.getMinUnit(), RoundingMode.UP);
    }




    public static BigDecimal convrtIntToDecimal(Coin payerCoin, BigInteger bigInteger) {
        BigDecimal bigDecimalBase = new BigDecimal(bigInteger);
        BigDecimal bdminUnit = new BigDecimal(Math.pow(10, -payerCoin.getMinUnit()), MathContext.DECIMAL64);
        return bigDecimalBase.multiply(bdminUnit);

    }

}
