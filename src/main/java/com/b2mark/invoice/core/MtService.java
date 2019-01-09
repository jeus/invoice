

package com.b2mark.invoice.core;

import com.b2mark.common.exceptions.ExceptionsDictionary;
import com.b2mark.common.exceptions.PublicException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.Formatter;


/**
 * <h1>Mobile Terminated service</h1>
 * this service send sms to user
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
@Service
public class MtService {
    private static final Logger LOG = LoggerFactory.getLogger(MtService.class);


    private final String priveteKey = "4755384C325A7232622B66662F41386D304E464F38366B31637851353575496E";
    private final RestTemplate restTemplate;
    private final String fooResourceUrl = "https://api.kavenegar.com/v1/%s/verify/lookup.json?receptor=%s&token=%s&template=%s";

    public MtService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public boolean validation1(String mobileNum, String token,String template) {
        StringBuilder strBuilder = new StringBuilder();
        Formatter formatter = new Formatter(strBuilder);
        formatter.format(fooResourceUrl, priveteKey, mobileNum, token, template);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(strBuilder.toString(), String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode status = root.path("return").path("status");
            if (status.toString().equals("200")) {
                LOG.info("action:MT,mobile_number:{},token:#,template:{}", mobileNum, template);
                return true;
            } else {
                LOG.info("action:MT,mobile_number:{},token:#,template:{}", mobileNum, template);
                throw new PublicException(ExceptionsDictionary.UNDEFINEDERROR, "Exception at send sms");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
