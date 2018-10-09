/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.core;


import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.entity.price.Price;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PriceDiscovery {
    private static final String lastPriceDiscovery = "http://localhost:8080/price/";
    private final RestTemplate restTemplate;

    public PriceDiscovery(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public Price getPrice(Coin payer,Coin merchant,String driver) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(lastPriceDiscovery).append(payer.getSymbol()).append(merchant.getSymbol());
        Price price = null;
        try {
            price = restTemplate.getForObject(stringBuilder.toString().toLowerCase(), Price.class);
        } catch (Exception ex) {
            System.out.println("JEUSDEBUG: ERROR   " + ex.getMessage());
        }
        System.out.println(price);
        return price;
    }
}
