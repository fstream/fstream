/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.service;

import io.fstream.core.model.state.State;
import io.fstream.core.model.state.StateListener;
import io.fstream.core.service.StateService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
public final class LocalStateService implements StateService {

  @NonNull
  @Getter
  @Setter
  private State state;

  @Override
  public void initialize() {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void addListener(StateListener listener) {
  }

}