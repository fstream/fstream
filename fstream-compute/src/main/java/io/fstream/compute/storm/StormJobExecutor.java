/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.storm;

/**
 * Abstraction responsible for executing alert and metric definitions in the form of {@link StormJob}s.
 */
public interface StormJobExecutor {

  /**
   * Execute a Storm {@code job} indefinitely.
   * 
   * @param job the job to execute
   */
  void execute(StormJob job);

}
