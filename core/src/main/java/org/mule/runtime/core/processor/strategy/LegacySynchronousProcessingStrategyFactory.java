/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;

import java.time.Duration;
import java.util.function.Function;

import javax.naming.OperationNotSupportedException;

import org.reactivestreams.Publisher;
import reactor.core.Cancellation;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxProcessor;

/**
 * This processing strategy processes all message processors in the calling thread.
 */
@Deprecated
public class LegacySynchronousProcessingStrategyFactory implements ProcessingStrategyFactory {

  public static ProcessingStrategy LEGACY_SYNCHRONOUS_PROCESSING_STRATEGY_INSTANCE = new AbstractProcessingStrategy() {

    @Override
    public boolean isSynchronous() {
      return true;
    }

    @Override
    public Sink getSink(FlowConstruct flowConstruct, Function<Publisher<Event>, Publisher<Event>> function) {
      return new Sink(){
        @Override
        public void accept(Event event)
        {

        }

        @Override
        public void submit(Event event, Duration duration)
        {

        }

        @Override
        public boolean emit(Event event)
        {
          return false;
        }
      };
    }
  };

  @Override
  public ProcessingStrategy create(MuleContext muleContext, String schedulersNamePrefix) {
    return LEGACY_SYNCHRONOUS_PROCESSING_STRATEGY_INSTANCE;
  }

}
