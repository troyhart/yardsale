package com.myco.auth;

import com.myco.api.values.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

public final class AuthToken extends JwtAuthenticationToken {

  private static final long serialVersionUID = 1L;
  private UserInfo principal;
  private Object credentials;

  public AuthToken(
      Jwt jwt, Collection<? extends GrantedAuthority> authorities, UserInfo principal, Object credentials
  ) {
    super(jwt, authorities);
    this.principal = principal;
    this.credentials = credentials;
  }

  @Override
  public UserInfo getPrincipal() {
    return principal;
  }

  @Override
  public Object getCredentials() {
    return credentials;
  }
}
