/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.core.transaction.TransactionCoordination.isTransactionActive;
import static reactor.core.Exceptions.propagate;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.processor.Sink;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.exception.MessagingException;

import java.time.Duration;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import reactor.core.Cancellation;
import reactor.core.publisher.BlockingSink;
import reactor.core.publisher.BlockingSink.Emission;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.FluxProcessor;

/**
 * Abstract base {@link ProcessingStrategy} that creates a {@link Sink} that serializes events.
 */
public abstract class AbstractProcessingStrategy implements ProcessingStrategy {

  public static final String TRANSACTIONAL_ERROR_MESSAGE = "Unable to process a transactional flow asynchronously";

  public Sink getSink(FlowConstruct flowConstruct, Function<Publisher<Event>, Publisher<Event>> function) {
    FluxProcessor<Event, Event> processor = EmitterProcessor.<Event>create().serialize();
    Cancellation cancellation = processor.transform(function).retry().subscribe();
    BlockingSink blockingSink = processor.connectSink();

    return new ReactorSink(blockingSink, flowConstruct, cancellation, assertCanProcess());
  }

  class ReactorSink implements Sink, Disposable {

    private final BlockingSink blockingSink;
    private final FlowConstruct flowConstruct;
    private final Cancellation cancellation;
    private final Consumer<Event> onEvent;

    ReactorSink(BlockingSink blockingSink, FlowConstruct flowConstruct, Cancellation cancellation, Consumer<Event> onEvent) {
      this.blockingSink = blockingSink;
      this.flowConstruct = flowConstruct;
      this.cancellation = cancellation;
      this.onEvent = onEvent;
    }

    @Override
    public void accept(Event event) {
      onEvent.accept(event);
      blockingSink.accept(event);
    }

    @Override
    public void submit(Event event, Duration duration) {
      onEvent.accept(event);
      if (blockingSink.submit(event) < 0) {
        MessagingException rejectedException =
            new MessagingException(event, new RejectedExecutionException("Flow rejected execution of event after "
                + duration.toMillis() + "ms"));
        flowConstruct.getExceptionListener().handleException(rejectedException, event);
        event.getContext().error(rejectedException);
      }
    }

    @Override
    public boolean emit(Event event) {
      onEvent.accept(event);
      return blockingSink.emit(event) == Emission.OK;
    }

    @Override
    public void dispose() {
      blockingSink.complete();
      cancellation.dispose();
    }
  }

  protected Consumer<Event> assertCanProcess() {
    return event -> {
      if (isTransactionActive()) {
        throw new MuleRuntimeException(createStaticMessage(TRANSACTIONAL_ERROR_MESSAGE));
      }
    };
  }
}
