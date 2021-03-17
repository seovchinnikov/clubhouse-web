package edu.clubhouseapi.filter;

import edu.clubhouseapi.dto.SecurityUserJwtTokenBody;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomSpringAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final SecurityUserJwtTokenBody userJwtTokenBody;

    private final String jwtToken;

    public CustomSpringAuthenticationToken(final Object principal,
            final Object credentials,
            final SecurityUserJwtTokenBody userJwtTokenBody,
            final String jwtToken) {
        super(principal, credentials);
        this.userJwtTokenBody = userJwtTokenBody;
        this.jwtToken = jwtToken;
    }

    public SecurityUserJwtTokenBody getUserJwtTokenBody() {
        return userJwtTokenBody;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public CustomSpringAuthenticationToken(final Object principal,
            final Object credentials,
            final Collection<? extends GrantedAuthority> authorities,
            final SecurityUserJwtTokenBody userJwtTokenBody,
            final String jwtToken) {
        super(principal, credentials, authorities);
        this.userJwtTokenBody = userJwtTokenBody;
        this.jwtToken = jwtToken;
    }
}
