/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.api.scheduler.SchedulerConfig.config;
import static org.mule.runtime.core.transaction.TransactionCoordination.isTransactionActive;
import static reactor.core.Exceptions.propagate;
import static reactor.core.publisher.Flux.from;
import static reactor.core.scheduler.Schedulers.fromExecutorService;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.exception.MessagingExceptionHandler;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import reactor.core.Cancellation;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.WorkQueueProcessor;

/**
 * Creates {@link WorkQueueProcessingStrategy} instances. This processing strategy dispatches incoming messages to a work queue
 * which is served by a pool of worker threads from the applications IO {@link Scheduler}. Processing of the flow is carried out
 * synchronously on the worker thread until completion.
 *
 * This processing strategy is not suitable for transactional flows and will fail if used with an active transaction.
 *
 * @since 4.0
 */
public class WorkQueueProcessingStrategyFactory implements ProcessingStrategyFactory {

  private int maxThreads;

  @Override
  public ProcessingStrategy create(MuleContext muleContext, String schedulersNamePrefix) {
    return new WorkQueueProcessingStrategy(() -> muleContext.getSchedulerService()
        .ioScheduler(config().withName(schedulersNamePrefix)),
                                           scheduler -> scheduler.stop(muleContext.getConfiguration().getShutdownTimeout(),
                                                                       MILLISECONDS),
                                           maxThreads,
                                           muleContext);
  }

  static class WorkQueueProcessingStrategy extends AbstractSchedulingProcessingStrategy {

    private Supplier<Scheduler> schedulerSupplier;
    private Scheduler scheduler;
    private int maxThreads;

    public WorkQueueProcessingStrategy(Supplier<Scheduler> schedulerSupplier, Consumer<Scheduler> schedulerStopper,
                                       int maxThreads, MuleContext muleContext) {
      super(schedulerStopper, muleContext);
      this.schedulerSupplier = schedulerSupplier;
      this.maxThreads = maxThreads;
    }

    public Sink getSink(FlowConstruct flowConstruct, Function<Publisher<Event>, Publisher<Event>> function) {
      WorkQueueProcessor<Event> processor = WorkQueueProcessor.share(scheduler, false);
      List<Cancellation> cancellationList = new ArrayList<>();
      for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
        cancellationList.add(processor.transform(function).retry().subscribe());
      }
      BlockingSink blockingSink = processor.connectSink();
      return new ReactorSink(blockingSink, flowConstruct,
                             () -> cancellationList.stream().forEach(cancellation -> cancellation.dispose()), assertCanProcess());
    }

    @Override
    public void start() throws MuleException {
      this.scheduler = schedulerSupplier.get();
    }

    @Override
    public void stop() throws MuleException {
      if (scheduler != null) {
        getSchedulerStopper().accept(scheduler);
      }
    }

    private Consumer<Event> assertCanProcessAsync() {
      return event -> {
        if (isTransactionActive()) {
          throw propagate(new DefaultMuleException(createStaticMessage(TRANSACTIONAL_ERROR_MESSAGE)));
        }
      };
    }

  }
}
