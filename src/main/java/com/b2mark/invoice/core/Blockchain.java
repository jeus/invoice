/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.core;

import com.b2mark.invoice.common.CoinFormatter;
import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.entity.blockchain.BlockchainInvoice;
import com.b2mark.invoice.entity.price.Price;
import com.b2mark.invoice.exception.PublicException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Formatter;


@Service
public class Blockchain {
    private final static ApiConfig BTCAPICONFIG = new ApiConfig("http://79.137.5.197:32793/invoice", "Z68wQ6h2TKBYURwIGUiqeSYVSFBbyITwFK9xeJtvaTBIL95s72N50D73S5ymd3Bb");
    private final static ApiConfig ETHAPICONFIG = new ApiConfig("http://79.137.5.197:32893/invoice", "QiFMdCnGUwBDndqxeapoSWHD39uL84iUDWWv9Zs3GuZMzqJF9XXCorz3yPaInetU");
    private final static String STATUS = "/detail";

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
     * @param sellAmount
     * @param invoiceId
     * @return
     */
    public String qrCode(Coin payerCoin, Coin merchantCoin, String sellAmount, long invoiceId) {
        try {
            Price price = priceDiscovery.getPrice(payerCoin, merchantCoin, "GENERAL");

            BigDecimal bigDecimalAmount = CoinFormatter.amountDecimal(payerCoin, sellAmount, price.getPrice());
            BigInteger bigIntegerAmount = CoinFormatter.amountInteger(payerCoin, bigDecimalAmount);

            //HEADER
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", getAPI(payerCoin).getApiKey());

            HttpEntity<String> entity = new HttpEntity<String>(getBlockchainRequest(bigIntegerAmount, invoiceId, payerCoin), headers);
            String url = getAPI(payerCoin).getApiHost();
            BlockchainInvoice bInvoice = restTemplate.postForObject(url, entity, BlockchainInvoice.class);
            System.out.println("JEUSDEUG=> WALLET ADDRESS:" + bInvoice.getAddress());

            return CoinFormatter.getQrcode(payerCoin, bInvoice.getAddress(), bigDecimalAmount);
        } catch (Exception e) {
            throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "get qrcode address error " + e.getCause() + "   ----    " + e.getMessage());
        }
    }


    public String getStatus(long invoiceId, Coin coin) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", getAPI(coin).getApiKey());
            HttpEntity<?> entity = new HttpEntity<>(headers);


            StringBuilder builder = new StringBuilder();
            builder.append(getAPI(coin).getApiHost()).append("/detail/").append(invoiceId);
            String url = builder.toString();
            HttpEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            BlockchainInvoice bInvoice = mapper.readValue(response.getBody(), BlockchainInvoice.class);
            return bInvoice.getRequestStatus();
        } catch (Exception e) {
            throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "Get Status not work " + e.getCause() + "   ----    " + e.getMessage());
        }
    }


    private String getBlockchainRequest(BigInteger bIMinUnit, long invoiceId, Coin payerCoin) {
        BlockchainRequestAddress requestAddress = new BlockchainRequestAddress();
        requestAddress.setAmount(bIMinUnit.toString());
        requestAddress.setInvoiceId(invoiceId + "");
        requestAddress.setCoinSymbol(payerCoin.getSymbol());
        System.out.println(requestAddress.toString());

        ObjectMapper mapper1 = new ObjectMapper();
        String stringRequestAddress = null;
        try {
            stringRequestAddress = mapper1.writeValueAsString(requestAddress);
        } catch (JsonProcessingException e) {
            throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "Object not valid");
        }
        return stringRequestAddress;
    }


    private ApiConfig getAPI(Coin coin) {
        switch (coin) {
            case BITCOIN:
                return BTCAPICONFIG;
            case TBITCOIN:
                return BTCAPICONFIG;
            case ETHEREUM:
                return ETHAPICONFIG;
            default:
                throw new PublicException(ExceptionsDictionary.UNSUPPORTEDCOIN, "This Coin is not support");
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ApiConfig {
        private final String apiHost;
        private final String apiKey;
    }


    @Setter
    @Getter
    private class BlockchainRequestAddress {
        String amount;
        String invoiceId;
        String coinSymbol;

        public String toString() {
            Gson json = new Gson();
            return json.toJson(this);
        }

    }

}
