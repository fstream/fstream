/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.web.service;

import org.springframework.stereotype.Service;

@Service
public class AlertMessageService extends AbstractMessageService {

  @Override
  protected String getMessageDestination() {
    return "/topic/alerts";
  }

  @Override
  protected String getTopicName() {
    return "alerts";
  }

}
