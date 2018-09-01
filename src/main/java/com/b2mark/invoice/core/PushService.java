/**
 * <h1></h1>
 *
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
//TODO: this class have to create new services for send all push to mobiles.
package com.b2mark.invoice.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PushService {
    public static ObjectMapper mapper = new ObjectMapper();

//     Insert your Secret API Key here
//    public static final String SECRET_API_KEY = "SECRET_API_KEY";
//
//    public static void sendPush(PushyPushRequest req) throws Exception {
//        // Get custom HTTP client
//        HttpClient client = new DefaultHttpClient();
//
//        // Create POST request
//        HttpPost request = new HttpPost("https://api.pushy.me/push?api_key=" + SECRET_API_KEY);
//
//        // Set content type to JSON
//        request.addHeader("Content-Type", "application/json");
//
//        // Convert post data to JSON
//        byte[] json = mapper.writeValueAsBytes(req);
//
//        // Send post data as byte array
//        request.setEntity(new ByteArrayEntity(json));
//
//        // Execute the request
//        HttpResponse response = client.execute(request, new BasicHttpContext());
//
//        // Get response JSON as string
//        String responseJSON = EntityUtils.toString(response.getEntity());
//
//        // Convert JSON response into HashMap
//        Map<String, Object> map = mapper.readValue(responseJSON, Map.class);
//
//        // Got an error?
//        if (map.containsKey("error")) {
//            // Throw it
//            throw new Exception(map.get("error").toString());
//        }
//    }

    public static class PushyPushRequest {
        public Object to;
        public Object data;
        public Object notification;

        public PushyPushRequest(Object data, Object to, Object notification) {
            this.to = to;
            this.data = data;
            this.notification = notification;
        }
    }

}
