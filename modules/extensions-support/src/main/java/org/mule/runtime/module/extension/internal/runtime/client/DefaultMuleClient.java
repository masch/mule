/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.client;

import static java.util.stream.Collectors.toList;
import static org.mule.runtime.module.extension.internal.util.MuleExtensionUtils.getInitialiserEvent;
import static reactor.core.publisher.Flux.from;
import static reactor.core.publisher.Mono.just;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.message.Attributes;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.policy.PolicyManager;
import org.mule.runtime.extension.api.client.MuleClient;
import org.mule.runtime.extension.api.client.OperationParameters;
import org.mule.runtime.extension.api.runtime.ConfigurationProvider;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.module.extension.internal.manager.ExtensionManagerAdapter;
import org.mule.runtime.module.extension.internal.runtime.operation.OperationMessageProcessor;
import org.mule.runtime.module.extension.internal.runtime.resolver.ResolverSet;
import org.mule.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;

import com.google.common.collect.ImmutableList;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.reactivestreams.Publisher;

public class DefaultMuleClient implements MuleClient, Initialisable {

  @Inject
  private MuleContext muleContext;

  private PolicyManager policyManager;
  private ExtensionManagerAdapter extensionManager;

  private final Map<String, ConfigurationProvider> configurations = new LinkedHashMap<>();
  private final Map<String, OperationModel> operations = new LinkedHashMap<>();

  @Override
  public void initialise() throws InitialisationException {
    policyManager = muleContext.getRegistry().lookupByType(PolicyManager.class).values().stream().findAny()
      .orElseThrow(() -> new RuntimeException("Error obtaining PolicyManager"));
    extensionManager = (ExtensionManagerAdapter) muleContext.getExtensionManager();
  }

  @Override
  public <T, A extends Attributes> Publisher<Result<T, A>> executeAsync(String extension, String operation,
                                                                        OperationParameters parameters) throws MuleException {
    OperationMessageProcessor processor = createProcessor(extension, operation, parameters);
    Publisher<Event> publisher = processor.apply(just(getInitialiserEvent(muleContext)));
    return from(publisher).map(e -> Result.<T, A>builder(e.getMessage()).build());
  }

  @Override
  public <T, A extends Attributes> Result<T, A> execute(String extension, String operation, OperationParameters parameters)
    throws MuleException {
    OperationMessageProcessor processor = createProcessor(extension, operation, parameters);
    Event process = processor.process(getInitialiserEvent(muleContext));
    return (Result<T, A>) Result.builder(process.getMessage()).build();
  }

  private OperationMessageProcessor createProcessor(String extension, String operation, OperationParameters parameters) {
    ExtensionModel extensionModel = findExtension(extension);
    OperationModel operationModel = findOperation(extensionModel, operation);
    String configName = (String) parameters.getParameter("config-ref");
    ConfigurationProvider configuration = findConfiguration(configName);
    ResolverSet resolverSet = toResolverSet(parameters, operationModel);
    return buildOperationMessageProcessor(extensionModel, operationModel, configuration, resolverSet);
  }

  private ExtensionModel findExtension(String extension) {
    return extensionManager.getExtension(extension)
      .orElseThrow(() -> new RuntimeException("No Extension [" + extension + "] Found"));
  }

  private OperationMessageProcessor buildOperationMessageProcessor(ExtensionModel extension, OperationModel operation,
                                                                   ConfigurationProvider configuration, ResolverSet resolverSet) {
    try {
      OperationMessageProcessor processor = new OperationMessageProcessor(extension,
                                                                          operation,
                                                                          configuration,
                                                                          "",
                                                                          resolverSet,
                                                                          extensionManager,
                                                                          policyManager);
      processor.setMuleContext(muleContext);
      processor.doInitialise();
      processor.doStart();
      return processor;
    } catch (MuleException e) {
      throw new MuleRuntimeException(e);
    }
  }

  private ResolverSet toResolverSet(OperationParameters parameters, OperationModel operationModel) {
    ResolverSet resolverSet = new ResolverSet();
    // TODO: check what happens with NullSafe parameters.
    operationModel.getAllParameterModels().forEach(p -> {
      Object param = parameters.getParameter(p.getName());
      if (param != null) {
        resolverSet.add(p.getName(), new StaticValueResolver<>(param));
      }
      // TODO: this should consider cases when the default value is represented as an String but is from another type. e.g. Enum.
      //else {
      //  if (p.getDefaultValue() != null) {
      //    resolverSet.add(p.getName(), new StaticValueResolver<>(p.getDefaultValue()));
      //  }
      //}
    });
    return resolverSet;
  }

  private ConfigurationProvider findConfiguration(String configName) {
    return configurations.computeIfAbsent(configName, name -> extensionManager.getConfigurationProvider(configName)
      .orElseThrow(() -> new RuntimeException("No configuration [" + configName + "] found")));
  }

  private OperationModel findOperation(ExtensionModel extensionModel, String operationName) {
    return operations.computeIfAbsent(operationName, operation -> ImmutableList.<OperationModel>builder()
      .addAll(extensionModel.getConfigurationModels().stream().flatMap(c -> c.getOperationModels().stream()).collect(toList()))
      .addAll(extensionModel.getOperationModels()).build().stream().filter(o -> o.getName().equalsIgnoreCase(operation))
      .findAny().orElseThrow(() -> new RuntimeException("No Operation [" + operation + "] Found")));
  }
}
