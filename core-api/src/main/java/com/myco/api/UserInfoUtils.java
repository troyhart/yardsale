package com.myco.api;

import com.myco.api.values.UserInfo;

public class UserInfoUtils {

  public static final boolean isUserInRole(UserInfo userInfo, String role) {
    return userInfo.getAuthorities().stream().filter(auth -> ("ROLE_" + role).equals(auth)).findAny().isPresent();
  }

  public static final boolean isUserInAnyRole(UserInfo userInfo, String...roles) {
    if (roles==null||roles.length==0) return true;
    for (String role: roles) {
      if (isUserInRole(userInfo, role)) return true;
    }
    return false;
  }
}
