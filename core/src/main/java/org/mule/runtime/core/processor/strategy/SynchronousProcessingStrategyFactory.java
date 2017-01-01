/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static reactor.util.concurrent.QueueSupplier.*;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;

import java.util.function.Consumer;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import reactor.core.Cancellation;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.TopicProcessor;
import reactor.util.concurrent.QueueSupplier;

/**
 * This processing strategy processes all message processors in the calling thread.
 */
public class SynchronousProcessingStrategyFactory implements ProcessingStrategyFactory {

  public static ProcessingStrategy SYNCHRONOUS_PROCESSING_STRATEGY_INSTANCE = new AbstractProcessingStrategy() {

    @Override
    public boolean isSynchronous() {
      return true;
    }

    @Override
    public Sink getSink(FlowConstruct flowConstruct, Function<Publisher<Event>, Publisher<Event>> function) {
      FluxProcessor<Event, Event> processor = EmitterProcessor.<Event>create(1, false).serialize();
      Cancellation cancellation = processor.transform(function).retry().subscribe();
      return new ReactorSink(processor.connectSink(), flowConstruct, cancellation, assertCanProcess());
    }

    @Override
    protected Consumer<Event> assertCanProcess() {
      return event -> {
      };
    }
  };

  @Override
  public ProcessingStrategy create(MuleContext muleContext, String schedulersNamePrefix) {
    return SYNCHRONOUS_PROCESSING_STRATEGY_INSTANCE;
  }

}
