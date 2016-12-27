/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.ws.security;

import static org.apache.ws.security.handler.WSHandlerConstants.ACTION;
import static org.apache.ws.security.handler.WSHandlerConstants.TIMESTAMP;
import static org.apache.ws.security.handler.WSHandlerConstants.TTL_TIMESTAMP;
import static org.apache.ws.security.handler.WSHandlerConstants.USERNAME_TOKEN;
import static org.junit.Assert.assertEquals;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

@SmallTest
public class WssTimestampSecurityStrategyTestCase extends AbstractMuleTestCase
{
    private static final long EXPIRES = 30;

    private WssTimestampSecurityStrategy strategy = new WssTimestampSecurityStrategy();

    @Test
    public void actionAndTtlTimestampFieldsAreSetOnEmptyMap()
    {
        Map<String, Object> outConfigProperties = new HashMap<String, Object>();
        Map<String, Object> inConfigProperties = new HashMap<String, Object>();
        strategy.setExpires(EXPIRES);
        strategy.apply(outConfigProperties, inConfigProperties);

        assertEquals(TIMESTAMP, outConfigProperties.get(ACTION));
        assertEquals(String.valueOf(EXPIRES), outConfigProperties.get(TTL_TIMESTAMP));
        assertEquals(TIMESTAMP, inConfigProperties.get(ACTION));
        assertEquals(String.valueOf(EXPIRES), inConfigProperties.get(TTL_TIMESTAMP));
    }

    @Test
    public void actionIsAppendedAfterExistingAction()
    {
        Map<String, Object> outConfigProperties = new HashMap<String, Object>();
        outConfigProperties.put(ACTION, USERNAME_TOKEN);
        Map<String, Object> inConfigProperties = new HashMap<String, Object>();
        inConfigProperties.put(ACTION, USERNAME_TOKEN);

        strategy.setExpires(EXPIRES);
        strategy.apply(outConfigProperties, inConfigProperties);

        String expectedAction = USERNAME_TOKEN + " " + TIMESTAMP;
        assertEquals(expectedAction, outConfigProperties.get(ACTION));
        assertEquals(String.valueOf(EXPIRES), outConfigProperties.get(TTL_TIMESTAMP));
        assertEquals(expectedAction, inConfigProperties.get(ACTION));
        assertEquals(String.valueOf(EXPIRES), inConfigProperties.get(TTL_TIMESTAMP));
    }


}
