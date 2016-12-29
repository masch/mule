/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.ws;

import static org.mule.extension.ws.WscTestUtils.HEADER_IN;
import static org.mule.extension.ws.WscTestUtils.HEADER_INOUT;
import static org.mule.extension.ws.WscTestUtils.getRequestResource;
import org.mule.extension.ws.api.SoapVersion;
import org.mule.extension.ws.service.Soap11Service;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.message.Message;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.ClassRule;

//@RunnerDelegateTo(Parameterized.class)
@ArtifactClassLoaderRunnerConfig(
  plugins = {"org.mule.modules:mule-module-wsc", "org.mule.modules:mule-module-http-ext", "org.mule.modules:mule-module-sockets"})
public abstract class AbstractSoapServiceTestCase extends MuleArtifactFunctionalTestCase {

  @ClassRule
  public static DynamicPort servicePort = new DynamicPort("servicePort");

  //@Parameterized.Parameter
  public SoapVersion soapVersion = SoapVersion.SOAP11;

  //@Parameterized.Parameter(1)
  public String serviceClass = Soap11Service.class.getName();

  //@Parameterized.Parameters(name = "{0}")
  //public static Collection<Object[]> data() {
  //  return asList(new Object[][] {
  //      {SOAP11, Soap11Service.class.getName()},
  //      {SOAP12, Soap12Service.class.getName()}
  //  });
  //}

  @Override
  protected String[] getConfigFiles() {
    return new String[] {"config/soapService.xml", getConfigurationFile()};
  }

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    System.setProperty("soapVersion", soapVersion.toString());
    System.setProperty("serviceClass", getServiceClass());
    XMLUnit.setIgnoreWhitespace(true);
  }

  protected Message runFlowWithRequest(String flowName, String requestXmlResourceName) throws Exception {
    return flowRunner(flowName)
      .withPayload(getRequestResource(requestXmlResourceName))
      .withVariable(HEADER_IN, getRequestResource(HEADER_IN))
      .withVariable(HEADER_INOUT, getRequestResource(HEADER_INOUT))
      .run()
      .getMessage();
  }

  protected abstract String getConfigurationFile();

  protected String getServiceClass() {
    return serviceClass;
  }
}
