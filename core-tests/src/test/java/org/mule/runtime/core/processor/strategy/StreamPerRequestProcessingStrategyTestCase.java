/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.internal.matchers.ThrowableCauseMatcher.hasCause;
import static org.mule.runtime.core.processor.strategy.SynchronousProcessingStrategyFactory.SYNCHRONOUS_PROCESSING_STRATEGY_INSTANCE;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.exception.MessagingException;
import org.mule.runtime.core.transaction.TransactionCoordination;
import org.mule.tck.testmodels.mule.TestTransaction;

import java.util.concurrent.TimeoutException;

import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

@Features("Processing Strategies")
@Stories("Synchronous Processing Strategy")
public class StreamPerRequestProcessingStrategyTestCase extends AbstractProcessingStrategyTestCase {

  public StreamPerRequestProcessingStrategyTestCase(Mode mode) {
    super(mode);
  }

  @Override
  protected ProcessingStrategy createProcessingStrategy(MuleContext muleContext, String schedulersNamePrefix) {
    return new StreamPerRequestProcesingStrategyFactory().create(muleContext, "");
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleCpuLight() throws Exception {
    super.singleCpuLight();
    assertThreads(1, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleCpuLightConcurrent() throws Exception {
    super.singleCpuLightConcurrent();
    assertThreads(2, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void multipleCpuLight() throws Exception {
    super.multipleCpuLight();
    assertThreads(1, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleBlocking() throws Exception {
    super.singleBlocking();
    assertThreads(1, 0, 0, 0);
  }

  @Override
  public void singleBlockingConcurrent() throws Exception {
    super.singleBlockingConcurrent();
    assertThreads(2, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void multipleBlocking() throws Exception {
    super.multipleBlocking();
    assertThreads(1, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void singleCpuIntensive() throws Exception {
    super.singleCpuIntensive();
    assertThreads(1, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void multipleCpuIntensive() throws Exception {
    super.multipleCpuIntensive();
    assertThreads(1, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void mix() throws Exception {
    super.mix();
    assertThreads(1, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void mix2() throws Exception {
    super.mix2();
    assertThreads(1, 0, 0, 0);
  }

  @Override
  @Description("Regardless of processor type, when the SynchronousProcessingStrategy is configured, the pipeline is executed "
      + "synchronously in a caller thread.")
  public void tx() throws Exception {
    flow.setMessageProcessors(asList(cpuLightProcessor, cpuIntensiveProcessor, blockingProcessor));
    flow.initialise();
    flow.start();

    TransactionCoordination.getInstance().bindTransaction(new TestTransaction(muleContext));

    process(flow, testEvent());

    assertThreads(1, 0, 0, 0);
  }


}
