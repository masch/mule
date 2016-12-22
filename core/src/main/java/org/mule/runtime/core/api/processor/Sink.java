/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.processor;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.EventContext;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * Used to dispatch {@link Event}'s asynchronsly for processing. The result of asynchronous processing can be obtained by
 * subscribing to the {@link Event}'s {@link EventContext}.
 *
 * @since 4.0
 */
public interface Sink extends Consumer<Event> {

  /**
   * Submit the given {@link Event} for processing with a timeout. If the {@link Event} cannot be processed, due to for example
   * back-pressue, when the timeout is reached then the {@link EventContext} will be completed with an error of type 'OVERLOAD'.
   *
   * @param event the {@link Event} to dispatch for processing
   * @param duration timeout after which the {@link EventContext} will be completed with an error.
   * @return the emit status. 'true' if the {@link Event} emission was successful, 'false' otherwise.
   */
  void submit(Event event, Duration duration);

  /**
   * Attempt to emit the given {@link Event} for processing. If the {@link Event} cannot be emitted due to, for example
   * back-pressue, then 'false' will be returned and the {@link EventContext} will remain uncomplete, such that re-emission can be
   * attempted as required.
   *
   * // TODO MULE-11251 Introduce Enum in core/ext-api for Sink emission status
   * 
   * @param event the {@link Event} to dispatch for processing
   * @return the emit status. 'true' if the {@link Event} emission was successful, 'false' otherwise.
   */
  boolean emit(Event event);

  /**
   * Complete the sink. This will perform completion/disposal of the infrastructure used to process {@link Event}'s and as such
   * completion if terminal, and no {@link Event}'s can be emitted after completion.
   */
  void complete();

}
