/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.core.api.scheduler.SchedulerConfig.config;

import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import reactor.core.Cancellation;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.TopicProcessor;
import reactor.core.publisher.WorkQueueProcessor;

/**
 * Creates {@link ReactorProcessingStrategy} instances. This processing strategy demultiplexes incoming messages using the
 * cpu-light scheduler.
 *
 * This processing strategy is not suitable for transactional flows and will fail if used with an active transaction.
 *
 * @since 4.0
 */
public class MultiReactorProcessingStrategyFactory extends ReactorProcessingStrategyFactory {

  @Override
  public ProcessingStrategy create(MuleContext muleContext, String schedulersNamePrefix) {
    return new MultiReactorProcessingStrategy(() -> muleContext.getSchedulerService()
        .cpuLightScheduler(config().withName(schedulersNamePrefix + ".event-loop")),
                                              scheduler -> scheduler.stop(muleContext.getConfiguration().getShutdownTimeout(),
                                                                          MILLISECONDS),
                                              muleContext);
  }

  static class MultiReactorProcessingStrategy extends ReactorProcessingStrategy {

    public MultiReactorProcessingStrategy(Supplier<Scheduler> cpuLightSchedulerSupplier,
                                          Consumer<Scheduler> schedulerStopper,
                                          MuleContext muleContext) {
      super(cpuLightSchedulerSupplier, schedulerStopper, muleContext);
    }

    public Sink getSink(FlowConstruct flowConstruct, Function<Publisher<Event>, Publisher<Event>> function) {
      WorkQueueProcessor<Event> processor = WorkQueueProcessor.share(cpuLightScheduler, false);
      Cancellation cancellation1 = processor.transform(function).retry().subscribe();
      Cancellation cancellation2 = processor.transform(function).retry().subscribe();
      Cancellation cancellation3 = processor.transform(function).retry().subscribe();
      BlockingSink blockingSink = processor.connectSink();
      return new ReactorSink(blockingSink, flowConstruct, new Cancellation() {

        @Override
        public void dispose() {
          cancellation1.dispose();
          cancellation2.dispose();
          cancellation3.dispose();
        }
      });
    }
  }

}
