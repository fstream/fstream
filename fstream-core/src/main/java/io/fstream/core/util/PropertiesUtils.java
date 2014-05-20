/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.util;

import static lombok.AccessLevel.PRIVATE;

import java.util.Map;
import java.util.Properties;

import lombok.NoArgsConstructor;
import lombok.val;

@NoArgsConstructor(access = PRIVATE)
public final class PropertiesUtils {

  public static Properties getProperties(Map<String, String> map) {
    val value = new Properties();
    value.putAll(map);

    return value;
  }

}
