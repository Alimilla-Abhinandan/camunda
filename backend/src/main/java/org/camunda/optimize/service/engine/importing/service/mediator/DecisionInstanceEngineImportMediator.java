package org.camunda.optimize.service.engine.importing.service.mediator;

import org.apache.commons.collections.ListUtils;
import org.camunda.optimize.dto.engine.HistoricDecisionInstanceDto;
import org.camunda.optimize.rest.engine.EngineContext;
import org.camunda.optimize.service.engine.importing.fetcher.instance.DecisionInstanceFetcher;
import org.camunda.optimize.service.engine.importing.index.handler.impl.DecisionInstanceImportIndexHandler;
import org.camunda.optimize.service.engine.importing.index.page.TimestampBasedImportPage;
import org.camunda.optimize.service.engine.importing.service.DecisionInstanceImportService;
import org.camunda.optimize.service.es.writer.DecisionInstanceWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DecisionInstanceEngineImportMediator extends BackoffImportMediator<DecisionInstanceImportIndexHandler> {

  private DecisionInstanceFetcher decisionInstanceFetcher;
  private DecisionInstanceImportService decisionInstanceImportService;

  @Autowired
  private DecisionInstanceWriter decisionInstanceWriter;

  public DecisionInstanceEngineImportMediator(final EngineContext engineContext) {
    super(engineContext);
  }

  @PostConstruct
  public void init() {
    importIndexHandler = provider.getDecisionInstanceImportIndexHandler(engineContext.getEngineAlias());
    decisionInstanceFetcher = beanHelper.getInstance(DecisionInstanceFetcher.class, engineContext);
    decisionInstanceImportService = new DecisionInstanceImportService(
      decisionInstanceWriter, elasticsearchImportJobExecutor, engineContext
    );
  }

  @Override
  public boolean importNextEnginePage() {
    final List<HistoricDecisionInstanceDto> entitiesOfLastTimestamp = decisionInstanceFetcher
      .fetchHistoricDecisionInstances(importIndexHandler.getTimestampOfLastEntity());

    final TimestampBasedImportPage page = importIndexHandler.getNextPage();
    final List<HistoricDecisionInstanceDto> nextPageEntities = decisionInstanceFetcher.fetchHistoricDecisionInstances(
      page
    );

    if (!nextPageEntities.isEmpty()) {
      OffsetDateTime timestamp = nextPageEntities.get(nextPageEntities.size() - 1).getEvaluationTime();
      importIndexHandler.updateTimestampOfLastEntity(timestamp);
    }

    if (!entitiesOfLastTimestamp.isEmpty() || !nextPageEntities.isEmpty()) {
      final List<HistoricDecisionInstanceDto> allEntities = ListUtils.union(entitiesOfLastTimestamp, nextPageEntities);
      decisionInstanceImportService.executeImport(allEntities);
    }

    return nextPageEntities.size() >= configurationService.getEngineImportDecisionInstanceMaxPageSize();
  }

}
