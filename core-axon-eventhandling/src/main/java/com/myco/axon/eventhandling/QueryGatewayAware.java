package com.myco.axon.eventhandling;

import org.axonframework.queryhandling.QueryGateway;

public interface QueryGatewayAware {

  QueryGateway queryGateway();
}
