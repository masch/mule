/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.db.api.param;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED_TAB;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;

import java.util.concurrent.TimeUnit;

/**
 * Parameters to configure queries
 *
 * @since 4.0
 */
public class QuerySettings {

  /**
   * Indicates the minimum amount of time before the JDBC driver attempts to cancel a running statement. No timeout is used by
   * default.
   */
  @Parameter
  @Optional(defaultValue = "0")
  @Placement(tab = ADVANCED_TAB)
  private int queryTimeout = 0;

  /**
   * A {@link TimeUnit} which qualifies the {@link #queryTimeout}
   */
  @Parameter
  @Optional(defaultValue = "SECONDS")
  @Placement(tab = ADVANCED_TAB)
  private TimeUnit queryTimeoutUnit = SECONDS;

  public void copyInto(QuerySettings other) {
    other.queryTimeout = queryTimeout;
    other.queryTimeoutUnit = queryTimeoutUnit;
  }

  public int getQueryTimeout() {
    return queryTimeout;
  }

  public TimeUnit getQueryTimeoutUnit() {
    return queryTimeoutUnit;
  }

}
