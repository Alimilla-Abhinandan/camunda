package org.camunda.optimize.dto.optimize.query.report.single.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.camunda.optimize.dto.optimize.query.report.Combinable;
import org.camunda.optimize.dto.optimize.query.report.single.group.value.GroupByValueDto;
import org.camunda.optimize.service.es.report.command.util.ReportUtil;

import java.util.Objects;

import static org.camunda.optimize.service.es.report.command.util.ReportConstants.GROUP_BY_FLOW_NODES_TYPE;
import static org.camunda.optimize.service.es.report.command.util.ReportConstants.GROUP_BY_NONE_TYPE;
import static org.camunda.optimize.service.es.report.command.util.ReportConstants.GROUP_BY_START_DATE_TYPE;
import static org.camunda.optimize.service.es.report.command.util.ReportConstants.GROUP_BY_VARIABLE_TYPE;


/**
 * Abstract class that contains a hidden "type" field to distinguish, which
 * group by type the jackson object mapper should transform the object to.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = StartDateGroupByDto.class, name = GROUP_BY_START_DATE_TYPE),
    @JsonSubTypes.Type(value = FlowNodesGroupByDto.class, name = GROUP_BY_FLOW_NODES_TYPE),
    @JsonSubTypes.Type(value = NoneGroupByDto.class, name = GROUP_BY_NONE_TYPE),
    @JsonSubTypes.Type(value = VariableGroupByDto.class, name = GROUP_BY_VARIABLE_TYPE)
}
)
public abstract class GroupByDto<VALUE extends GroupByValueDto> implements Combinable {

  @JsonProperty
  protected String type;
  protected VALUE value;

  public VALUE getValue() {
    return value;
  }

  public void setValue(VALUE value) {
    this.value = value;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return type;
  }

  @Override
  public boolean isCombinable(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GroupByDto)) {
      return false;
    }
    GroupByDto<?> that = (GroupByDto<?>) o;
    return Objects.equals(type, that.type) &&
      ReportUtil.isCombinable(value, that.value);
  }

  @JsonIgnore
  public String createCommandKey() {
    return type;
  }


}
