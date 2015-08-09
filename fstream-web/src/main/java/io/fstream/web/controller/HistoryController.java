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
import io.fstream.web.service.HistoryService;

import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.influxdb.dto.QueryResult.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/history")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HistoryController {

  /**
   * Dependencies.
   */
  @NonNull
  private final HistoryService historyService;

  @RequestMapping(method = GET)
  public @ResponseBody List<Result> getState(@RequestParam("query") String query) {
    return historyService.executeQuery(query);
  }

}
