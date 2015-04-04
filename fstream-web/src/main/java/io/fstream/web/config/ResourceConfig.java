/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.config;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Slf4j
@Configuration
public class ResourceConfig extends WebMvcConfigurerAdapter {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    val staticDir = resolveStaticDir();
    log.info("Static web resources from: {}", staticDir);

    registry.addResourceHandler("/**").addResourceLocations(staticDir);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("forward:/index.html");
  }

  private String resolveStaticDir() {
    return isJar() ? "classpath:/static/" : "file:../fstream-ui/dist/";
  }

  private boolean isJar() {
    return getClass().getProtectionDomain().getCodeSource().getLocation().getProtocol().equals("jar");
  }

}