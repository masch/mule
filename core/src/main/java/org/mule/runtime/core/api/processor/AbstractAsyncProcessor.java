/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.processor;

import static reactor.core.publisher.Flux.from;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.Event;

import org.reactivestreams.Publisher;

/**

 * @since 4.0
 */
public abstract class AbstractAsyncProcessor implements AsyncProcessor {


  @Override
  public Publisher<Event> apply(Publisher<Event> publisher) {
    return from(publisher).concatMap(event -> processAsync(event));
  }

}
