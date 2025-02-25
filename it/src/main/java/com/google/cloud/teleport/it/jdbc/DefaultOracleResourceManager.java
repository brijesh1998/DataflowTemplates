/*
 * Copyright (C) 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.teleport.it.jdbc;

import com.google.common.annotations.VisibleForTesting;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Default class for the Oracle implementation of {@link AbstractJDBCResourceManager} abstract
 * class.
 *
 * <p>The class supports one database, and multiple tables per database object. A database is *
 * created when the container first spins up, if one is not given.
 *
 * <p>The class is thread-safe.
 */
public class DefaultOracleResourceManager extends AbstractJDBCResourceManager<OracleContainer> {

  private static final String DEFAULT_ORACLE_CONTAINER_NAME = "gvenzl/oracle-xe";

  // A list of available oracle-xe Docker image tags can be found at
  // https://hub.docker.com/r/gvenzl/oracle-xe/tags?tab=tags
  private static final String DEFAULT_ORACLE_CONTAINER_TAG = "21-slim-faststart";
  private static final int DEFAULT_ORACLE_INTERNAL_PORT = 1521;

  // TODO - oracle-xe seems to require these credentials to spin up the container.
  //  Adding a more secure user after startup, or finding an image without these
  //  restrictions may be required.
  private static final String DEFAULT_ORACLE_USERNAME = "testUser";
  private static final String DEFAULT_ORACLE_PASSWORD = "testPassword";

  private DefaultOracleResourceManager(DefaultOracleResourceManager.Builder builder) {
    this(
        new OracleContainer(
            DockerImageName.parse(builder.containerImageName).withTag(builder.containerImageTag)),
        builder);
  }

  @VisibleForTesting
  DefaultOracleResourceManager(
      OracleContainer container, DefaultOracleResourceManager.Builder builder) {
    super(container, builder);
  }

  public static DefaultOracleResourceManager.Builder builder(String testId) {
    return new DefaultOracleResourceManager.Builder(testId);
  }

  @Override
  protected int getJDBCPort() {
    return DEFAULT_ORACLE_INTERNAL_PORT;
  }

  @Override
  public String getJDBCPrefix() {
    return "oracle";
  }

  @Override
  public synchronized String getUri() {
    return String.format(
        "jdbc:%s:thin:@%s:%d/%s",
        getJDBCPrefix(), this.getHost(), this.getPort(getJDBCPort()), this.getDatabaseName());
  }

  @Override
  protected String getFirstRow(String tableName) {
    return "SELECT * FROM " + tableName + " WHERE ROWNUM <= 1";
  }

  /** Builder for {@link DefaultOracleResourceManager}. */
  public static final class Builder extends AbstractJDBCResourceManager.Builder<OracleContainer> {

    public Builder(String testId) {
      super(testId);
      this.containerImageName = DEFAULT_ORACLE_CONTAINER_NAME;
      this.containerImageTag = DEFAULT_ORACLE_CONTAINER_TAG;
      this.username = DEFAULT_ORACLE_USERNAME;
      this.password = DEFAULT_ORACLE_PASSWORD;
    }

    @Override
    public DefaultOracleResourceManager build() {
      return new DefaultOracleResourceManager(this);
    }
  }
}
