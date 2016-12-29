/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package jmh;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import org.mule.runtime.core.processor.strategy.AbstractProcessingStrategy;

import java.time.Duration;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class MonoProcesingStrategyFactory implements ProcessingStrategyFactory {


  @Override
  public ProcessingStrategy create(MuleContext muleContext, String schedulersNamePrefix) {
    return new MonoProcesingStrategy();
  }

  static class MonoProcesingStrategy extends AbstractProcessingStrategy {

    @Override
    public Sink getSink(FlowConstruct flowConstruct, Function<Publisher<Event>, Publisher<Event>> function) {
      return new Sink() {

        @Override
        public void accept(Event event) {
          Mono.just(event).transform(function).block();
        }

        @Override
        public void submit(Event event, Duration duration) {
          Mono.just(event).transform(function).subscribe();
        }

        @Override
        public boolean emit(Event event) {
          Mono.just(event).transform(function).subscribe();
          return true;
        }
      };
    }
  }

}
