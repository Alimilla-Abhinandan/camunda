package org.camunda.optimize.test.query.performance;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.collections.ListUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.optimize.dto.engine.ProcessDefinitionEngineDto;
import org.camunda.optimize.dto.optimize.query.report.single.SingleReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.CompletedInstancesOnlyFilterDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.FilterDto;
import org.camunda.optimize.dto.optimize.query.report.single.filter.VariableFilterDto;
import org.camunda.optimize.dto.optimize.query.report.single.processpart.ProcessPartDto;
import org.camunda.optimize.service.es.report.command.util.ReportConstants;
import org.camunda.optimize.test.it.rule.ElasticSearchIntegrationTestRule;
import org.camunda.optimize.test.it.rule.EmbeddedOptimizeRule;
import org.camunda.optimize.test.it.rule.EngineIntegrationRule;
import org.camunda.optimize.test.util.DateUtilHelper;
import org.camunda.optimize.test.util.PropertyUtil;
import org.camunda.optimize.test.util.ReportDataBuilder;
import org.camunda.optimize.test.util.ReportDataType;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.temporal.ChronoUnit.YEARS;
import static org.camunda.optimize.service.es.filter.FilterOperatorConstants.LESS_THAN;
import static org.camunda.optimize.service.es.report.command.util.ReportConstants.DATE_UNIT_WEEK;
import static org.camunda.optimize.service.util.VariableHelper.DOUBLE_TYPE;
import static org.camunda.optimize.test.util.VariableFilterUtilHelper.createBooleanVariableFilter;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

@RunWith(JUnitParamsRunner.class)
public class QueryPerformanceTest {

  private static final Logger logger = LoggerFactory.getLogger(QueryPerformanceTest.class);
  private static final String PROPERTY_LOCATION = "query-performance.properties";
  private static final Properties properties = PropertyUtil.loadProperties(PROPERTY_LOCATION);
  private static final Random randomGen = new Random();

  private static ElasticSearchIntegrationTestRule elasticSearchRule =
    new ElasticSearchIntegrationTestRule(PROPERTY_LOCATION);
  private static EmbeddedOptimizeRule embeddedOptimizeRule = new EmbeddedOptimizeRule();
  private static EngineIntegrationRule engineRule =
    new EngineIntegrationRule(PROPERTY_LOCATION);

  @ClassRule
  public static RuleChain chain = RuleChain
    .outerRule(elasticSearchRule).around(embeddedOptimizeRule).around(engineRule);

  @BeforeClass
  public static void init() throws TimeoutException, InterruptedException {
    // given
    importEngineData();
  }

  private static List<SingleReportDataDto> createAllPossibleReports() {
    List<ProcessDefinitionEngineDto> latestDefinitionVersions =
      engineRule.getLatestProcessDefinitions();

    return latestDefinitionVersions
      .stream()
      .map(QueryPerformanceTest::createReportsFromDefinition)
      .reduce(ListUtils::union)
      .orElse(Collections.emptyList());

  }

  private static List<SingleReportDataDto> createReportsFromDefinition(ProcessDefinitionEngineDto definition) {
    String variableName = "doubleVar";
    List<SingleReportDataDto> reports = new ArrayList<>();
    ProcessPartDto processPart = createProcessPart(definition);
    for (ReportDataType reportDataType : ReportDataType.values()) {
      ReportDataBuilder reportDataBuilder = ReportDataBuilder.createReportData()
        .setReportDataType(reportDataType)
        .setProcessDefinitionKey(definition.getKey())
        .setProcessDefinitionVersion(definition.getVersionAsString())
        .setVariableName(variableName)
        .setVariableType(DOUBLE_TYPE)
        .setDateInterval(DATE_UNIT_WEEK)
        .setStartFlowNodeId(processPart.getStart())
        .setEndFlowNodeId(processPart.getEnd())
        .setFilter(createFilter());

      SingleReportDataDto reportDataLatestDefinitionVersion =
        reportDataBuilder.build();
      reports.add(reportDataLatestDefinitionVersion);
      reportDataBuilder.setProcessDefinitionVersion(ReportConstants.ALL_VERSIONS);
      SingleReportDataDto reportDataAllDefinitionVersions = reportDataBuilder.build();
      reports.add(reportDataAllDefinitionVersions);
    }
    return reports;
  }

  private static ProcessPartDto createProcessPart(ProcessDefinitionEngineDto definition) {
    String xml = engineRule
      .getProcessDefinitionXml(definition.getId())
      .getBpmn20Xml();
    ModelInstance model = Bpmn.readModelFromStream(new ByteArrayInputStream(xml.getBytes()));
    String startFlowNodeId = model.getModelElementsByType(StartEvent.class).stream().findFirst().get().getId();
    String endFlowNodeId = model.getModelElementsByType(EndEvent.class).stream().findFirst().get().getId();
    ProcessPartDto processPart = new ProcessPartDto();
    processPart.setStart(startFlowNodeId);
    processPart.setEnd(endFlowNodeId);
    return processPart;
  }

  private static List<FilterDto> createFilter() {
    List<FilterDto> filterList = new ArrayList<>();

    VariableFilterDto booleanVariableFilter =
      createBooleanVariableFilter("boolVar", String.valueOf(randomGen.nextBoolean()));
    filterList.add(booleanVariableFilter);
    filterList.addAll(DateUtilHelper.createFixedStartDateFilter(null, OffsetDateTime.now().minusYears(100L)));
    filterList.addAll(DateUtilHelper.createFixedEndDateFilter(null, OffsetDateTime.now().plusYears(100L)));
    filterList.add(new CompletedInstancesOnlyFilterDto());
    filterList.addAll(DateUtilHelper.createDurationFilter(LESS_THAN, 100, YEARS.toString()));
    return filterList;
  }

  private static void importEngineData() throws InterruptedException, TimeoutException {
    logger.info("Start importing engine data...");
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.execute(
      () -> embeddedOptimizeRule.scheduleAllJobsAndImportEngineEntities()
    );

    executor.shutdown();
    boolean wasAbleToFinishImportInTime =
      executor.awaitTermination(getImportTimeout(), TimeUnit.HOURS);
    if (!wasAbleToFinishImportInTime) {
      throw new TimeoutException("Import was not able to finish import in " + 2 + " hours!");
    }
    elasticSearchRule.refreshOptimizeIndexInElasticsearch();
    logger.info("Finished importing engine data...");
  }

  private static long getImportTimeout() {
    String timeoutAsString =
      properties.getProperty("camunda.optimize.test.import.timeout.in.hours", "2");
    return Long.parseLong(timeoutAsString);
  }

  @Test
  @Parameters(source = ReportDataProvider.class)
  public void testQueryPerformance(SingleReportDataDto report) {
    // given the report to evaluate

    // when
    long timeElapsed = evaluateReportAndReturnEvaluationTime(report);

    // then
    assertThat(timeElapsed, lessThan(getMaxAllowedQueryTime()));
  }

  private long getMaxAllowedQueryTime() {
    String timeoutAsString =
      properties.getProperty("camunda.optimize.test.import.max.query.time.in.ms", "5000");
    return Long.parseLong(timeoutAsString);
  }

  private long evaluateReportAndReturnEvaluationTime(SingleReportDataDto report) {
    logger.info("Evaluating report {}", report);
    Instant start = Instant.now();
    Response response = embeddedOptimizeRule
      .getRequestExecutor()
      .buildEvaluateSingleUnsavedReportRequest(report)
      .execute();
    assertThat(response.getStatus(), is(200));
    Instant finish = Instant.now();

    long timeElapsed = Duration.between(start, finish).toMillis();
    logger.info("Evaluation of report took {} milliseconds", timeElapsed);
    return timeElapsed;
  }

  public static class ReportDataProvider {
    public static Object[] provideReportData() {
      List<SingleReportDataDto> allReports = createAllPossibleReports();
      return allReports.toArray();
    }
  }
}
