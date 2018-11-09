package org.camunda.operate.property;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ZeebeProperties {

  private String brokerContactPoint = "localhost:26500";

  private List<String> topics = Collections.singletonList("default-topic");

  private String worker;

  public String getBrokerContactPoint() {
    return brokerContactPoint;
  }

  public void setBrokerContactPoint(String brokerContactPoint) {
    this.brokerContactPoint = brokerContactPoint;
  }

  public List<String> getTopics() {
    return topics;
  }

  public void setTopics(List<String> topics) {
    this.topics = topics;
  }

  public String getWorker() {
    return worker;
  }

  public void setWorker(String worker) {
    this.worker = worker;
  }
}