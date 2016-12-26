/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package jmh;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.DefaultEventContext;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextFactory;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.construct.Flow;
import org.mule.runtime.core.context.DefaultMuleContextFactory;
import org.mule.tck.TriggerableMessageSource;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;

/**
 *
 */
@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class FlowBenchmark {

  public static final String TEST_PAYLOAD = "test";
  public static final String TEST_CONNECTOR = "test";

  private MuleContext muleContext;
  private Flow flow;
  private TriggerableMessageSource source;

  @Setup
  public void setup() throws Exception {
    MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
    muleContext = muleContextFactory.createMuleContext();
    muleContext.start();

    source = new TriggerableMessageSource();
    flow = new Flow("flow", muleContext);
    flow.setMessageProcessors(Collections.singletonList(event -> event));
    flow.setMessageSource(source);
    muleContext.getRegistry().registerFlowConstruct(flow);
  }

  @Benchmark
  @Threads(1)
  public Event processSourceOneWay1Thread() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(4)
  public Event processSourceOneWay4Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(16)
  public Event processSourceOneWay16Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

  @Benchmark
  @Threads(64)
  public Event processSourceOneWay64Threads() throws MuleException {
    return source.trigger(Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR))
        .message(InternalMessage.of(TEST_PAYLOAD)).build());
  }

}
