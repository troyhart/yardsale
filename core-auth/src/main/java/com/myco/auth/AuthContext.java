package com.myco.auth;

import com.myco.api.values.UserInfo;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthContext {
  public static final UserInfo authenticatedUserInfo() {
    AuthToken auth = (AuthToken) SecurityContextHolder.getContext().getAuthentication();
    return auth == null ? null : auth.getPrincipal();
  }
}
