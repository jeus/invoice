/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.util.Formatter;

@Service
public class MtService {


    private final String priveteKey = "4755384C325A7232622B66662F41386D304E464F38366B31637851353575496E";
    private final RestTemplate restTemplate;
    private final String fooResourceUrl = "https://api.kavenegar.com/v1/%s/verify/lookup.json?receptor=%s&token=%s&template=%s";
    ;

    public MtService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public boolean validation1(String mobileNum, String token) {
        String template = "otp1";
        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        fmt.format(fooResourceUrl, priveteKey, mobileNum, token, template);
        System.out.print(sbuf.toString());
        try {
            return callValidation(sbuf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean callValidation(String url) throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode status = root.path("return").path("status");
        System.out.println(root.toString());
        if (status.equals("200"))
            return true;
        else
            System.out.println("ERROOOOOOOOOORRRRRR"+response.getStatusCodeValue());
            return false;
    }


}
