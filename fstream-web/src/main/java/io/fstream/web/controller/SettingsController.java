/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import io.fstream.web.config.WebProperties;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SettingsController {

  /**
   * Dependencies.
   */
  @NonNull
  private final WebProperties properties;

  @RequestMapping(method = GET)
  public @ResponseBody WebProperties getSettings() {
    return properties;
  }

}
