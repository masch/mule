/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.model.resolvers;

import static org.mule.api.transport.PropertyScope.SESSION;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.MuleRuntimeException;
import org.mule.api.MuleSession;
import org.mule.api.lifecycle.Callable;
import org.mule.api.model.EntryPointResolver;
import org.mule.api.model.InvocationResult;
import org.mule.config.i18n.CoreMessages;
import org.mule.config.i18n.MessageFactory;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

/**
 * An entry-point resolver that only allows Service objects that implement the
 * Callable interface
 *
 * @see org.mule.api.lifecycle.Callable
 */
public class CallableEntryPointResolver implements EntryPointResolver
{
    protected static final Method callableMethod;

    static
    {
        try
        {
            callableMethod = Callable.class.getMethod("onCall", new Class[] {MuleEventContext.class});
        }
        catch (NoSuchMethodException e)
        {
            throw new MuleRuntimeException(
                    MessageFactory.createStaticMessage("Panic! No onCall(MuleEventContext) method found in the Callable interface."));
        }
    }

    public InvocationResult invoke(Object component, MuleEventContext context) throws Exception
    {
        if (component instanceof Callable)
        {
            copySessionProperties(context.getSession(), context.getMessage());
            Object result = ((Callable) component).onCall(context);
            return new InvocationResult(this, result, callableMethod);
        }
        else
        {
            InvocationResult result = new InvocationResult(this, InvocationResult.State.NOT_SUPPORTED);
            result.setErrorMessage(CoreMessages.objectDoesNotImplementInterface(component, Callable.class).toString());
            return result;
        }
    }

    private void copySessionProperties(MuleSession session, MuleMessage muleMessage)
    {
        Set<String> namesProperties = session.getPropertyNamesAsSet();
        Iterator<String> namesPropertiesIterator = namesProperties.iterator();
        while (namesPropertiesIterator.hasNext())
        {
            String propertyName = namesPropertiesIterator.next();
            muleMessage.setProperty(propertyName, session.getProperty(propertyName), SESSION);
        }
    }

    @Override
    public String toString()
    {
        return "CallableEntryPointResolver{}";
    }
}
