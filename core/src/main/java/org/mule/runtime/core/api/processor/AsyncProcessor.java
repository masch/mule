/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.processor;

import static org.mule.runtime.core.util.rx.Exceptions.checkedFunction;
import static org.mule.runtime.core.util.rx.internal.Operators.nullSafeMap;
import static reactor.core.publisher.Flux.from;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.util.rx.Exceptions;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * Processes {@link Event}'s. Implementations that do not mutate the {@link Event} or pass it on to another MessageProcessor
 * should return the MuleEvent they receive.
 *
 * From 4.0 this interface also extends {@link ReactiveProcessor} and implementations of this interface can be used in
 * {@link Event} stream processing via the default implementation of {@link #apply(Publisher)} that performs a map function on the
 * stream using the result of the invocation of the blocking {@link #process(Event)} method. Using this approach simple processor
 * implementations that don't block or perform blocking IO can continue to implement {@link AsyncProcessor} and require no
 * changes.
 * 
 * @since 4.0
 */
public interface AsyncProcessor extends ReactiveProcessor {

  /**
   * Invokes the MessageProcessor.
   * 
   * @param event MuleEvent to be processed
   * @return optional response MuleEvent
   * @throws MuleException
   */
  Publisher<Event> processAsync(Event event);

}
