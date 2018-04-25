package org.camunda.optimize.service.engine.importing.service;

import org.camunda.optimize.dto.engine.HistoricProcessInstanceDto;
import org.camunda.optimize.dto.optimize.importing.ProcessInstanceDto;
import org.camunda.optimize.rest.engine.EngineContext;
import org.camunda.optimize.service.engine.importing.diff.MissingEntitiesFinder;
import org.camunda.optimize.service.es.ElasticsearchImportJobExecutor;
import org.camunda.optimize.service.es.job.ElasticsearchImportJob;
import org.camunda.optimize.service.es.job.importing.RunningProcessInstanceElasticsearchImportJob;
import org.camunda.optimize.service.es.writer.RunningProcessInstanceWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class RunningProcessInstanceImportService {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected ElasticsearchImportJobExecutor elasticsearchImportJobExecutor;
  private MissingEntitiesFinder<HistoricProcessInstanceDto> missingProcessInstanceFinder;
  protected EngineContext engineContext;
  private RunningProcessInstanceWriter runningProcessInstanceWriter;

  public RunningProcessInstanceImportService(
      RunningProcessInstanceWriter runningProcessInstanceWriter,
      ElasticsearchImportJobExecutor elasticsearchImportJobExecutor,
      MissingEntitiesFinder<HistoricProcessInstanceDto> missingProcessInstanceFinder,
      EngineContext engineContext
  ) {
    this.elasticsearchImportJobExecutor = elasticsearchImportJobExecutor;
    this.missingProcessInstanceFinder = missingProcessInstanceFinder;
    this.engineContext = engineContext;
    this.runningProcessInstanceWriter = runningProcessInstanceWriter;
  }

  public void executeImport(List<HistoricProcessInstanceDto> pageOfEngineEntities) {
    logger.trace("Importing entities from engine...");

    List<HistoricProcessInstanceDto> newEngineEntities =
          missingProcessInstanceFinder.retrieveMissingEntities(pageOfEngineEntities);
    boolean newDataIsAvailable = !newEngineEntities.isEmpty();
    if (newDataIsAvailable) {
      List<ProcessInstanceDto> newOptimizeEntities = mapEngineEntitiesToOptimizeEntities(newEngineEntities);
      ElasticsearchImportJob<ProcessInstanceDto> elasticsearchImportJob =
        createElasticsearchImportJob(newOptimizeEntities);
      addElasticsearchImportJobToQueue(elasticsearchImportJob);
    }
  }

  private void addElasticsearchImportJobToQueue(ElasticsearchImportJob elasticsearchImportJob) {
    try {
      elasticsearchImportJobExecutor.executeImportJob(elasticsearchImportJob);
    } catch (InterruptedException e) {
      logger.error("Was interrupted while trying to add new job to Elasticsearch import queue.", e);
    }
  }

  private List<ProcessInstanceDto> mapEngineEntitiesToOptimizeEntities(List<HistoricProcessInstanceDto> engineEntities) {
    return engineEntities
      .stream().map(this::mapEngineEntityToOptimizeEntity)
      .collect(Collectors.toList());
  }

  private ElasticsearchImportJob<ProcessInstanceDto> createElasticsearchImportJob(List<ProcessInstanceDto> processInstances) {
    RunningProcessInstanceElasticsearchImportJob importJob =
        new RunningProcessInstanceElasticsearchImportJob(runningProcessInstanceWriter);
    importJob.setEntitiesToImport(processInstances);
    return importJob;
  }

  private ProcessInstanceDto mapEngineEntityToOptimizeEntity(HistoricProcessInstanceDto engineEntity) {
    ProcessInstanceDto processInstanceDto = new ProcessInstanceDto();
    processInstanceDto.setProcessDefinitionKey(engineEntity.getProcessDefinitionKey());
    processInstanceDto.setProcessDefinitionVersion(engineEntity.getProcessDefinitionVersionAsString());
    processInstanceDto.setProcessDefinitionId(engineEntity.getProcessDefinitionId());
    processInstanceDto.setProcessInstanceId(engineEntity.getId());
    processInstanceDto.setStartDate(engineEntity.getStartTime());
    processInstanceDto.setEndDate(null);
    processInstanceDto.setEngine(engineContext.getEngineAlias());
    return processInstanceDto;
  }

}
