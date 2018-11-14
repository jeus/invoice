package com.b2mark.invoice.core;


import com.b2mark.invoice.common.enums.Coin;
import com.b2mark.invoice.common.exceptions.ExceptionsDictionary;
import com.b2mark.invoice.entity.price.Price;
import com.b2mark.invoice.exception.PublicException;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
@Service
public class PriceDiscovery {
    private static final Logger LOG = LoggerFactory.getLogger(PriceDiscovery.class);
    private final EurekaClient eurekaClient;
    private final RestTemplate restTemplate;

    @Autowired
    public PriceDiscovery(RestTemplateBuilder restTemplateBuilder, @Qualifier("eurekaClient") EurekaClient eurekaClient) {
        this.restTemplate = restTemplateBuilder.build();
        this.eurekaClient = eurekaClient;
    }

    Price getPrice(Coin payerCoin, Coin merchantCoin, String driver) {
        Application application = eurekaClient.getApplication("PRICEDISCOVERY");
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String host = instanceInfo.getHostName();
        int port = instanceInfo.getPort();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getUrl(host, port)).append(payerCoin.getSymbol().toLowerCase()).append(merchantCoin.getSymbol().toLowerCase());
        Price price;
        try {
            price = restTemplate.getForObject(stringBuilder.toString(), Price.class);
        } catch (Exception ex) {
            throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "price discovery error");
        }
        LOG.info("action:PriceDiscovery,payer_coin:{},merchant_coin:{},driver:{},price:{}", payerCoin.getSymbol(), merchantCoin.getSymbol(), driver, price);
        return price;
    }


    private String getUrl(String host, int port) {
      return  "http://"+host+":"+port+"/price/";
    }

}
