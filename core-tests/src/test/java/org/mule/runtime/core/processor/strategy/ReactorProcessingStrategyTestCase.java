/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.internal.matchers.ThrowableCauseMatcher.*;
import static org.mule.runtime.core.processor.strategy.AbstractSchedulingProcessingStrategy.TRANSACTIONAL_ERROR_MESSAGE;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.exception.MessagingException;
import org.mule.runtime.core.processor.strategy.ReactorProcessingStrategyFactory.ReactorProcessingStrategy;
import org.mule.runtime.core.transaction.TransactionCoordination;
import org.mule.tck.testmodels.mule.TestTransaction;

import java.util.concurrent.TimeoutException;

import org.junit.Test;
import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

@Features("Processing Strategies")
@Stories("MultiReactor Processing Strategy")
public class ReactorProcessingStrategyTestCase extends AbstractProcessingStrategyTestCase {

  public ReactorProcessingStrategyTestCase(Mode mode) {
    super(mode);
  }

  @Override
  protected ProcessingStrategy createProcessingStrategy(MuleContext muleContext, String schedulersNamePrefix) {
    return new ReactorProcessingStrategy(() -> cpuLight, scheduler -> {
    }, muleContext);
  }

  @Override
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a single cpu light thread.")
  public void singleCpuLight() throws Exception {
    super.singleCpuLight();
    assertThreads(0, 1, 0, 0);
  }

  @Override
  public void singleCpuLightConcurrent() throws Exception {
    expectedException.expect(MessagingException.class);
    expectedException.expect(hasCause(hasCause(instanceOf(TimeoutException.class))));
    super.singleCpuLightConcurrent();
  }

  @Override
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a single cpu light thread.")
  public void multipleCpuLight() throws Exception {
    super.multipleCpuLight();
    assertThreads(0, 1, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a single cpu light thread.")
  public void singleBlocking() throws Exception {
    super.singleBlocking();
    assertThreads(0, 1, 0, 0);
  }

  @Test
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
               + "synchronously in a single cpu light thread.")
  public void singleBlockingConcurrent() throws Exception {
    expectedException.expect(MessagingException.class);
    expectedException.expect(hasCause(hasCause(instanceOf(TimeoutException.class))));
    super.singleBlockingConcurrent();
  }

  @Override
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a single cpu light thread.")
  public void multipleBlocking() throws Exception {
    super.multipleBlocking();
    assertThreads(0, 1, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a single cpu light thread.")
  public void singleCpuIntensive() throws Exception {
    super.singleCpuIntensive();
    assertThreads(0, 1, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a single cpu light thread.")
  public void multipleCpuIntensive() throws Exception {
    super.multipleCpuIntensive();
    assertThreads(0, 1, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a single cpu light thread.")
  public void mix() throws Exception {
    super.mix();
    assertThreads(0, 1, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the ReactorProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a single cpu light thread.")
  public void mix2() throws Exception {
    super.mix2();
    assertThreads(0, 1, 0, 0);
  }

  @Override
  @Description("When the ReactorProcessingStrategy is configured and a transaction is active processing fails with an error")
  public void tx() throws Exception {
    flow.setMessageProcessors(asList(cpuLightProcessor, cpuIntensiveProcessor, blockingProcessor));
    flow.initialise();
    flow.start();

    TransactionCoordination.getInstance().bindTransaction(new TestTransaction(muleContext));

    expectedException.expect(DefaultMuleException.class);
    expectedException.expectMessage(equalTo(TRANSACTIONAL_ERROR_MESSAGE));
    process(flow, testEvent());
  }

}
