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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;


@Service
public class PriceDiscovery {
    private final String btcUsdPriceApi = "https://api.coinmarketcap.com/v2/ticker/?convert=BTC&limit=1";
    private final String usdRialPriceApi = "http://my.becopay.com/api/";
    private final RestTemplate restTemplate;


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
