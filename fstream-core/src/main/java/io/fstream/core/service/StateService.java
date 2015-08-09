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

public interface StateService {

  public abstract void initialize();

  public abstract void destroy();

  public abstract State getState();

  public abstract void setState(State state);

  public abstract void addListener(StateListener listener);

}