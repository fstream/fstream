/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import static com.google.common.base.Strings.repeat;
import io.fstream.core.model.state.State;
import io.fstream.core.model.state.StateListener;
import io.fstream.core.service.StateService;

import javax.annotation.PostConstruct;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Compute job submission entry point.
 */
@Slf4j
@Service
@Profile("esper")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EsperService implements StateListener {

  /**
   * Dependencies.
   */
  @NonNull
  private StateService stateService;
  @NonNull
  private EsperJobFactory jobFactory;
  @NonNull
  private EsperJobExecutor jobExecutor;

  @PostConstruct
  @SneakyThrows
  public void initialize() {
    log.info("Registering for state updates...");
    stateService.initialize();
    stateService.addListener(this);
  }

  @Override
  @SneakyThrows
  public void onUpdate(@NonNull State nextState) {
    log.info("{}", repeat("-", 100));
    log.info("Updating state...");
    log.info("{}", repeat("-", 100));

    val job = jobFactory.createJob(nextState);
    jobExecutor.execute(job);
  }

}
