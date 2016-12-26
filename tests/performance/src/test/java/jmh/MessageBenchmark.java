/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package jmh;

import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextFactory;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.message.InternalMessage.Builder;
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
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@Fork(1)
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MessageBenchmark {

  public static final String TEST_PAYLOAD = "test";
  public static final String KEY = "key";
  public static final String VALUE = "value";

  private Message message;
  private Message messageWith20Properties;
  private Message messageWith100Properties;

  @Setup
  public void setup() throws Exception {
    Builder messsageBuilder = InternalMessage.builder().payload(TEST_PAYLOAD);
    message = messsageBuilder.build();
    Message.builder().payload(TEST_PAYLOAD).build();
    for (int i = 0; i < 20; i++) {
      messsageBuilder.addOutboundProperty(KEY + i, VALUE);
    }
    messageWith20Properties = messsageBuilder.build();
    for (int i = 0; i < 80; i++) {
      messsageBuilder.addOutboundProperty(KEY + 20 + i, VALUE);
    }
    messageWith100Properties = messsageBuilder.build();
  }

  @Benchmark
  public Message createMessage() {
    return Message.builder().payload(TEST_PAYLOAD).build();
  }

  @Benchmark
  public Message createMessageWithDataType() {
    return Message.builder().payload(TEST_PAYLOAD).mediaType(MediaType.TEXT).build();
  }


  @Benchmark
  public Message copyMessage() {
    return Message.builder(message).build();
  }

  @Benchmark
  public Message copyMessageWith20VariablesProperties() {
    return Message.builder(messageWith20Properties).build();
  }

  @Benchmark
  public Message copyMessageWith100VariablesProperties() {
    return Message.builder(messageWith100Properties).build();
  }

  @Benchmark
  public Message mutateMessagePayload() {
    return Message.builder(message).payload(VALUE).build();
  }

  @Benchmark
  public Message mutateMessagePayloadWithDataType() {
    return Message.builder(message).payload(VALUE).mediaType(MediaType.TEXT).build();
  }

  @Benchmark
  public Message addMessageProperty() {
    return InternalMessage.builder(message).addOutboundProperty(KEY, VALUE).build();
  }

  @Benchmark
  public Message addMessagePropertyMessageWith20Properties() {
    return InternalMessage.builder(messageWith20Properties).addOutboundProperty(KEY, VALUE).build();
  }

  @Benchmark
  public Message addMessagePropertyMessageWith100Properties() {
    return InternalMessage.builder(messageWith100Properties).addOutboundProperty(KEY, VALUE).build();
  }

  @Benchmark
  public Message addMessagePropertyWithDataType() {
    return InternalMessage.builder(message).addOutboundProperty(KEY, VALUE, DataType.STRING).build();
  }

  @Benchmark
  public Message addRemoveMessageProperty() {
    InternalMessage temp = InternalMessage.builder(message).addOutboundProperty(KEY, VALUE).build();
    return InternalMessage.builder(temp).removeOutboundProperty(KEY).build();
  }

  @Benchmark
  public Message addRemoveMessagePropertyMessageWith20Properties() {
    InternalMessage temp = InternalMessage.builder(messageWith20Properties).addOutboundProperty(KEY, VALUE).build();
    return InternalMessage.builder(temp).removeOutboundProperty(KEY).build();
  }

  @Benchmark
  public Message addRemoveMessagePropertyMessageWith100Properties() {
    InternalMessage temp = InternalMessage.builder(messageWith100Properties).addOutboundProperty(KEY, VALUE).build();
    return InternalMessage.builder(temp).removeOutboundProperty(KEY).build();
  }

  @Benchmark
  public Message addRemoveMessagePropertyWithDataType() {
    InternalMessage temp = InternalMessage.builder(message).addOutboundProperty(KEY, VALUE, DataType.STRING).build();
    return InternalMessage.builder(temp).removeOutboundProperty(KEY).build();
  }
}
