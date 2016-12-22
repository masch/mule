/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.source;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.EventContext;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.processor.Sink;

import java.util.function.Supplier;

/**
 * Message source interface that instead of providing a {@link Processor} listener to sources, provides a {@link Sink} that allows
 * {@link Event}'s to be dispatched asynchronously. Sources can then recieve completion signals by subscribing to the
 * {@link EventContext}
 *
 * // TODO MULE-11250 Migrate MessageSource to PushSource approach in transports and tests
 *
 * @since 4.0
 */
public interface PushSource extends MessageSource {

  /**
   * Sets the {@link Sink} to be used by the source to dispatch {@link Event}'s for asynchronous processing.
   * @param sink the sink instance.
   */
  void setSink(Sink sink);

}
