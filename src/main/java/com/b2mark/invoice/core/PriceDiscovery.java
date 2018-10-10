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
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PriceDiscovery {

    @Qualifier("eurekaClient")
    @Autowired
    private EurekaClient eurekaClient;
    private final RestTemplate restTemplate;

    public PriceDiscovery(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public Price getPrice(Coin payer, Coin merchant, String driver) {
        Application application = eurekaClient.getApplication("PRICEDISCOVERY");
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String host = instanceInfo.getHostName();
        int port = instanceInfo.getPort();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getUrl(host,port)).append(payer.getSymbol().toLowerCase()).append(merchant.getSymbol().toLowerCase());
        Price price = null;
        try {
            price = restTemplate.getForObject(stringBuilder.toString(), Price.class);
        } catch (Exception ex) {
            System.out.println("JEUSDEBUG: ERROR   " + ex.getMessage());
        }
        System.out.println(price);
        return price;
    }


    private String getUrl(String host,int port) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("http://").append(host).append(":").append(port).append("/price/");
        return stringBuilder.toString();
    }

}
