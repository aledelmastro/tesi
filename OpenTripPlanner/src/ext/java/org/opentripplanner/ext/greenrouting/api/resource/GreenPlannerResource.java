package org.opentripplanner.ext.greenrouting.api.resource;

import static java.util.Objects.requireNonNullElse;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.glassfish.grizzly.http.server.Request;
import org.opentripplanner.api.common.Message;
import org.opentripplanner.api.common.ParameterException;
import org.opentripplanner.api.resource.PlannerResource;
import org.opentripplanner.api.resource.TripPlannerResponse;
import org.opentripplanner.ext.greenrouting.api.resource.filters.GreenRequest;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("routers/{ignoreRouterId}/plan")
public class GreenPlannerResource extends PlannerResource {

    private static final Logger LOG = LoggerFactory.getLogger(GreenPlannerResource.class);
    private GreenRequest gr = new GreenRequest();
    private List<String> defaultVaribales = List.of("weight");

    @POST
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public TripPlannerResponse plan(
            @Context UriInfo uriInfo, @Context Request grizzlyRequest, GreenRequest gr
    ) {
        this.gr = gr;

        if (gr.getVariables() != null)
            gr.getVariables().addAll(defaultVaribales);

        return plan(uriInfo, grizzlyRequest);
    }

    @Override
    protected RoutingRequest buildRequest(MultivaluedMap<String, String> queryParameters) throws ParameterException {
        var request = super.buildRequest(queryParameters);
        request.filterFeatureDescriptions.clear();
        request.filterScoreDescriptions.clear();
        request.preFilterFeatureDescriptions.clear();
        request.preFilterScoreDescriptions.clear();

        request.filterFeatureDescriptions.addAll(requireNonNullElse(gr.getFeatures(), List.of()));
        request.filterScoreDescriptions.addAll(requireNonNullElse(gr.getScores(), List.of()));
        request.preFilterFeatureDescriptions.addAll(requireNonNullElse(gr.getPreFeatures(), List.of()));
        request.preFilterScoreDescriptions.addAll(requireNonNullElse(gr.getPreScores(), List.of()));

        request.itineraryFilters.booleanParams = request.filterFeatureDescriptions;
        request.itineraryFilters.numberParams = request.filterScoreDescriptions;

        if (gr.getFormula() != null && gr.getVariables() != null) {
            var expression = new ExpressionBuilder(gr.getFormula()).variables(gr.getVariables()).build();
            /*if (expression.validate().isValid()) {*/
                request.expression = expression;
/*            } else {
                throw new ParameterException(Message.BOGUS_PARAMETER); // TODO valutare un errore specifico
            }*/
        }
        else {
            request.expression = null;
        }

        return request;
    }
}
