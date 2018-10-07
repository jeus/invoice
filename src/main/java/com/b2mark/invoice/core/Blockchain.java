/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.core;

import com.b2mark.invoice.entity.blockchain.BInvoice;
import com.b2mark.invoice.exception.BadRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;


@Service
public class Blockchain {
    private final String apiKey = "Z68wQ6h2TKBYURwIGUiqeSYVSFBbyITwFK9xeJtvaTBIL95s72N50D73S5ymd3Bb";
    private final String newInvoiceApi = "http://79.137.5.197:32793/invoice";
    private final String statusApi = "http://79.137.5.197:32793/invoice/details";
    private final RestTemplate restTemplate;
    @Autowired
    PriceDiscovery priceDiscovery;

    public Blockchain(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
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
    public String qrCode(String coinSymbol, long amount, long invoiceId) {
        try {
            System.out.println("JEUSDEBUG: coin:" + coinSymbol + " amount:" + amount + " invoiceId:" + invoiceId);
            double btc = priceDiscovery.getRialToBtc(amount);
            long satoshi = (long) (btc * Math.pow(10, 8));
            RequestAddress requestAddress = new RequestAddress();
            requestAddress.setAmount(satoshi);
            requestAddress.setInvoiceId(invoiceId + "");
            requestAddress.setCoinSymbol(coinSymbol);
            System.out.println(requestAddress.toString());
            ObjectMapper mapper1 = new ObjectMapper();
            String invoiceJsonReq = mapper1.writeValueAsString(requestAddress);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);
            HttpEntity<String> entity = new HttpEntity<String>(invoiceJsonReq, headers);
            BInvoice bInvoice = restTemplate.postForObject(newInvoiceApi, entity, BInvoice.class);
            System.out.println("JEUSDEUG=> WALLET ADDRESS:" + bInvoice.getAddress());
            String qrCodeStr = "bitcoin:%s?amount=%s";
            StringBuilder sbuf = new StringBuilder();
            Formatter fmt = new Formatter(sbuf);
            System.out.println("JEUSDEBUG:=>BTC FROM BLOCKCHAIN:" + btc);
            System.out.println("JEUSDEBUG:=>SATOSHI FROM BLOCKCHAIN:" + satoshi);
            NumberFormat formatter = new DecimalFormat("#0.00000000");
            formatter.setRoundingMode(RoundingMode.DOWN);
            String btcStr = formatter.format(btc);
            System.out.println("JEUSDEBUG:=>BTC AFTER FORMAT:" + btcStr);
            fmt.format(qrCodeStr, bInvoice.getAddress(), btcStr);
            return sbuf.toString();
        } catch (Exception e) {
            throw new BadRequest("get qrcode address error " + e.getCause() + "   ----    " + e.getMessage());
        }
    }


    public String getStatus(long invoiceId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKey);
            StringBuilder builder = new StringBuilder();
            builder.append(statusApi).append("/").append(invoiceId);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            HttpEntity<String> response = restTemplate.exchange(builder.toString(), HttpMethod.GET, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            BInvoice bInvoice = mapper.readValue(response.getBody(), BInvoice.class);
            return bInvoice.getRequestStatus();
        } catch (Exception e) {
            throw new BadRequest("Get Status not work " + e.getCause() + "   ----    " + e.getMessage());
        }
    }


    @Setter
    @Getter
    public class RequestAddress {
        long amount;
        String invoiceId;
        String coinSymbol;
        public String toString(){
            Gson json = new Gson();
            return json.toJson(this);
        }

    }


}
