/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.compatibility.transport.http.issues;

import org.mule.functional.functional.EventCallback;
import org.mule.functional.functional.FunctionalTestNotification;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.MuleEventContext;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.core.api.lifecycle.Callable;
import org.mule.runtime.api.i18n.I18nMessageFactory;
import org.mule.runtime.core.util.StringMessageUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>FunctionalTestComponent</code> is a service that can be used by functional tests. This service accepts an EventCallback
 * that can be used to assert the state of the current event.
 * <p/>
 * Also, this service fires {@link org.mule.tck.functional.FunctionalTestNotification} via Mule for every message received. Tests
 * can register with Mule to receive these events by implementing
 * {@link org.mule.tck.functional.FunctionalTestNotificationListener}.
 *
 * @see org.mule.tck.functional.EventCallback
 * @see org.mule.tck.functional.FunctionalTestNotification
 * @see org.mule.tck.functional.FunctionalTestNotificationListener
 */

public class NoTransformFunctionalTestComponent implements Callable, MuleContextAware {

  protected transient Logger logger = LoggerFactory.getLogger(getClass());

  public static final int STREAM_SAMPLE_SIZE = 4;
  public static final int STREAM_BUFFER_SIZE = 4096;
  private EventCallback eventCallback;
  private Object returnMessage = null;
  private boolean appendComponentName = false;
  private boolean throwException = false;

  private MuleContext muleContext;

  /**
   * {@inheritDoc}
   */
  @Override
  public Object onCall(MuleEventContext context) throws Exception {
    String contents = context.getMessageAsString(muleContext);
    String msg = StringMessageUtils.getBoilerPlate("Message Received in service: "
        + context.getFlowConstruct().getName() + ". Content is: "
        + StringMessageUtils.truncate(contents, 100, true), '*', 80);

    logger.info(msg);

    if (eventCallback != null) {
      eventCallback.eventReceived(context, this, muleContext);
    }

    Object replyMessage;
    if (returnMessage != null) {
      replyMessage = returnMessage;
    } else {
      replyMessage = received(contents) + (appendComponentName ? " " + context.getFlowConstruct().getName() : "");
    }

    muleContext
        .fireNotification(new FunctionalTestNotification(context, replyMessage, FunctionalTestNotification.EVENT_RECEIVED));

    if (throwException) {
      throw new DefaultMuleException(I18nMessageFactory.createStaticMessage("Functional Test Service Exception"));
    }

    return replyMessage;
  }

  /**
   * Append " Received" to contents. Exposed as static method so tests can call to construct string for comparison.
   *
   * @param contents
   * @return Extended message
   */
  public static String received(String contents) {
    return contents + " Received";
  }

  /**
   * An event callback is called when a message is received by the service. An MuleEvent callback isn't strictly required but it
   * is usfal for performing assertions on the current message being received. Note that the FunctionalTestComponent should be
   * made a singleton when using MuleEvent callbacks
   * <p/>
   * Another option is to register a {@link org.mule.tck.functional.FunctionalTestNotificationListener} with Mule and this will
   * deleiver a {@link org.mule.tck.functional.FunctionalTestNotification} for every message received by this service
   *
   * @return the callback to call when a message is received
   * @see org.mule.tck.functional.FunctionalTestNotification
   * @see org.mule.tck.functional.FunctionalTestNotificationListener
   */
  public EventCallback getEventCallback() {
    return eventCallback;
  }

  /**
   * An event callback is called when a message is received by the service. An MuleEvent callback isn't strictly required but it
   * is usfal for performing assertions on the current message being received. Note that the FunctionalTestComponent should be
   * made a singleton when using MuleEvent callbacks
   * <p/>
   * Another option is to register a {@link org.mule.tck.functional.FunctionalTestNotificationListener} with Mule and this will
   * deleiver a {@link org.mule.tck.functional.FunctionalTestNotification} for every message received by this service
   *
   * @param eventCallback the callback to call when a message is received
   * @see org.mule.tck.functional.FunctionalTestNotification
   * @see org.mule.tck.functional.FunctionalTestNotificationListener
   */
  public void setEventCallback(EventCallback eventCallback) {
    this.eventCallback = eventCallback;
  }

  /**
   * Often you will may want to return a fixed message payload to simulate and external system call. This can be done using the
   * 'returnEvent' property. Note that you can return complex objects by using the <container-property> element in the Xml
   * configuration.
   *
   * @return the message payload to always return from this service instance
   */
  public Object getReturnMessage() {
    return returnMessage;
  }

  /**
   * Often you will may want to return a fixed message payload to simulate and external system call. This can be done using the
   * 'returnEvent' property. Note that you can return complex objects by using the <container-property> element in the Xml
   * configuration.
   *
   * @param returnMessage the message payload to always return from this service instance
   */
  public void setReturnMessage(Object returnMessage) {
    this.returnMessage = returnMessage;
  }

  /**
   * Sometimes you will want the service to always throw an exception, if this is the case you can set the 'throwException'
   * property to true.
   *
   * @return throwException true if an exception should always be thrown from this instance. If the {@link #returnMessage}
   *         property is set and is of type java.lang.Exception, that exception will be thrown.
   */
  public boolean isThrowException() {
    return throwException;
  }

  /**
   * Sometimes you will want the service to always throw an exception, if this is the case you can set the 'throwException'
   * property to true.
   *
   * @param throwException true if an exception should always be thrown from this instance. If the {@link #returnMessage} property
   *        is set and is of type java.lang.Exception, that exception will be thrown.
   */
  public void setThrowException(boolean throwException) {
    this.throwException = throwException;
  }

  /**
   * This will cause the service to append the compoent name to the end of the message returned from this service. This only works
   * when processing String messages. This feature is useful when processing multiple messages using a pool of
   * FunctionalTestComponents to determine who processed the resulting message
   *
   * @return true if the service name will be appended to the return message
   */
  public boolean isAppendComponentName() {
    return appendComponentName;
  }

  /**
   * This will cause the service to append the compoent name to the end of the message returned from this service. This only works
   * when processing String messages. This feature is useful when processing multiple messages using a pool of
   * FunctionalTestComponents to determine who processed the resulting message
   *
   * @param appendComponentName true if the service name will be appended to the return message
   */
  public void setAppendComponentName(boolean appendComponentName) {
    this.appendComponentName = appendComponentName;
  }

  @Override
  public void setMuleContext(MuleContext context) {
    this.muleContext = context;
  }
}
