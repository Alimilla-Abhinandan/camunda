package org.camunda.optimize.service.engine.importing.service.mediator;

import org.apache.commons.collections.ListUtils;
import org.camunda.optimize.dto.engine.HistoricActivityInstanceEngineDto;
import org.camunda.optimize.rest.engine.EngineContext;
import org.camunda.optimize.service.engine.importing.fetcher.instance.ActivityInstanceFetcher;
import org.camunda.optimize.service.engine.importing.index.handler.impl.ActivityImportIndexHandler;
import org.camunda.optimize.service.engine.importing.index.page.TimestampBasedImportPage;
import org.camunda.optimize.service.engine.importing.service.ActivityInstanceImportService;
import org.camunda.optimize.service.es.writer.EventsWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.util.List;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ActivityInstanceEngineImportMediator
  extends BackoffImportMediator<ActivityImportIndexHandler> {

  private ActivityInstanceFetcher engineEntityFetcher;

  @Autowired
  private EventsWriter eventsWriter;

  private ActivityInstanceImportService activityInstanceImportService;


  public ActivityInstanceEngineImportMediator(EngineContext engineContext) {
    super(engineContext);
  }

  @PostConstruct
  public void init() {
    importIndexHandler = provider.getActivityImportIndexHandler(engineContext.getEngineAlias());
    engineEntityFetcher = beanHelper.getInstance(ActivityInstanceFetcher.class, engineContext);
    activityInstanceImportService = new ActivityInstanceImportService(
      eventsWriter, elasticsearchImportJobExecutor, engineContext
    );
  }

  @Override
  public boolean importNextEnginePage() {
    final List<HistoricActivityInstanceEngineDto> entitiesOfLastTimestamp = engineEntityFetcher
      .fetchHistoricActivityInstancesForTimestamp(importIndexHandler.getTimestampOfLastEntity());

    final TimestampBasedImportPage page = importIndexHandler.getNextPage();
    final List<HistoricActivityInstanceEngineDto> nextPageEntities = engineEntityFetcher
      .fetchHistoricActivityInstances(page);

    if (!nextPageEntities.isEmpty()) {
      OffsetDateTime timestamp = nextPageEntities.get(nextPageEntities.size() - 1).getEndTime();
      importIndexHandler.updateTimestampOfLastEntity(timestamp);
    }

    if (!entitiesOfLastTimestamp.isEmpty() || !nextPageEntities.isEmpty()) {
      final List<HistoricActivityInstanceEngineDto> allEntities =
        ListUtils.union(entitiesOfLastTimestamp, nextPageEntities);
      activityInstanceImportService.executeImport(allEntities);
    }

    return nextPageEntities.size() >= configurationService.getEngineImportActivityInstanceMaxPageSize();
  }
}
