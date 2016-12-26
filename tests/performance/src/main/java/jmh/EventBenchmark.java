/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package jmh;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextFactory;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.processor.ProcessingStrategy;
import org.mule.construct.Flow;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.tck.TriggerableMessageSource;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class EventBenchmark
{

    public static final String TEST_PAYLOAD = "test";
    public static final String KEY = "key";
    public static final String VALUE = "value";

    private MuleContext muleContext;
    private Flow flow;
    private MuleEvent event;
    private MuleEvent eventWith20VariablesProperties;
    private MuleEvent eventWith100VariablesProperties;

    @Setup
    public void setup() throws Exception
    {
        MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
        muleContext = muleContextFactory.createMuleContext();
        muleContext.start();
        flow = new Flow("src/main/java/jmh", muleContext);
        muleContext.getRegistry().registerFlowConstruct(flow);
        event = new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext), MessageExchangePattern.ONE_WAY, flow);
        eventWith20VariablesProperties = new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext),
                                                              MessageExchangePattern
                                                                      .ONE_WAY, flow);
        for (int i = 0; i < 20; i++)
        {
            eventWith20VariablesProperties.setFlowVariable(KEY + i, VALUE);
            eventWith20VariablesProperties.getMessage().setOutboundProperty(KEY + 1, VALUE);
        }
        eventWith100VariablesProperties = new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext),
                                                              MessageExchangePattern
                                                                      .ONE_WAY, flow);
        for (int i = 0; i < 100; i++)
        {
            eventWith100VariablesProperties.setFlowVariable(KEY + i, VALUE);
            eventWith100VariablesProperties.getMessage().setOutboundProperty(KEY + 1, VALUE);
        }
    }

    @Benchmark
    public String eventUUID() throws MuleException
    {
        return muleContext.getUniqueIdString();
    }

    @Benchmark
    public MuleEvent createEvent() throws MuleException
    {
        return new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext), MessageExchangePattern.ONE_WAY, flow);
    }

    @Benchmark
    public MuleEvent copyEvent() throws MuleException
    {
        return new DefaultMuleEvent(event.getMessage(), event);
    }

    @Benchmark
    public MuleEvent copyEventWith20VariablesProperties() throws MuleException
    {
        return new DefaultMuleEvent(eventWith20VariablesProperties.getMessage(), eventWith20VariablesProperties);
    }

    @Benchmark
    public MuleEvent copyEventWith100VariablesProperties() throws MuleException
    {
        return new DefaultMuleEvent(eventWith100VariablesProperties.getMessage(), eventWith100VariablesProperties);
    }

    @Benchmark
    public MuleEvent deepCopyEvent() throws MuleException
    {
        return DefaultMuleEvent.copy(event);
    }


    @Benchmark
    public MuleEvent deepCopyEventWith20VariablesProperties() throws MuleException
    {
        return DefaultMuleEvent.copy(eventWith20VariablesProperties);
    }

    @Benchmark
    public MuleEvent deepCopyEventWith100VariablesProperties() throws MuleException
    {
        return DefaultMuleEvent.copy(eventWith100VariablesProperties);
    }

    @Benchmark
    public MuleEvent addEventVariable() throws MuleException
    {
        event.setFlowVariable(KEY, VALUE);
        return event;
    }

    @Benchmark
    public MuleEvent addEventVariableEventWith20VariablesProperties() throws MuleException
    {
        eventWith20VariablesProperties.setFlowVariable(KEY, VALUE);
        return event;
    }

    @Benchmark
    public MuleEvent addEventVariableEventWith100VariablesProperties() throws MuleException
    {
        eventWith100VariablesProperties.setFlowVariable(KEY, VALUE);
        return event;
    }


    public RunResult runTest(String testName) throws RunnerException
    {
        Options opt = new OptionsBuilder()
                .include(".*" + EventBenchmark.class.getSimpleName() + "." +testName)
                .forks(1)
                .build();
        return new Runner(opt).runSingle();
    }

}
