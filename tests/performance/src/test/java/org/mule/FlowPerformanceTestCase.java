/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule;

import static java.util.Collections.singletonList;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.Flow;
import org.mule.processor.strategy.SynchronousProcessingStrategy;
import org.mule.tck.TriggerableMessageSource;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test the performance of different approach of invoking a flow (blocking, mono, flux) along with the different processing
 * strategies.
 */
public class FlowPerformanceTestCase extends AbstractMuleContextTestCase {

  private static MessageProcessor liteProcessor = new MessageProcessor() {

    @Override
    public MuleEvent process(MuleEvent event) throws MuleException {
      return event;
    }
  };

  private Flow flow;
  private TriggerableMessageSource source;

  private static int ITERATIONS = 10000;

  @Rule
  public ContiPerfRule rule = new ContiPerfRule();

  @Before
  public void setup() throws MuleException {
    muleContext.start();

    source = new TriggerableMessageSource();
    flow = new Flow("src/main/java/jmh", muleContext);
    flow.setMessageProcessors(singletonList(liteProcessor));
    flow.setMessageSource(source);
    flow.setProcessingStrategy(new SynchronousProcessingStrategy());
    muleContext.getRegistry().registerFlowConstruct(flow);
  }

  @Test
  @PerfTest(duration = 30000, threads = 16, warmUp = 5000)
  public void blocking() throws Exception {
    for (int i = 0; i < ITERATIONS; i++) {
      source.trigger(new DefaultMuleEvent(new DefaultMuleMessage(TEST_PAYLOAD, muleContext),
                                          MessageExchangePattern.REQUEST_RESPONSE, flow));
    }
  }

}
