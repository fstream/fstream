/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Bean
  @Profile("secure")
  public WebSecurityConfigurerAdapter secure() {
    return new WebSecurityConfigurerAdapter() {

      @Override
      protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .httpBasic()
            .and()
            .logout().logoutSuccessUrl("/")
            .and()
            .authorizeRequests()
            .antMatchers(
                "/", "/index.html",
                "/views/**", "/scripts/**", "/styles/**", "/fonts/**").permitAll()
            .anyRequest().authenticated();
      }

    };
  }

  @Bean
  @Profile("!secure")
  public WebSecurityConfigurerAdapter notSecure() {
    return new WebSecurityConfigurerAdapter() {

      @Override
      protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll();
      }

    };
  }

}