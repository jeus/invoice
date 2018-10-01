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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;


@Service
public class Blockchain {
    private final String newInvoiceApi = "http://79.137.5.197:32793/btc/invoicepayment/new";
    private final String statusApi = "http://79.137.5.197:32793/btc/invoicepayment/details";
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
            System.out.println("JEUSDEBUG: coin:"+coinSymbol+" amount:"+amount+" invoiceId:"+invoiceId);
            double btc = priceDiscovery.getRialToBtc(amount);
            long satoshi = (long) (btc * Math.pow(10, 8));
            RequestAddress requestAddress = new RequestAddress();
            requestAddress.setAmount(satoshi);
            requestAddress.setInvoiceId(invoiceId + "");
            requestAddress.setCoinSymbol(coinSymbol);
            ObjectMapper mapper1 = new ObjectMapper();
            String invoiceJsonReq = mapper1.writeValueAsString(requestAddress);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(invoiceJsonReq, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(newInvoiceApi, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode root1 = root.findPath("bitcoinAddress");
            String wallet = root1.asText();
            System.out.println("JEUSDEUG=> WALLET ADDRESS:" + wallet);
            String qrCodeStr = "bitcoin:%s?amount=%s";
            StringBuilder sbuf = new StringBuilder();
            Formatter fmt = new Formatter(sbuf);

            System.out.println("JEUSDEBUG:=>BTC FROM BLOCKCHAIN:" + btc);
            System.out.println("JEUSDEBUG:=>SATOSHI FROM BLOCKCHAIN:" + satoshi);

            NumberFormat formatter = new DecimalFormat("#0.00000000");
            formatter.setRoundingMode(RoundingMode.DOWN);
            String btcStr = formatter.format(btc);
            System.out.println("JEUSDEBUG:=>BTC AFTER FORMAT:" + btcStr);


            fmt.format(qrCodeStr, wallet, btcStr);
            if (response.getStatusCodeValue() == 200)
                return sbuf.toString();
            else
                throw new BadRequest("get qrcode address error not Response" + response.getStatusCodeValue());
        } catch (Exception e) {
            throw new BadRequest("get qrcode address error " + e.getCause() + "   ----    " + e.getMessage());
        }
    }


    public String getStatus(long invoiceId) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(statusApi + "/" + invoiceId, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode root1 = root.findPath("requestStatus");
            String status = root1.asText();
            if (response.getStatusCodeValue() == 200)
                return status;
            else
                throw new BadRequest("get status invalid ");
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
    }


}
