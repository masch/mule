/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformers.simple;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mule.api.transport.PropertyScope.SESSION;
import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.util.concurrent.Latch;


import org.junit.Rule;
import org.junit.Test;

public class SessionVariableAfterJavaComponentTestCase extends FunctionalTestCase
{

    private static final String SESSION_VARIABLE_VALUE = "TEST";
    private static Latch latch = new Latch();

    @Rule
    public SystemProperty sessionVariableSystemProperty = new SystemProperty("sessionVariable", SESSION_VARIABLE_VALUE);

    @Override
    protected String getConfigFile()
    {
        return "session-variable-after-java-component.xml";
    }


    @Test
    public void testSessionVariableExistsAfterJavaComponent() throws Exception
    {
        runFlow("main");
        latch.await(RECEIVE_TIMEOUT, MILLISECONDS);
        assertThat(DispatchJavaComponent.sessionVariableValue, is(SESSION_VARIABLE_VALUE));
        latch.await(RECEIVE_TIMEOUT, MILLISECONDS);
        assertThat(OutboundJavaComponent.sessionVariableValue, is(SESSION_VARIABLE_VALUE));
    }

    public static class DispatchJavaComponent implements Callable
    {

        private static String sessionVariableValue = null;

        @Override
        public Object onCall(MuleEventContext eventContext) throws Exception
        {
            sessionVariableValue = eventContext.getMessage().getProperty("testVariable", SESSION);
            eventContext.getMuleContext().getClient().dispatch("vm://in", eventContext.getMessage());
            latch.release();
            return eventContext.getMessage().getPayload();
        }
    }

    public static class OutboundJavaComponent implements Callable
    {

        private static String sessionVariableValue = null;

        @Override
        public Object onCall(MuleEventContext eventContext) throws Exception
        {
            sessionVariableValue = eventContext.getMessage().getProperty("testVariable", SESSION);
            latch.release();
            return eventContext.getMessage().getPayload();
        }
    }
}
