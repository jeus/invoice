/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.core;


import com.b2mark.invoice.exception.BadRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;

@Service
public class PriceDiscovery {
    private final String btcUsdPriceApi = "https://api.coinmarketcap.com/v2/ticker/?convert=BTC&limit=1";
    private final RestTemplate restTemplate;
    private final String usdRialPriceApi = "http://staging1.b2mark.com/api/";
    private final String newInvoiceApi = "http://79.137.5.197:32793/btc/InvoicePayment/NewInvoicePayment";
    private final String statusApi = "http://79.137.5.197:32793/btc/InvoicePayment/InvoiceDetailsByInvoiceId/10040";


    public PriceDiscovery(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public long getRialToSatoshi(long rial) {
        try {
            String btcUsdStr = getBtcPrice();
            String rialUsdStr = getRialPrice();
            float usdfloat = Float.parseFloat(btcUsdStr);
            long usdLong = (long) usdfloat;
            long rialUsdLong = Long.parseLong(rialUsdStr);
            long satoshi1 = (long) Math.pow(10, 8);
            long usdRial1 = usdLong * rialUsdLong;
            float btc = (float) rial / usdRial1;
            long satoshi = (long) (btc * satoshi1);
            return satoshi;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public double getRialToBtc(long rial) {
        try {
            String btcUsdStr = getBtcPrice();
            String rialUsdStr = getRialPrice();
            float usdfloat = Float.parseFloat(btcUsdStr);
            long usdLong = (long) usdfloat;
            long rialUsdLong = Long.parseLong(rialUsdStr);
            long usdRial1 = usdLong * rialUsdLong;
            float btc = (float) rial / usdRial1;
            return btc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public long getUsdToSatoshi(long usd) {

        return 0;
    }

    public float getUsdToBtc(long usd) {
        try {
            String btcUsdStr = getBtcPrice();
            float usdfloat = Float.parseFloat(btcUsdStr);
            long usdLong = (long) usdfloat;

            float btc = (float) usd / usdLong;
            return btc;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }


    private String getBtcPrice() throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity(btcUsdPriceApi, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode root1 = root.findPath("data");
        JsonNode root3 = root1.findPath("1");
        JsonNode root4 = root3.findPath("quotes");
        JsonNode root5 = root4.findPath("USD");
        JsonNode root6 = root5.findPath("price");
        String price = root6.asText();
        System.out.println(price);
        if (response.getStatusCodeValue() == 200)
            return price;
        else
            throw new BadRequest("Price discovery not work");
    }


    /**
     * <h2>newInvoicePayment</h2>
     * request to blockchain service to set new invoice and get bitcoin wallet address
     * Reques:
     * {"amount": 1100,
     * "invoiceId": "asdsadasd",
     * "coinSymbol": "TBTC"}
     * <p>
     * Response:
     * {"invoiceId": "asdsadasd",
     * "trasnactionId": "5cd71e90c782eb78f02b485e6eaaffdc54725db6844c8a31f8e4d941484f747a",
     * "requestStatus": "Verified",
     * "bitcoinAddress": "mz1m1BASDrEoeHutoz1ANJNkRvAuDRxnsy",
     * "expiredDateTime": "2018-09-02 22:21:07",
     * "depositeDateTime": "2018-09-02 10:22:34",
     * "createtedDateTiem": "2018-09-02 10:21:07",
     * "trasnactionConfirmation": 0,
     * "coinSymbol": "TBTC"}
     *
     * @param amount
     * @param invoiceId
     * @return
     */
    public String qrCode(long amount, long invoiceId) {
        try {
            double btc = getRialToBtc(amount);
            long satoshi = (long) (btc * Math.pow(10, 8));
            System.out.println("-----------------SATOSHI---" + satoshi);
            System.out.println("-----------------BTC---" + btc);
            RequestAddress requestAddress = new RequestAddress();
            requestAddress.setAmount(satoshi);
            requestAddress.setInvoiceId(invoiceId + "");
            requestAddress.setCoinSymbol("BTC");
            ObjectMapper mapper1 = new ObjectMapper();
            String invoiceJsonReq = mapper1.writeValueAsString(requestAddress);
            System.out.println("=========================********========================");
            System.out.println(invoiceJsonReq);
            System.out.println("=========================********========================");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(invoiceJsonReq, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(newInvoiceApi, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode root1 = root.findPath("bitcoinAddress");
            String wallet = root1.asText();
            System.out.println(wallet);
            String qrCodeStr = "bitcoin:%s?amount=%s";
            StringBuilder sbuf = new StringBuilder();
            Formatter fmt = new Formatter(sbuf);


            NumberFormat formatter = new DecimalFormat("#0.00000000");
            String btcStr = formatter.format(btc);
            System.out.println("---------" + btcStr);


            fmt.format(qrCodeStr, wallet, btcStr);
            if (response.getStatusCodeValue() == 200)
                return sbuf.toString();
            else
                throw new BadRequest("get address not work");
        } catch (Exception e) {
            throw new BadRequest("get address not work" + e.getCause() + "   ----    " + e.getMessage());
        }
    }


    public String getStatus(long invoiceId) {
        try {
            RequestAddress requestAddress = new RequestAddress();
            ObjectMapper mapper1 = new ObjectMapper();
            String invoiceJsonReq = mapper1.writeValueAsString(requestAddress);
            System.out.println("=========================********========================");
            System.out.println(invoiceJsonReq);
            System.out.println("=========================********========================");

            HttpHeaders headers = new HttpHeaders();
            ResponseEntity<String> response = restTemplate.getForEntity(statusApi, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode root1 = root.findPath("requestStatus");
            String wallet = root1.asText();
            System.out.println(wallet);
            String qrCodeStr = "bitcoin:%s?amount=%s";
            StringBuilder sbuf = new StringBuilder();
            Formatter fmt = new Formatter(sbuf);
            if (response.getStatusCodeValue() == 200)
                return sbuf.toString();
            else
                throw new BadRequest("get address not work");
        } catch (Exception e) {
            throw new BadRequest("get address not work" + e.getCause() + "   ----    " + e.getMessage());
        }
    }


    @Setter
    @Getter
    public class RequestAddress {
        long amount;
        String invoiceId;
        String coinSymbol;
    }

    private String getRialPrice() throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity(usdRialPriceApi, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String price = root.findPath("price").asText();
        System.out.println(price);
        if (response.getStatusCodeValue() == 200)
            return price;
        else
            throw new BadRequest("Price discovery not work");
    }

}
