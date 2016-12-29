/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.oauth2.internal;

import static org.mule.extension.http.api.HttpConstants.HttpStatus.FORBIDDEN;
import static org.mule.extension.http.api.HttpConstants.HttpStatus.UNAUTHORIZED;

import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.request.authentication.HttpAuthentication;
import org.mule.extension.oauth2.internal.tokenmanager.TokenManagerConfig;
import org.mule.runtime.api.message.Attributes;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.context.MuleContextAware;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.function.Function;

/**
 * Common interface for all grant types must extend this interface.
 */
public abstract class AbstractGrantType implements HttpAuthentication, ApplicationCredentials, MuleContextAware {

  protected MuleContext muleContext;

  /**
   * The token manager configuration to use for this grant type.
   */
  @Parameter
  @Optional
  @Alias("tokenManager-ref")
  protected TokenManagerConfig tokenManager;

  protected abstract Function<Event, Boolean> getRefreshTokenWhen();

  protected boolean evaluateShouldRetry(final Event firstAttemptResponseEvent) {
    if (getRefreshTokenWhen() != null) {
      return getRefreshTokenWhen().apply(firstAttemptResponseEvent);
    } else {
      final Attributes attributes = firstAttemptResponseEvent.getMessage().getAttributes();
      if (attributes instanceof HttpResponseAttributes) {
        return ((HttpResponseAttributes) attributes).getStatusCode() == UNAUTHORIZED.getStatusCode()
            || ((HttpResponseAttributes) attributes).getStatusCode() == FORBIDDEN.getStatusCode();
      } else {
        return false;
      }
    }
  }

  /**
   * @param accessToken an ouath access token
   * @return the content of the HTTP authentication header.
   */
  public static String buildAuthorizationHeaderContent(String accessToken) {
    return "Bearer " + accessToken;
  }

  @Override
  public void setMuleContext(MuleContext muleContext) {
    this.muleContext = muleContext;
  }
}
