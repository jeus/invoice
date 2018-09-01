package com.b2mark.invoice.entity.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JwtUserDetails implements UserDetails {

    private String token;
    private Collection<? extends GrantedAuthority> authorities;
private JwtUser jwtUser;

    public JwtUserDetails(JwtUser jwtUser ,String token) {

        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
                .commaSeparatedStringToAuthorityList(jwtUser.getRealmAccess().getRoles().stream().collect(Collectors.joining(",")));

        this.token= token;
        this.jwtUser = jwtUser;
        this.authorities = grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return jwtUser.getSub();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setJwtUser(JwtUser jwtUser)
    {
        this.jwtUser = jwtUser;
    }

    public JwtUser getJwtUser() {
        return jwtUser;
    }

    public String getUserName() {
        return jwtUser.getPreferredUsername();
    }

    public String getToken() {
        return token;
    }


    public String getId() {
        return jwtUser.getSub();
    }

}
