package com.myco.user.query;

import com.myco.jpa.JacksonAttributeConverter;

import javax.persistence.Converter;

@Converter class UserProfileConverter extends JacksonAttributeConverter<UserProfileImpl> {
  public UserProfileConverter() {
    super(UserProfileImpl.class);
  }
}
