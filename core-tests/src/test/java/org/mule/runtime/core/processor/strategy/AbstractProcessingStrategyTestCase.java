/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType.BLOCKING;
import static reactor.core.Exceptions.unwrap;
import static reactor.core.publisher.Mono.from;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.scheduler.Scheduler;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.processor.ReactiveProcessor.ProcessingType;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.registry.RegistrationException;
import org.mule.runtime.core.construct.Flow;
import org.mule.runtime.core.util.concurrent.Latch;
import org.mule.runtime.core.util.concurrent.NamedThreadFactory;
import org.mule.tck.junit4.AbstractMuleContextTestCase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class AbstractProcessingStrategyTestCase extends AbstractMuleContextTestCase
{

  protected static int LATCH_TIMEOUT_MS = 200;
  protected static final String CPU_LIGHT = "cpuLight";
  protected static final String IO = "I/O";
  protected static final String CPU_INTENSIVE = "cpuIntensive";

  protected Flow flow;
  protected volatile Set<String> threads = new HashSet<>();
  protected Processor cpuLightProcessor = new ThreadTrackingProcessor() {

    @Override
    public ProcessingType getProcessingType() {
      return ProcessingType.CPU_LITE;
    }
  };
  protected Processor cpuIntensiveProcessor = new ThreadTrackingProcessor() {

    @Override
    public ProcessingType getProcessingType() {
      return ProcessingType.CPU_INTENSIVE;
    }
  };
  protected Processor blockingProcessor = new ThreadTrackingProcessor() {

    @Override
    public ProcessingType getProcessingType() {
      return BLOCKING;
    }
  };

  protected Scheduler cpuLight;
  protected Scheduler blocking;
  protected Scheduler cpuIntensive;
  private ExecutorService asyncExecutor;
  private Mode mode;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  public AbstractProcessingStrategyTestCase(Mode mode) {
    this.mode = mode;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> parameters() {
    return asList(new Object[][] {{Mode.BLOCKING}, {Mode.ASYNC}});
  }

  @Before
  public void before() throws RegistrationException {
    cpuLight = new TestScheduler(10, CPU_LIGHT);
    blocking = new TestScheduler(10, IO);
    cpuIntensive = new TestScheduler(10, CPU_INTENSIVE);
    asyncExecutor = newSingleThreadExecutor();

    flow = new Flow("test", muleContext);
    flow.setProcessingStrategyFactory((muleContext, prefix) -> createProcessingStrategy(muleContext, prefix));
  }

  protected abstract ProcessingStrategy createProcessingStrategy(MuleContext muleContext, String schedulersNamePrefix);

  @After
  public void after() {
    cpuLight.shutdownNow();
    blocking.shutdownNow();
    cpuIntensive.shutdownNow();
    asyncExecutor.shutdownNow();
  }

  @Test
  public void singleCpuLight() throws Exception {
    flow.setMessageProcessors(singletonList(cpuLightProcessor));
    flow.initialise();
    flow.start();
    process(flow, testEvent());
  }

  @Test
  public void singleCpuLightConcurrent() throws Exception {
    FirstInvocationLatchedProcessor latchedProcessor = new FirstInvocationLatchedProcessor(ProcessingType.CPU_LITE);

    flow.setMessageProcessors(singletonList(latchedProcessor));
    flow.initialise();
    flow.start();

    asyncExecutor.submit(() -> process(flow, testEvent()));

    latchedProcessor.awaitFirst();
    process(flow, testEvent());
    latchedProcessor.releaseFirst();
  }

  @Test
  public void multipleCpuLight() throws Exception {
    flow.setMessageProcessors(asList(cpuLightProcessor, cpuLightProcessor, cpuLightProcessor));
    flow.initialise();
    flow.start();

    process(flow, testEvent());
  }

  @Test
  public void singleBlocking() throws Exception {
    flow.setMessageProcessors(singletonList(blockingProcessor));
    flow.initialise();
    flow.start();

    process(flow, testEvent());
  }

  @Test
  public void singleBlockingConcurrent() throws Exception {
    FirstInvocationLatchedProcessor latchedProcessor = new FirstInvocationLatchedProcessor(BLOCKING);

    flow.setMessageProcessors(singletonList(latchedProcessor));
    flow.initialise();
    flow.start();

    asyncExecutor.submit(() -> process(flow, testEvent()));

    latchedProcessor.awaitFirst();
    process(flow, testEvent());
    latchedProcessor.releaseFirst();
  }

  @Test
  public void multipleBlocking() throws Exception {
    flow.setMessageProcessors(asList(blockingProcessor, blockingProcessor, blockingProcessor));
    flow.initialise();
    flow.start();

    process(flow, testEvent());
  }

  @Test
  public void singleCpuIntensive() throws Exception {
    flow.setMessageProcessors(singletonList(cpuIntensiveProcessor));
    flow.initialise();
    flow.start();

    process(flow, testEvent());
  }

  @Test
  public void multipleCpuIntensive() throws Exception {
    flow.setMessageProcessors(asList(cpuIntensiveProcessor, cpuIntensiveProcessor, cpuIntensiveProcessor));
    flow.initialise();
    flow.start();

    process(flow, testEvent());
  }

  @Test
  public void mix() throws Exception {
    flow.setMessageProcessors(asList(cpuLightProcessor, cpuIntensiveProcessor, blockingProcessor));
    flow.initialise();
    flow.start();

    process(flow, testEvent());
  }

  @Test
  public void mix2() throws Exception {
    flow.setMessageProcessors(asList(cpuLightProcessor, cpuLightProcessor, blockingProcessor, blockingProcessor,
                                     cpuLightProcessor, cpuIntensiveProcessor, cpuIntensiveProcessor, cpuLightProcessor));
    flow.initialise();
    flow.start();

    process(flow, testEvent());
  }

  @Test
  public abstract void tx() throws Exception;

  protected void assertThreads(int test, int cpuLite, int io, int cpuIntensive)
  {
    assertThat(threads.size(), equalTo(test + cpuLite + io + cpuIntensive));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_LIGHT)).count(), equalTo((long) cpuLite));
    assertThat(threads.stream().filter(name -> name.startsWith(IO)).count(), equalTo((long) io));
    assertThat(threads.stream().filter(name -> name.startsWith(CPU_INTENSIVE)).count(), equalTo((long) cpuIntensive));
  }

  class FirstInvocationLatchedProcessor implements Processor {

    private ProcessingType type;
    private volatile Latch latch = new Latch();
    private volatile Latch firstCalledLatch = new Latch();
    private AtomicBoolean firstCalled = new AtomicBoolean();

    public FirstInvocationLatchedProcessor(ProcessingType type) {
      this.type = type;
    }

    @Override
    public Event process(Event event) throws MuleException {
      threads.add(currentThread().getName());
      if (firstCalled.compareAndSet(false, true)) {
        firstCalledLatch.release();
        try {
          if(!latch.await(LATCH_TIMEOUT_MS, MILLISECONDS)){
            throw new DefaultMuleException(new TimeoutException(""));
          }
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }

      return event;
    }

    @Override
    public ProcessingType getProcessingType() {
      return type;
    }

    public void releaseFirst() {
      latch.release();
    }

    public void awaitFirst() throws InterruptedException {
      firstCalledLatch.await();
    }

  }

  static class TestScheduler extends ScheduledThreadPoolExecutor implements Scheduler {

    public TestScheduler(int threads, String threadNamePrefix) {
      super(threads, new NamedThreadFactory(threadNamePrefix));
    }

    @Override
    public Future<?> submit(Runnable task) {
      return super.submit(task);
    }

    @Override
    public void stop(long gracefulShutdownTimeoutSecs, TimeUnit unit) {
      // Nothing to do.
    }

    @Override
    public ScheduledFuture<?> scheduleWithCronExpression(Runnable command, String cronExpression) {
      throw new UnsupportedOperationException(
                                              "Cron expression scheduling is not supported in unit tests. You need the productive service implementation.");
    }

    @Override
    public ScheduledFuture<?> scheduleWithCronExpression(Runnable command, String cronExpression, TimeZone timeZone) {
      throw new UnsupportedOperationException(
                                              "Cron expression scheduling is not supported in unit tests. You need the productive service implementation.");
    }

    @Override
    public String getName() {
      return TestScheduler.class.getSimpleName();
    }

  }

  class ThreadTrackingProcessor implements Processor {

    @Override
    public Event process(Event event) {
      threads.add(currentThread().getName());
      return event;
    }

  }

  protected Event process(Flow flow, Event event) throws Exception {
    switch (mode) {
      case BLOCKING:
        return flow.process(event);
      case ASYNC:
        try {
          return from(flow.processAsync(event)).block();
        } catch (Throwable exception) {
          throw (Exception) unwrap(exception);
        }
      default:
        return null;
    }
  }

  public enum Mode {
    BLOCKING,
    ASYNC,
  }


}
