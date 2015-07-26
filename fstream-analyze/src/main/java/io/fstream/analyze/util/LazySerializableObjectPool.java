/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.analyze.util;

import static lombok.AccessLevel.PRIVATE;

import java.io.Serializable;
import java.util.NoSuchElementException;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.apache.commons.pool2.ObjectPool;

@RequiredArgsConstructor
public abstract class LazySerializableObjectPool<T> implements ObjectPool<T>, Serializable {

  @NonNull
  @Getter(lazy = true, value = PRIVATE)
  @Accessors(fluent = true)
  private final transient ObjectPool<T> delegate = createDelegate();

  /**
   * Template method.
   */
  protected abstract ObjectPool<T> createDelegate();

  @Override
  public T borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
    return delegate().borrowObject();
  }

  @Override
  public void returnObject(T obj) throws Exception {
    delegate().returnObject(obj);
  }

  @Override
  public void invalidateObject(T obj) throws Exception {
    delegate().invalidateObject(obj);
  }

  @Override
  public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
    delegate().addObject();
  }

  @Override
  public int getNumIdle() {
    return delegate().getNumIdle();
  }

  @Override
  public int getNumActive() {
    return delegate().getNumActive();
  }

  @Override
  public void clear() throws Exception, UnsupportedOperationException {
    delegate().clear();
  }

  @Override
  public void close() {
    delegate().close();
  }

}
