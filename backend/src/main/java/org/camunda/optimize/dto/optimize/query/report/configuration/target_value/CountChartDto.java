package org.camunda.optimize.dto.optimize.query.report.configuration.target_value;

import java.util.Objects;

public class CountChartDto {

  private Boolean isBelow = false;
  private Integer value = 100;

  public Boolean getBelow() {
    return isBelow;
  }

  public void setBelow(Boolean below) {
    isBelow = below;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CountChartDto)) {
      return false;
    }
    CountChartDto that = (CountChartDto) o;
    return Objects.equals(isBelow, that.isBelow) &&
      Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isBelow, value);
  }
}
