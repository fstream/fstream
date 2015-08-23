/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.compute.esper;

import static lombok.AccessLevel.PRIVATE;
import lombok.NoArgsConstructor;

import org.joda.time.DateTime;

import com.google.common.math.DoubleMath;

/**
 * Esper singlerow-functions.
 * 
 * @See http://esper.codehaus.org/esper-5.0.0/doc/reference/en-US/html_single/index.html#custom-singlerow-function
 */
@NoArgsConstructor(access = PRIVATE)
public class SingleRowFunctions {

  public static boolean fuzzyEquals(double a, double b, double tolerance) {
    return DoubleMath.fuzzyEquals(a, b, tolerance);
  }

  public static long eventMillis(DateTime dateTime) {
    return dateTime.getMillis();
  }

}
