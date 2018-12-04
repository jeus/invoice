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

    /**
     *
     * @param baseCoin this coin is 1 unit. e.g 1 USD (1$) is equal to 110000 IRR (Iran Rial)
     * @param toCoin this coin is not 1 unit. e.g base is USD (1$) and to IRR return 110000 IRR.
     * @param driver Price discovery drive. For example GENERAL.
     * @return
     */
    public Price getPrice(Coin baseCoin, Coin toCoin, String driver) {
        Application application = eurekaClient.getApplication("PRICEDISCOVERY");
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String host = instanceInfo.getHostName();
        int port = instanceInfo.getPort();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getUrl(host, port)).append(baseCoin.getSymbol().toLowerCase()).append(toCoin.getSymbol().toLowerCase());
        Price price;
        try {
            price = restTemplate.getForObject(stringBuilder.toString(), Price.class);
        } catch (Exception ex) {
            throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "price discovery error");
        }
        LOG.info("action:PriceDiscovery,baseCoin:{},priceCoin:{},driver:{},price:{}", baseCoin.getSymbol(), toCoin.getSymbol(), driver, price);
        return price;
    }

    private String getUrl(String host, int port) {
      return  "http://"+host+":"+port+"/price/";
    }

}
