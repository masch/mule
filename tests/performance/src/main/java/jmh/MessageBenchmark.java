/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package jmh;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.context.MuleContextFactory;
import org.mule.api.transformer.DataType;
import org.mule.api.transport.PropertyScope;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.util.UUID;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MessageBenchmark
{

    public static final String TEST_PAYLOAD = "test";
    public static final String KEY = "key";
    public static final String VALUE = "value";

    private MuleContext muleContext;
    private MuleMessage message;
    private MuleMessage messageWith20Properties;
    private MuleMessage messageWith100Properties;

    @Setup
    public void setup() throws Exception
    {
        MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
        muleContext = muleContextFactory.createMuleContext();
        message = new DefaultMuleMessage(TEST_PAYLOAD, muleContext);
        messageWith20Properties = new DefaultMuleMessage(TEST_PAYLOAD, muleContext);
        for (int i = 0; i < 20; i++)
        {
            messageWith20Properties.setOutboundProperty(KEY + 1, VALUE);
        }
        messageWith100Properties = new DefaultMuleMessage(TEST_PAYLOAD, muleContext);
        for (int i = 0; i < 100; i++)
        {
            messageWith20Properties.setOutboundProperty(KEY + 1, VALUE);
        }
    }

    @Benchmark
    public String messageUUID() throws MuleException
    {
        return UUID.getUUID();
    }


    @Benchmark
    public MuleMessage createMessage() throws MuleException
    {
        return new DefaultMuleMessage(TEST_PAYLOAD, muleContext);
    }

    @Benchmark
    public MuleMessage createMessageWithDataType() throws MuleException
    {
        return new DefaultMuleMessage(TEST_PAYLOAD, null, null, null, muleContext, DataType.STRING_DATA_TYPE);
    }


    @Benchmark
    public MuleMessage copyMessage() throws MuleException
    {
        return new DefaultMuleMessage(message);
    }

    @Benchmark
    public MuleMessage copyMessageWith20VariablesProperties() throws MuleException
    {
        return new DefaultMuleMessage(messageWith20Properties);
    }

    @Benchmark
    public MuleMessage copyMessageWith100VariablesProperties() throws MuleException
    {
        return new DefaultMuleMessage(messageWith100Properties);
    }

    @Benchmark
    public MuleMessage mutateMessagePayload() throws MuleException
    {
        message.setPayload(VALUE);
        return message;
    }

    @Benchmark
    public MuleMessage mutateMessagePayloadWithDataType() throws MuleException
    {
        message.setPayload(VALUE, DataType.STRING_DATA_TYPE);
        return message;
    }

    @Benchmark
    public MuleMessage addMessageProperty() throws MuleException
    {
        message.setOutboundProperty(KEY, VALUE);
        return message;
    }

    @Benchmark
    public MuleMessage addMessagePropertyMessageWith20Properties() throws MuleException
    {
        messageWith20Properties.setOutboundProperty(KEY, VALUE);
        return message;
    }

    @Benchmark
    public MuleMessage addMessagePropertyMessageWith100Properties() throws MuleException
    {
        messageWith100Properties.setOutboundProperty(KEY, VALUE);
        return message;
    }

    @Benchmark
    public MuleMessage addMessagePropertyWithDataType() throws MuleException
    {
        message.setOutboundProperty(KEY, VALUE, DataType.STRING_DATA_TYPE);
        return message;
    }

    @Benchmark
    public MuleMessage addRemoveMessageProperty() throws MuleException
    {
        message.setOutboundProperty(KEY, VALUE);
        message.removeProperty(KEY, PropertyScope.OUTBOUND);
        return message;
    }

    @Benchmark
    public MuleMessage addRemoveMessagePropertyMessageWith20Properties() throws MuleException
    {
        messageWith20Properties.setOutboundProperty(KEY, VALUE);
        messageWith20Properties.removeProperty(KEY, PropertyScope.OUTBOUND);
        return message;
    }

    @Benchmark
    public MuleMessage addRemoveMessagePropertyMessageWith100Properties() throws MuleException
    {
        messageWith100Properties.setOutboundProperty(KEY, VALUE);
        messageWith100Properties.removeProperty(KEY, PropertyScope.OUTBOUND);
        return message;
    }

    @Benchmark
    public MuleMessage addRemoveMessagePropertyWithDataType() throws MuleException
    {
        message.setOutboundProperty(KEY, VALUE, DataType.STRING_DATA_TYPE);
        message.removeProperty(KEY, PropertyScope.OUTBOUND);
        return message;
    }
}
