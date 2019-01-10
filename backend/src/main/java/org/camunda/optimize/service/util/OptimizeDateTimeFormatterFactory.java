package org.camunda.optimize.service.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

import static org.camunda.optimize.upgrade.es.ElasticsearchConstants.OPTIMIZE_DATE_FORMAT;

@Component
public class OptimizeDateTimeFormatterFactory implements FactoryBean<DateTimeFormatter> {

  private DateTimeFormatter dateTimeFormatter;

  @Override
  public DateTimeFormatter getObject() throws Exception {
    if (dateTimeFormatter == null) {
      dateTimeFormatter = DateTimeFormatter.ofPattern(OPTIMIZE_DATE_FORMAT);
    }
    return dateTimeFormatter;
  }

  @Override
  public Class<?> getObjectType() {
    return DateTimeFormatter.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
