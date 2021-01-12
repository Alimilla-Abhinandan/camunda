/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.es.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.camunda.optimize.dto.optimize.query.IdResponseDto;
import org.camunda.optimize.dto.optimize.query.report.ReportDefinitionUpdateDto;
import org.camunda.optimize.dto.optimize.query.report.combined.CombinedReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.combined.CombinedReportDefinitionRequestDto;
import org.camunda.optimize.dto.optimize.query.report.single.decision.DecisionReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.decision.SingleDecisionReportDefinitionRequestDto;
import org.camunda.optimize.dto.optimize.query.report.single.decision.SingleDecisionReportDefinitionUpdateDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.ProcessReportDataDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.SingleProcessReportDefinitionRequestDto;
import org.camunda.optimize.dto.optimize.query.report.single.process.SingleProcessReportDefinitionUpdateDto;
import org.camunda.optimize.service.es.OptimizeElasticsearchClient;
import org.camunda.optimize.service.exceptions.OptimizeRuntimeException;
import org.camunda.optimize.service.security.util.LocalDateUtil;
import org.camunda.optimize.service.util.IdGenerator;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.engine.DocumentMissingException;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;

import static org.camunda.optimize.service.es.schema.index.report.AbstractReportIndex.COLLECTION_ID;
import static org.camunda.optimize.service.es.schema.index.report.AbstractReportIndex.COMBINED;
import static org.camunda.optimize.service.es.schema.index.report.AbstractReportIndex.CREATED;
import static org.camunda.optimize.service.es.schema.index.report.AbstractReportIndex.LAST_MODIFIED;
import static org.camunda.optimize.service.es.schema.index.report.AbstractReportIndex.LAST_MODIFIER;
import static org.camunda.optimize.service.es.schema.index.report.AbstractReportIndex.NAME;
import static org.camunda.optimize.service.es.schema.index.report.AbstractReportIndex.OWNER;
import static org.camunda.optimize.service.es.schema.index.report.AbstractReportIndex.REPORT_TYPE;
import static org.camunda.optimize.service.es.schema.index.report.CombinedReportIndex.DATA;
import static org.camunda.optimize.service.es.schema.index.report.CombinedReportIndex.REPORTS;
import static org.camunda.optimize.service.es.schema.index.report.CombinedReportIndex.REPORT_ITEM_ID;
import static org.camunda.optimize.upgrade.es.ElasticsearchConstants.COMBINED_REPORT_INDEX_NAME;
import static org.camunda.optimize.upgrade.es.ElasticsearchConstants.NUMBER_OF_RETRIES_ON_CONFLICT;
import static org.camunda.optimize.upgrade.es.ElasticsearchConstants.SINGLE_DECISION_REPORT_INDEX_NAME;
import static org.camunda.optimize.upgrade.es.ElasticsearchConstants.SINGLE_PROCESS_REPORT_INDEX_NAME;
import static org.elasticsearch.action.support.WriteRequest.RefreshPolicy.IMMEDIATE;
import static org.elasticsearch.index.query.QueryBuilders.idsQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

@AllArgsConstructor
@Component
@Slf4j
public class ReportWriter {
  private static final Set<String> UPDATABLE_FIELDS = ImmutableSet.of(
    NAME, DATA, LAST_MODIFIED, LAST_MODIFIER, CREATED, OWNER, COLLECTION_ID, COMBINED, REPORT_TYPE
  );
  private static final String PROCESS_DEFINITION_PROPERTY = String.join(
    ".", DATA, ProcessReportDataDto.Fields.processDefinitionKey
  );

  private final ObjectMapper objectMapper;
  private final OptimizeElasticsearchClient esClient;

  public IdResponseDto createNewCombinedReport(@NonNull final String userId,
                                               @NonNull final CombinedReportDataDto reportData,
                                               @NonNull final String reportName,
                                               final String collectionId) {
    log.debug("Writing new combined report to Elasticsearch");
    final String id = IdGenerator.getNextId();
    final CombinedReportDefinitionRequestDto reportDefinitionDto = new CombinedReportDefinitionRequestDto();
    reportDefinitionDto.setId(id);
    final OffsetDateTime now = LocalDateUtil.getCurrentDateTime();
    reportDefinitionDto.setCreated(now);
    reportDefinitionDto.setLastModified(now);
    reportDefinitionDto.setOwner(userId);
    reportDefinitionDto.setLastModifier(userId);
    reportDefinitionDto.setName(reportName);
    reportDefinitionDto.setData(reportData);
    reportDefinitionDto.setCollectionId(collectionId);

    try {
      IndexRequest request = new IndexRequest(COMBINED_REPORT_INDEX_NAME)
        .id(id)
        .source(objectMapper.writeValueAsString(reportDefinitionDto), XContentType.JSON)
        .setRefreshPolicy(IMMEDIATE);

      IndexResponse indexResponse = esClient.index(request, RequestOptions.DEFAULT);

      if (!indexResponse.getResult().equals(IndexResponse.Result.CREATED)) {
        String message = "Could not write report to Elasticsearch. ";
        log.error(message);
        throw new OptimizeRuntimeException(message);
      }

      log.debug("Report with id [{}] has successfully been created.", id);
      return new IdResponseDto(id);
    } catch (IOException e) {
      String errorMessage = "Was not able to insert combined report.!";
      log.error(errorMessage, e);
      throw new OptimizeRuntimeException(errorMessage, e);
    }
  }


  public IdResponseDto createNewSingleProcessReport(@NonNull final String userId,
                                                    @NonNull final ProcessReportDataDto reportData,
                                                    @NonNull final String reportName,
                                                    final String collectionId) {
    log.debug("Writing new single report to Elasticsearch");

    final String id = IdGenerator.getNextId();
    final SingleProcessReportDefinitionRequestDto reportDefinitionDto = new SingleProcessReportDefinitionRequestDto();
    reportDefinitionDto.setId(id);
    final OffsetDateTime now = LocalDateUtil.getCurrentDateTime();
    reportDefinitionDto.setCreated(now);
    reportDefinitionDto.setLastModified(now);
    reportDefinitionDto.setOwner(userId);
    reportDefinitionDto.setLastModifier(userId);
    reportDefinitionDto.setName(reportName);
    reportDefinitionDto.setData(reportData);
    reportDefinitionDto.setCollectionId(collectionId);

    try {
      IndexRequest request = new IndexRequest(SINGLE_PROCESS_REPORT_INDEX_NAME)
        .id(id)
        .source(objectMapper.writeValueAsString(reportDefinitionDto), XContentType.JSON)
        .setRefreshPolicy(IMMEDIATE);

      IndexResponse indexResponse = esClient.index(request, RequestOptions.DEFAULT);

      if (!indexResponse.getResult().equals(IndexResponse.Result.CREATED)) {
        String message = "Could not write single process report to Elasticsearch.";
        log.error(message);
        throw new OptimizeRuntimeException(message);
      }

      log.debug("Single process report with id [{}] has successfully been created.", id);
      return new IdResponseDto(id);
    } catch (IOException e) {
      String errorMessage = "Was not able to insert single process report.";
      log.error(errorMessage, e);
      throw new OptimizeRuntimeException(errorMessage, e);
    }
  }

  public IdResponseDto createNewSingleDecisionReport(@NonNull final String userId,
                                                     @NonNull final DecisionReportDataDto reportData,
                                                     @NonNull final String reportName,
                                                     final String collectionId) {
    log.debug("Writing new single report to Elasticsearch");

    final String id = IdGenerator.getNextId();
    final SingleDecisionReportDefinitionRequestDto reportDefinitionDto = new SingleDecisionReportDefinitionRequestDto();
    reportDefinitionDto.setId(id);
    final OffsetDateTime now = LocalDateUtil.getCurrentDateTime();
    reportDefinitionDto.setCreated(now);
    reportDefinitionDto.setLastModified(now);
    reportDefinitionDto.setOwner(userId);
    reportDefinitionDto.setLastModifier(userId);
    reportDefinitionDto.setName(reportName);
    reportDefinitionDto.setData(reportData);
    reportDefinitionDto.setCollectionId(collectionId);

    try {
      IndexRequest request = new IndexRequest(SINGLE_DECISION_REPORT_INDEX_NAME)
        .id(id)
        .source(objectMapper.writeValueAsString(reportDefinitionDto), XContentType.JSON)
        .setRefreshPolicy(IMMEDIATE);

      IndexResponse indexResponse = esClient.index(request, RequestOptions.DEFAULT);

      if (!indexResponse.getResult().equals(IndexResponse.Result.CREATED)) {
        String message = "Could not write single decision report to Elasticsearch.";
        log.error(message);
        throw new OptimizeRuntimeException(message);
      }

      log.debug("Single decision report with id [{}] has successfully been created.", id);
      return new IdResponseDto(id);
    } catch (IOException e) {
      String errorMessage = "Was not able to insert single decision report.";
      log.error(errorMessage, e);
      throw new OptimizeRuntimeException(errorMessage, e);
    }
  }

  public void updateSingleProcessReport(final SingleProcessReportDefinitionUpdateDto reportUpdate) {
    updateReport(reportUpdate, SINGLE_PROCESS_REPORT_INDEX_NAME);
  }

  public void updateSingleDecisionReport(final SingleDecisionReportDefinitionUpdateDto reportUpdate) {
    updateReport(reportUpdate, SINGLE_DECISION_REPORT_INDEX_NAME);
  }

  public void updateCombinedReport(final ReportDefinitionUpdateDto updatedReport) {
    updateReport(updatedReport, COMBINED_REPORT_INDEX_NAME);
  }

  public void updateProcessDefinitionXmlForProcessReportsWithKey(final String definitionKey,
                                                                 final String definitionXml) {
    final String updateItem = String.format("reports with definitionKey [%s]", definitionKey);
    log.debug("Updating definition XML in {} in Elasticsearch", updateItem);

    final Script updateDefinitionXmlScript = new Script(
      ScriptType.INLINE,
      Script.DEFAULT_SCRIPT_LANG,
      // this script is deliberately not updating the modified date as this is no user operation
      "ctx._source.data.configuration.xml = params.newXml;",
      Collections.singletonMap("newXml", definitionXml)
    );

    ElasticsearchWriterUtil.tryUpdateByQueryRequest(
      esClient,
      updateItem,
      updateDefinitionXmlScript,
      termQuery(PROCESS_DEFINITION_PROPERTY, definitionKey),
      SINGLE_PROCESS_REPORT_INDEX_NAME
    );
  }

  private void updateReport(ReportDefinitionUpdateDto updatedReport, String indexName) {
    log.debug("Updating report with id [{}] in Elasticsearch", updatedReport.getId());
    try {
      Script updateScript = ElasticsearchWriterUtil.createFieldUpdateScript(
        UPDATABLE_FIELDS,
        updatedReport,
        objectMapper
      );
      final UpdateRequest request =
        new UpdateRequest()
          .index(indexName)
          .id(updatedReport.getId())
          .script(updateScript)
          .setRefreshPolicy(IMMEDIATE)
          .retryOnConflict(NUMBER_OF_RETRIES_ON_CONFLICT);

      final UpdateResponse updateResponse = esClient.update(request, RequestOptions.DEFAULT);
      if (updateResponse.getShardInfo().getFailed() > 0) {
        log.error(
          "Was not able to update report with id [{}] and name [{}].", updatedReport.getId(), updatedReport.getName()
        );
        throw new OptimizeRuntimeException("Was not able to update collection!");
      }
    } catch (IOException e) {
      String errorMessage = String.format(
        "Was not able to update report with id [%s].", updatedReport.getId()
      );
      log.error(errorMessage, e);
      throw new OptimizeRuntimeException(errorMessage, e);
    } catch (DocumentMissingException e) {
      String errorMessage = String.format(
        "Was not able to update report with id [%s] and name [%s]. Report does not exist!",
        updatedReport.getId(),
        updatedReport.getName()
      );
      log.error(errorMessage, e);
      throw new NotFoundException(errorMessage, e);
    }
  }

  public void deleteSingleReport(final String reportId) {
    ElasticsearchWriterUtil.tryDeleteByQueryRequest(
      esClient,
      idsQuery().addIds(reportId),
      String.format("single report with ID [%s]", reportId),
      true,
      SINGLE_PROCESS_REPORT_INDEX_NAME,
      SINGLE_DECISION_REPORT_INDEX_NAME
    );
  }

  public void removeSingleReportFromCombinedReports(final String reportId) {
    String updateItemName = String.format("report with ID [%s]", reportId);
    log.info("Removing {} from combined report.", updateItemName);

    Script removeReportIdFromCombinedReportsScript = new Script(
      ScriptType.INLINE,
      Script.DEFAULT_SCRIPT_LANG,
      "def reports = ctx._source.data.reports;" +
        "if(reports != null) {" +
        "  reports.removeIf(r -> r.id.equals(params.idToRemove));" +
        "}",
      Collections.singletonMap("idToRemove", reportId)
    );

    NestedQueryBuilder query = nestedQuery(
      DATA,
      nestedQuery(
        String.join(".", DATA, REPORTS),
        termQuery(String.join(".", DATA, REPORTS, REPORT_ITEM_ID), reportId),
        ScoreMode.None
      ),
      ScoreMode.None
    );

    ElasticsearchWriterUtil.tryUpdateByQueryRequest(
      esClient,
      updateItemName,
      removeReportIdFromCombinedReportsScript,
      query,
      COMBINED_REPORT_INDEX_NAME
    );
  }

  public void deleteCombinedReport(final String reportId) {
    log.debug("Deleting combined report with id [{}]", reportId);

    DeleteRequest request = new DeleteRequest(COMBINED_REPORT_INDEX_NAME)
      .id(reportId)
      .setRefreshPolicy(IMMEDIATE);

    DeleteResponse deleteResponse;
    try {
      deleteResponse = esClient.delete(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
      String reason =
        String.format("Could not delete combined report with id [%s].", reportId);
      log.error(reason, e);
      throw new OptimizeRuntimeException(reason, e);
    }

    if (!deleteResponse.getResult().equals(DeleteResponse.Result.DELETED)) {
      String message =
        String.format("Could not delete combined process report with id [%s]. " +
                        "Combined process report does not exist." +
                        "Maybe it was already deleted by someone else?", reportId);
      log.error(message);
      throw new NotFoundException(message);
    }
  }

  public void deleteAllReportsOfCollection(String collectionId) {
    ElasticsearchWriterUtil.tryDeleteByQueryRequest(
      esClient,
      QueryBuilders.termQuery(COLLECTION_ID, collectionId),
      String.format("all reports of collection with collectionId [%s]", collectionId),
      true,
      COMBINED_REPORT_INDEX_NAME,
      SINGLE_PROCESS_REPORT_INDEX_NAME,
      SINGLE_DECISION_REPORT_INDEX_NAME
    );
  }
}
