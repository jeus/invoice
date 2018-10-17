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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;



/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

@Service
public class Blockchain {
    private static final Logger LOG = LoggerFactory.getLogger(Blockchain.class);
    private final static ApiConfig BTCAPICONFIG = new ApiConfig("http://79.137.5.197:32793/invoice", "Z68wQ6h2TKBYURwIGUiqeSYVSFBbyITwFK9xeJtvaTBIL95s72N50D73S5ymd3Bb");
    private final static ApiConfig ETHAPICONFIG = new ApiConfig("http://79.137.5.197:32893/invoice", "QiFMdCnGUwBDndqxeapoSWHD39uL84iUDWWv9Zs3GuZMzqJF9XXCorz3yPaInetU");
    private final static String STATUS = "/detail";

    private final RestTemplate restTemplate;
    private final PriceDiscovery priceDiscovery;

    @Autowired
    public Blockchain(RestTemplateBuilder restTemplateBuilder, PriceDiscovery priceDiscovery) {
        this.restTemplate = restTemplateBuilder.build();
        this.priceDiscovery = priceDiscovery;
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
     * @param payerCoin coin that user pay
     * @param merchantCoin coin that merchant get
     * @param sellAmount price that merchant get by merchantCoin
     * @param invoiceId invoice id
     * @return QR_Code e.g bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00021095
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

            HttpEntity<String> entity = new HttpEntity<>(getBlockchainRequest(bigIntegerAmount, invoiceId, payerCoin), headers);
            String url = getAPI(payerCoin).getApiHost();
            BlockchainInvoice blockchainInvoice = restTemplate.postForObject(url, entity, BlockchainInvoice.class);
            if(blockchainInvoice == null)
                throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "blockchain address doesn't get please try again");
            LOG.info("action:Get_QR,payer_coin:{},merchant_coin:{},sell_amount:{},invoice_id:{},blockchain_address:{}",payerCoin.getSymbol(),merchantCoin.getSymbol(),sellAmount,invoiceId,blockchainInvoice.getAddress());
            return CoinFormatter.getQrcode(payerCoin, blockchainInvoice.getAddress(), bigDecimalAmount);
        } catch (Exception e) {
            throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "get qrcode address error " + e.getCause() + "   ----    " + e.getMessage());
        }
    }


    public String getStatus(long invoiceId, Coin coin) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ApiConfig apiConfig =  getAPI(coin);
            headers.set("Authorization",apiConfig.getApiKey());
            HttpEntity<?> entity = new HttpEntity<>(headers);
            String url = apiConfig.getApiHost()+"/detail/"+invoiceId;
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
        ObjectMapper mapper1 = new ObjectMapper();
        String stringRequestAddress;
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
