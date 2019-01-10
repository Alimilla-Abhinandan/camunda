package org.camunda.optimize.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.optimize.dto.optimize.query.report.ReportDefinitionDto;
import org.camunda.optimize.dto.optimize.query.report.ReportType;
import org.camunda.optimize.dto.optimize.query.report.single.filter.data.variable.BooleanVariableFilterDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.ProcessReportDataDto;
import org.camunda.optimize.service.util.configuration.ConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class ObjectMapperFactoryTest {

  private ObjectMapperFactory objectMapperFactory;

  private ObjectMapper optimizeMapper;
  private ObjectMapper engineMapper;

  private ConfigurationService configurationService;
  private OptimizeDateTimeFormatterFactory optimizeDateTimeFormatterFactory;

  @Before
  public void init() throws Exception {
    configurationService = new ConfigurationService(new String[]{"service-config.yaml"});
    this.optimizeDateTimeFormatterFactory = new OptimizeDateTimeFormatterFactory();
    this.objectMapperFactory = new ObjectMapperFactory(
      optimizeDateTimeFormatterFactory.getObject(), configurationService
    );
    optimizeMapper = optimizeMapper == null ? objectMapperFactory.createOptimizeMapper() : optimizeMapper;
    engineMapper = engineMapper == null ? objectMapperFactory.createEngineMapper() : engineMapper;
  }

  /**
   * By default jackson fails if the external type id property is present but the actual property not.
   * In this case "reportType" is the external type id and "data" is the property.
   *
   * @throws Exception
   * @see com.fasterxml.jackson.databind.DeserializationFeature#FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY
   */
  @Test
  public void testNoFailOnMissingReportDataAlthoughReportTypeSet() throws Exception {
    final ReportDefinitionDto reportDefinitionDto = optimizeMapper.readValue(
      getClass().getResourceAsStream("/test/data/single-process-report-definition-create-request.json"),
      ReportDefinitionDto.class
    );
    assertThat(reportDefinitionDto.getCombined(), is(false));
    assertThat(reportDefinitionDto.getReportType(), is(ReportType.PROCESS));
    assertThat(reportDefinitionDto.getData(), is(nullValue()));
  }

  @Test
  public void testFilterSerialization() throws Exception {
    ProcessReportDataDto data = optimizeMapper.readValue(
      this.getClass()
        .getResourceAsStream("/test/data/filter_request.json"),
      ProcessReportDataDto.class
    );
    assertThat(((BooleanVariableFilterDataDto) data.getFilter().get(0).getData()).getData().getValue(), is("true"));

    data = optimizeMapper.readValue(
      this.getClass().getResourceAsStream("/test/data/filter_request_single.json"),
      ProcessReportDataDto.class
    );
    assertThat(((BooleanVariableFilterDataDto) data.getFilter().get(0).getData()).getData().getValue(), is("true"));
  }

  @Test
  public void testDateSerialization() throws Exception {
    DateHolder instance = new DateHolder();
    instance.setDate(OffsetDateTime.now());
    String s = optimizeMapper.writeValueAsString(instance);
    assertThat(s, is(notNullValue()));

    DateHolder result = optimizeMapper.readValue(
      s,
      DateHolder.class
    );
    assertThat(result.getDate(), is(instance.getDate()));
  }

  @Test
  public void testFromString() throws JsonProcessingException {
    String value = "2017-12-11T17:28:38.222+0100";
    DateHolder toTest = new DateHolder();
    toTest.setDate(OffsetDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));

    assertThat("value is [" + value + "]", optimizeMapper.writeValueAsString(toTest), containsString(value));
  }

  @Test
  public void testFromStringWithOldEngineFormat() throws JsonProcessingException {
    String value = "2017-12-11T17:28:38";
    DateHolder toTest = new DateHolder();
    toTest.setDate(
      ZonedDateTime
        .parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault()))
        .toOffsetDateTime()
    );

    assertThat("value is [" + value + "]", optimizeMapper.writeValueAsString(toTest), containsString(value));
  }

  @Test
  public void testEngineDateSerialization() throws Exception {
    DateHolder instance = new DateHolder();
    instance.setDate(OffsetDateTime.now());
    String s = engineMapper.writeValueAsString(instance);
    assertThat(s, is(notNullValue()));

    DateHolder result = engineMapper.readValue(
      s,
      DateHolder.class
    );
    assertThat(result.getDate(), is(instance.getDate()));
  }

  @Test
  public void testEngineDateFromString() throws JsonProcessingException {
    String value = "2017-12-11T17:28:38.222+0100";
    DateHolder toTest = new DateHolder();
    toTest.setDate(OffsetDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")));

    assertThat("value is [" + value + "]", engineMapper.writeValueAsString(toTest), containsString(value));
  }

  @Test
  public void testEngineDateFromStringWithOldEngineFormat() throws JsonProcessingException {
    String value = "2017-12-11T17:28:38";
    DateHolder toTest = new DateHolder();
    toTest.setDate(
      ZonedDateTime
        .parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.systemDefault()))
        .toOffsetDateTime()
    );

    assertThat("value is [" + value + "]", engineMapper.writeValueAsString(toTest), containsString(value));
  }


  static class DateHolder {
    private OffsetDateTime date;

    public OffsetDateTime getDate() {
      return date;
    }

    public void setDate(OffsetDateTime date) {
      this.date = date;
    }
  }
}