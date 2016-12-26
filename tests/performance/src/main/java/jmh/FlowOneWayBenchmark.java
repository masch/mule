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
import org.mule.api.context.MuleContextFactory;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.processor.ProcessingStrategy;
import org.mule.construct.Flow;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.processor.NullMessageProcessor;
import org.mule.processor.strategy.AsynchronousProcessingStrategy;
import org.mule.processor.strategy.SynchronousProcessingStrategy;
import org.mule.tck.TriggerableMessageSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

/**
 *
 */
@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class FlowOneWayBenchmark
{

    public static final String TEST_PAYLOAD = "test";

    private MuleContext muleContext;
    private Flow flow;
    private TriggerableMessageSource source;

    @Param({"org.mule.processor.strategy.AsynchronousProcessingStrategy",
            "org.mule.processor.strategy.QueuedAsynchronousProcessingStrategy"})
    public String processingStrategyFactory;

    @Setup
    public void setup() throws Exception
    {
        MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
        muleContext = muleContextFactory.createMuleContext();
        muleContext.start();

        source = new TriggerableMessageSource();
        flow = new Flow("flow", muleContext);
        List<MessageProcessor> processors = new ArrayList<>();
        flow.setMessageProcessors(Collections.<MessageProcessor>singletonList(new MessageProcessor()
        {

            @Override
            public MuleEvent process(MuleEvent event)
            {
                return event;
            }
        }));
        flow.setMessageSource(source);
        flow.setProcessingStrategy((ProcessingStrategy) Class.forName(processingStrategyFactory).newInstance());
        muleContext.getRegistry().registerFlowConstruct(flow);
    }


    @Benchmark
    @Threads(1)
    public MuleEvent processSourceOneWay1Thread() throws MuleException
    {
        return source.trigger(new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext),
                                                   MessageExchangePattern.ONE_WAY, flow));
    }

    @Benchmark
    @Threads(4)
    public MuleEvent processSourceOneWay4Threads() throws MuleException
    {
        return source.trigger(new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext),
                                                   MessageExchangePattern.ONE_WAY, flow));
    }

    @Benchmark
    @Threads(16)
    public MuleEvent processSourceOneWay16Threads() throws MuleException
    {
        return source.trigger(new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext),
                                                   MessageExchangePattern.ONE_WAY, flow));
    }

    @Benchmark
    @Threads(64)
    public MuleEvent processSourceOneWay64Threads() throws MuleException
    {
        return source.trigger(new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext),
                                                   MessageExchangePattern.ONE_WAY, flow));
    }

}
