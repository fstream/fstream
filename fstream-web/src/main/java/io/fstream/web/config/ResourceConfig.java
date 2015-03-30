/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Slf4j
@Configuration
public class ResourceConfig extends WebMvcConfigurerAdapter {

  /**
   * At development time, we want the static resources served directly from the <code>ontrack-web</code> project, under
   * the <code>target/dev</code> directory.
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String staticDir = "src/main/resources/static/dist";
    log.info("Static web resources from: " + staticDir);

    String prefix = "file:";
    String dir = prefix + staticDir;
    if (!dir.endsWith("/")) {
      dir += "/";
    }
    registry.addResourceHandler("/fonts/**").addResourceLocations(dir + "fonts/");
    registry.addResourceHandler("/images/**").addResourceLocations(dir + "images/");
    registry.addResourceHandler("/scripts/**").addResourceLocations(dir + "scripts/");
    registry.addResourceHandler("/styles/**").addResourceLocations(dir + "styles/");
    registry.addResourceHandler("/views/**").addResourceLocations(dir + "views/");
    registry.addResourceHandler("index.html").addResourceLocations(dir);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("forward:/index.html");
  }

}