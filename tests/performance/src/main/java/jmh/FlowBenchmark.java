/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package jmh;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.config.builders.BasicRuntimeServicesConfigurationBuilder;
import org.mule.runtime.core.DefaultEventContext;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.ConfigurationBuilder;
import org.mule.runtime.core.api.context.MuleContextFactory;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import org.mule.runtime.core.config.builders.DefaultsConfigurationBuilder;
import org.mule.runtime.core.construct.Flow;
import org.mule.runtime.core.context.DefaultMuleContextFactory;
import org.mule.service.scheduler.internal.DefaultSchedulerService;
import org.mule.tck.TriggerableMessageSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Mono;

/**
 *
 */
@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 10)
public class FlowBenchmark {

  public static final String TEST_PAYLOAD = "test";
  public static final String TEST_CONNECTOR = "test";

  private MuleContext muleContext;
  private Flow flow;
  private TriggerableMessageSource source;

  @Param({"org.mule.runtime.core.processor.strategy.LegacySynchronousProcessingStrategyFactory",
      "org.mule.runtime.core.processor.strategy.SynchronousProcessingStrategyFactory",
      "org.mule.runtime.core.processor.strategy.ReactorProcessingStrategyFactory",
      "org.mule.runtime.core.processor.strategy.MultiReactor1ProcessingStrategyFactory",
      "org.mule.runtime.core.processor.strategy.MultiReactorProcessingStrategyFactory",
      "jmh.MonoProcesingStrategyFactory"})
  public String processingStrategyFactory;


  @Setup
  public void setup() throws Exception {
    MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
    List<ConfigurationBuilder> builderList = new ArrayList<>();
    builderList.add(new DefaultsConfigurationBuilder());
    builderList.add(new BasicRuntimeServicesConfigurationBuilder());
    muleContext = muleContextFactory.createMuleContext(builderList.toArray(new ConfigurationBuilder[] {}));
    muleContext.start();

    source = new TriggerableMessageSource();
    flow = new Flow("flow", muleContext);
    flow.setMessageProcessors(Collections.singletonList(event -> event));
    flow.setMessageSource(source);
    flow.setProcessingStrategyFactory((ProcessingStrategyFactory) Class.forName(processingStrategyFactory).newInstance());
    muleContext.getRegistry().registerFlowConstruct(flow);
  }

  @TearDown
  public void teardown() throws MuleException {
    ((DefaultSchedulerService) muleContext.getSchedulerService()).stop();
  }

  @Benchmark
  @Threads(1)
  public Event processSource1Thread() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(2)
  public Event processSource2Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(4)
  public Event processSource4Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(8)
  public Event processSource8Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(16)
  public Event processSource16Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(32)
  public Event processSource32Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(64)
  public Event processSource64Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }


  @Benchmark
  @Threads(128)
  public Event processSource128Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }


  @Benchmark
  @Threads(256)
  public Event processSource256Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

}
