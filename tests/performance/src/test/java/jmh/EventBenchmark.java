/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package jmh;

import org.mule.runtime.core.DefaultEventContext;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextFactory;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.construct.Flow;
import org.mule.runtime.core.context.DefaultMuleContextFactory;

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
    public static final String TEST_CONNECTOR = "test";


    private MuleContext muleContext;
    private Flow flow;
    private Event event;
    private Event eventWith20VariablesProperties;
    private Event eventWith100VariablesProperties;

    @Setup
    public void setup() throws Exception
    {
        MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
        muleContext = muleContextFactory.createMuleContext();
        muleContext.start();
        flow = new Flow("flow", muleContext);
        muleContext.getRegistry().registerFlowConstruct(flow);
        InternalMessage.Builder messageBuilder = InternalMessage.builder().payload(TEST_PAYLOAD);
        Event.Builder eventBuilder = Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR)).message(messageBuilder.build());
        event = eventBuilder.build();
        for (int i = 0; i < 20; i++)
        {
            eventBuilder.addVariable(KEY + i, VALUE);
            messageBuilder.addOutboundProperty(KEY + 1, VALUE);
        }
        eventWith20VariablesProperties = eventBuilder.message(messageBuilder.build()).build();
        for (int i = 0; i < 80; i++)
        {
            eventBuilder.addVariable(KEY + 20 +i, VALUE);
            messageBuilder.addOutboundProperty(KEY + 20+ 1, VALUE);
        }
        eventWith100VariablesProperties = eventBuilder.message(messageBuilder.build()).build();
    }

    @Benchmark
    public String eventUUID() 
    {
        return flow.getUniqueIdString();
    }

    @Benchmark
    public Event createEvent() 
    {
        return Event.builder(DefaultEventContext.create(flow, TEST_CONNECTOR)).message(InternalMessage.of(TEST_PAYLOAD)).build();
    }

    @Benchmark
    public Event copyEvent() 
    {
        return Event.builder(event).build();
    }

    @Benchmark
    public Event copyEventWith20VariablesProperties() 
    {
        return Event.builder(eventWith20VariablesProperties).build();
    }

    @Benchmark
    public Event copyEventWith100VariablesProperties() 
    {
        return Event.builder(eventWith100VariablesProperties).build();
    }

    @Benchmark
    public Event deepCopyEvent() 
    {
        return Event.builder(event).message(InternalMessage.builder(event.getMessage()).build()).build();
    }

    @Benchmark
    public Event deepCopyEventWith20VariablesProperties() 
    {
        return Event.builder(eventWith20VariablesProperties).message(InternalMessage.builder(eventWith20VariablesProperties.getMessage()).build()).build();
    }

    @Benchmark
    public Event deepCopyEventWith100VariablesProperties() 
    {
        return Event.builder(eventWith100VariablesProperties).message(InternalMessage.builder(eventWith100VariablesProperties.getMessage()).build()).build();
    }

    @Benchmark
    public Event addEventVariable() 
    {
        return Event.builder(event).addVariable(KEY, VALUE).build();
    }

    @Benchmark
    public Event addEventVariableEventWith20VariablesProperties() 
    {
        return Event.builder(eventWith20VariablesProperties).addVariable(KEY, VALUE).build();
    }

    @Benchmark
    public Event addEventVariableEventWith100VariablesProperties() 
    {
        return Event.builder(eventWith100VariablesProperties).addVariable(KEY, VALUE).build();
    }

}
