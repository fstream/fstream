/*
 * Copyright (c) 2015 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package commands

import com.google.common.io.Resources;

/**
 * Customize shell banner.
 */
welcome = { ->
  if (!crash.context.attributes['spring.environment'].getProperty("spring.main.show_banner", Boolean.class, Boolean.TRUE)) {
    return ""
  }

  return Resources.getResource("banner.txt").text + "\nWelcome to the simulate shell! Type 'help' to get started.\n\n"
}

/**
 * Customize shell prompt.
 */
prompt = { ->
  return "fstream-simulate> ";
}
