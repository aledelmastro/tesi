package org.opentripplanner.ext.greenrouting.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.glassfish.grizzly.http.server.Request;
import org.opentripplanner.api.common.ParameterException;
import org.opentripplanner.api.resource.PlannerResource;
import org.opentripplanner.api.resource.TripPlannerResponse;
import org.opentripplanner.ext.greenrouting.api.resource.filters.GreenFilterRequest;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("routers/{ignoreRouterId}/plan")
public class GreenPlannerResource extends PlannerResource {

    private static final Logger LOG = LoggerFactory.getLogger(GreenPlannerResource.class);
    private GreenFilterRequest greenFilterRequest = new GreenFilterRequest();

    @PUT
    @Consumes("application/json")
    @Produces(MediaType.APPLICATION_JSON)
    public TripPlannerResponse plan(
            @Context UriInfo uriInfo, @Context Request grizzlyRequest, GreenFilterRequest gfr
    ) {
        this.greenFilterRequest = gfr;
        return plan(uriInfo, grizzlyRequest);
    }

    @Override
    protected RoutingRequest buildRequest(MultivaluedMap<String, String> queryParameters) throws ParameterException {
        var request = super.buildRequest(queryParameters);
        request.filterFeatureDescriptions.clear();
        request.filterScoreDescriptions.clear();
        request.filterFeatureDescriptions.addAll(greenFilterRequest.getFeatures());
        request.filterScoreDescriptions.addAll(greenFilterRequest.getScores());

        request.itineraryFilters.booleanParams = request.filterFeatureDescriptions;
        request.itineraryFilters.numberParams = request.filterScoreDescriptions;

        return request;
    }
}
