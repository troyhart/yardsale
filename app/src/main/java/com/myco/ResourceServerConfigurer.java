package com.myco;

import com.myco.api.values.UserInfo;
import com.myco.auth.AuthToken;
import com.myco.util.values.ObfuscatedToStringProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class ResourceServerConfigurer extends WebSecurityConfigurerAdapter {

  @Bean
  public ObfuscatedToStringProperty<String> secureDataSecret(
      @Value("${yardsale.secure-data-secret}") String secureDataSecret
  ) {
    return new ObfuscatedToStringProperty<>(secureDataSecret);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http.authorizeRequests()
      .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
      .antMatchers("/actuator/**").permitAll()//TODO: remove permitAll and re-instate hasRole ->  .hasRole("SYSTEM_ADMIN")
      .antMatchers(
          "/webjars/**",
          "/resources/**",
          "/v2/api-docs/**",
          "/swagger-resources/**",
          "/swagger-ui.html").permitAll()
      .anyRequest().authenticated()
      .and().oauth2ResourceServer()
        .jwt().jwtAuthenticationConverter(jwtConverter());
    // @formatter:on
  }

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtConverter() {
    return sourceJwt -> {

      Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
      Map<String, ?> realmAccess = sourceJwt.getClaimAsMap("realm_access");

      // Realm Roles
      if (!CollectionUtils.isEmpty(realmAccess)) {
        @SuppressWarnings("unchecked") List<String> roles = (List<String>) realmAccess.get("roles");
        if (!CollectionUtils.isEmpty(roles)) {
          roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).forEach(authorities::add);
        }
      }

      // Scopes
      String scopes = sourceJwt.getClaimAsString("scope");
      if (StringUtils.hasText(scopes)) {
        Arrays.stream(scopes.split(" ")).filter(StringUtils::hasText)
            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope)).forEach(authorities::add);
      }

      // @formatter:off
      UserInfo.Builder principalBuilder = new UserInfo.Builder()
          .userId(sourceJwt.getSubject())
          .userName(sourceJwt.getClaimAsString("preferred_username"))
          .authorities(authorities.stream().map(SimpleGrantedAuthority::getAuthority).collect(Collectors.toList()));
      // @formatter:on

      String name = sourceJwt.getClaimAsString("name");
      if (!StringUtils.hasText(name)) {
        name = sourceJwt.getClaimAsString("clientId");
      }
      principalBuilder.name(name);

      // Only include the email in the userinfo when it has been verified
      Boolean emailVerified = sourceJwt.getClaimAsBoolean("email_verified");
      if (emailVerified != null && emailVerified) {
        principalBuilder.email(sourceJwt.getClaimAsString("email"));
      }

      return new AuthToken(sourceJwt, authorities, principalBuilder.build(), null);
    };
  }
}
