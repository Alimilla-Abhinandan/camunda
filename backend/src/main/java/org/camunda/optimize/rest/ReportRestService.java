package org.camunda.optimize.rest;

import org.camunda.optimize.dto.optimize.query.report.IdDto;
import org.camunda.optimize.dto.optimize.query.report.ReportDefinitionDto;
import org.camunda.optimize.rest.providers.Secured;
import org.camunda.optimize.rest.util.AuthenticationUtil;
import org.camunda.optimize.service.es.reader.ReportReader;
import org.camunda.optimize.service.es.writer.ReportWriter;
import org.camunda.optimize.service.exceptions.OptimizeException;
import org.camunda.optimize.service.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static org.camunda.optimize.rest.util.RestResponseUtil.buildServerErrorResponse;

@Secured
@Path("/report")
@Component
public class ReportRestService {

  @Autowired
  private ReportWriter reportWriter;

  @Autowired
  private ReportReader reportReader;

  @Autowired
  private TokenService tokenService;

  /**
   * Creates an empty report.
   * @return the id of the report
   * @throws OptimizeException if it wasn't possible to create the report.
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public IdDto createNewReport(@Context ContainerRequestContext requestContext) {
    String token = AuthenticationUtil.getToken(requestContext);
    String userId = tokenService.getTokenIssuer(token);
    return reportWriter.createNewReportAndReturnId(userId);
  }

  /**
   * Updates the given fields of a report to the given id.
   * @param reportId the id of the report
   * @param updatedReport report that needs to be updated. Only the fields that are defined here are actually updated.
   */
  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateReport(@Context ContainerRequestContext requestContext,
                           @PathParam("id") String reportId,
                           ReportDefinitionDto updatedReport) {
    updatedReport.setId(reportId);
    String token = AuthenticationUtil.getToken(requestContext);
    String userId = tokenService.getTokenIssuer(token);
    updatedReport.setLastModifier(userId);
    reportWriter.updateReport(updatedReport);
  }

  /**
   * Get a list of all available reports.
   * @throws IOException If there was a problem retrieving the reports from Elasticsearch.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ReportDefinitionDto> getStoredReports() throws IOException {
    return reportReader.getAllReport();
  }

  /**
   * Retrieve the report to the specified id.
   */
  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getReport(@PathParam("id") String reportId) throws IOException, OptimizeException {
    try {
      return Response.ok(reportReader.getReport(reportId), MediaType.APPLICATION_JSON).build();
    } catch (Exception e) {
      return buildServerErrorResponse(e);
    }
  }

  /**
   * Delete the report to the specified id.
   */
  @DELETE
  @Path("/{id}")
  public void deleteReport(@PathParam("id") String reportId) {
    reportWriter.deleteReport(reportId);
  }


}
