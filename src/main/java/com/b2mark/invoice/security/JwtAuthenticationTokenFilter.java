/**
 * @author b2mark
 * @version 1.0
 * @since 2018
 */

package com.b2mark.invoice.security;

import com.b2mark.invoice.entity.jwt.JwtAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthenticationTokenFilter extends AbstractAuthenticationProcessingFilter {

    public JwtAuthenticationTokenFilter() {
        super("/invoice/**");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest,
                                                HttpServletResponse httpServletResponse)
            throws AuthenticationException, IOException, ServletException {

        String header = httpServletRequest.getHeader("Authorisation");

       if(!httpServletRequest.getMethod().equals("OPTIONS")) {
           if (header == null || !header.startsWith("Token ")) {
               throw new RuntimeException("JWT Token is missing");
           }

           String authenticationToken = header.substring(6);
           JwtAuthenticationToken token = new JwtAuthenticationToken(authenticationToken);
           return getAuthenticationManager().authenticate(token);
       }

        String username = "";
        String password = "";
        List<GrantedAuthority> grantedAuths = new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ANONYMOUS"));
        return new UsernamePasswordAuthenticationToken(username, password, grantedAuths);

    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
