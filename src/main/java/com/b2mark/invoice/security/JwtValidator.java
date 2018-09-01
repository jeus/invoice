/**
 * @author b2mark
 * @version 1.0
 * @since 2018
 */
package com.b2mark.invoice.security;


import com.b2mark.invoice.entity.jwt.JwtUser;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;


@Component
public class JwtValidator {


    private String secret = "secret";

    public JwtUser validate(String token) {

        JwtUser jwtUser = null;
        try {

            //TODO: this Line Have to Delete VVVVVVVVVVVVVVVVVVV
            ObjectMapper mapper = new ObjectMapper();
            jwtUser = mapper.readValue(token, JwtUser.class);

            Claims claims = Jwts.claims()
                    .setSubject(jwtUser.getSub());
            claims.put("userId", String.valueOf(jwtUser.getPreferredUsername()));
            claims.put("role", jwtUser.getRealmAccess().getRoles());


            String token1 = Jwts.builder()
                    .setClaims(claims)
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact();

            //TODO: to here have to delete ^^^^^^^^^^^^^^^^^^^^ :))))) Crazy Codew to pass demo


            Claims body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token1)
                    .getBody();

        } catch (JsonGenerationException e1) {
            System.err.println(e1);
        } catch (JsonMappingException e2) {
            System.err.println(e2);
        } catch (Exception e) {
            System.out.println(e);
        }

        return jwtUser;
    }
}
