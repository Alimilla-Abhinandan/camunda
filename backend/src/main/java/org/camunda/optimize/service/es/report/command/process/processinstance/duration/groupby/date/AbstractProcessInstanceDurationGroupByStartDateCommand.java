package org.camunda.optimize.service.es.report.command.process.processinstance.duration.groupby.date;

import org.camunda.optimize.dto.optimize.query.report.single.group.GroupByDateUnit;
import org.camunda.optimize.dto.optimize.query.report.single.process.ProcessReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.StartDateGroupByDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.group.value.StartDateGroupByValueDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.result.ProcessReportMapResultDto;
import org.camunda.optimize.service.es.report.command.process.ProcessReportCommand;
import org.camunda.optimize.service.es.report.command.util.ReportUtil;
import org.camunda.optimize.service.es.schema.type.ProcessInstanceType;
import org.camunda.optimize.service.exceptions.OptimizeException;
import org.camunda.optimize.service.exceptions.OptimizeRuntimeException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.camunda.optimize.service.es.schema.OptimizeIndexNameHelper.getOptimizeIndexAliasForType;
import static org.camunda.optimize.upgrade.es.ElasticsearchConstants.OPTIMIZE_DATE_FORMAT;
import static org.camunda.optimize.upgrade.es.ElasticsearchConstants.PROC_INSTANCE_TYPE;

public abstract class AbstractProcessInstanceDurationGroupByStartDateCommand
  extends ProcessReportCommand<ProcessReportMapResultDto> {

  protected static final String DURATION_AGGREGATION = "durationAggregation";
  private static final String DATE_HISTOGRAM_AGGREGATION = "dateIntervalGrouping";

  @Override
  protected ProcessReportMapResultDto evaluate() throws OptimizeException {

    final ProcessReportDataDto processReportData = getProcessReportData();
    logger.debug(
      "Evaluating process instance duration grouped by start date report " +
        "for process definition key [{}] and version [{}]",
      processReportData.getProcessDefinitionKey(),
      processReportData.getProcessDefinitionVersion()
    );

    BoolQueryBuilder query = setupBaseQuery(
      processReportData.getProcessDefinitionKey(),
      processReportData.getProcessDefinitionVersion()
    );

    queryFilterEnhancer.addFilterToQuery(query, processReportData.getFilter());

    StartDateGroupByValueDto groupByStartDate = ((StartDateGroupByDto) processReportData.getGroupBy()).getValue();

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
      .query(query)
      .fetchSource(false)
      .aggregation(createAggregation(groupByStartDate.getUnit()))
      .size(0);
    SearchRequest searchRequest =
      new SearchRequest(getOptimizeIndexAliasForType(PROC_INSTANCE_TYPE))
        .types(PROC_INSTANCE_TYPE)
        .source(searchSourceBuilder);

    SearchResponse response;
    try {
      response = esClient.search(searchRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      String reason =
        String.format(
          "Could not evaluate process instance duration grouped by start date report " +
            "for process definition key [%s] and version [%s]",
          processReportData.getProcessDefinitionKey(),
          processReportData.getProcessDefinitionVersion()
        );
      logger.error(reason, e);
      throw new OptimizeRuntimeException(reason, e);
    }

    ProcessReportMapResultDto mapResult = new ProcessReportMapResultDto();
    mapResult.setResult(processAggregations(response.getAggregations()));
    mapResult.setProcessInstanceCount(response.getHits().getTotalHits());
    return mapResult;
  }

  private Map<String, Long> processAggregations(Aggregations aggregations) {

    Histogram agg = aggregations.get(DATE_HISTOGRAM_AGGREGATION);

    Map<String, Long> result = new LinkedHashMap<>();
    // For each entry
    for (Histogram.Bucket entry : agg.getBuckets()) {
      DateTime key = (DateTime) entry.getKey();    // Key
      String formattedDate = key.toString(OPTIMIZE_DATE_FORMAT);

      long roundedDuration = processAggregationOperation(entry.getAggregations());
      result.put(formattedDate, roundedDuration);
    }
    return result;
  }

  private AggregationBuilder createAggregation(GroupByDateUnit unit) throws OptimizeException {
    DateHistogramInterval interval = ReportUtil.getDateHistogramInterval(unit);
    return AggregationBuilders
      .dateHistogram(DATE_HISTOGRAM_AGGREGATION)
      .field(ProcessInstanceType.START_DATE)
      .order(BucketOrder.key(false))
      .dateHistogramInterval(interval)
      .subAggregation(
        createAggregationOperation()
      );
  }

  protected abstract long processAggregationOperation(Aggregations aggregations);

  protected abstract AggregationBuilder createAggregationOperation();


}
