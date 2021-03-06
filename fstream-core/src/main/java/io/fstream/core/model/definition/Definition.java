/*
 * Copyright (c) 2014 fStream. All Rights Reserved.
 *
 * Project and contact information: https://bitbucket.org/fstream/fstream
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package io.fstream.core.model.definition;

import lombok.Data;

@Data
public class Definition {

  private int id;
  private DefinitionType type;
  private int version;
  private String name;
  private String description;

}
