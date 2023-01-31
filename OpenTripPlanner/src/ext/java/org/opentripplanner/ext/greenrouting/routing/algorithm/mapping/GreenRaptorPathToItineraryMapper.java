package org.opentripplanner.ext.greenrouting.routing.algorithm.mapping;

import static org.opentripplanner.routing.algorithm.raptoradapter.transit.cost.RaptorCostConverter.toOtpDomainCost;

import java.time.ZonedDateTime;
import org.opentripplanner.ext.greenrouting.model.plan.GreenFrequencyTransitLeg;
import org.opentripplanner.ext.greenrouting.model.plan.GreenScheduledTransitLeg;
import org.opentripplanner.model.plan.Leg;
import org.opentripplanner.model.transfer.ConstrainedTransfer;
import org.opentripplanner.routing.algorithm.mapping.AlertToLegMapper;
import org.opentripplanner.routing.algorithm.mapping.RaptorPathToItineraryMapper;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TransitLayer;
import org.opentripplanner.routing.algorithm.raptoradapter.transit.TripSchedule;
import org.opentripplanner.routing.api.request.RoutingRequest;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.transit.raptor.api.path.TransitPathLeg;

/*
public class GreenRaptorPathToItineraryMapper extends RaptorPathToItineraryMapper {

    */
/**
     * Constructs an itinerary mapper for a request and a set of results
     *
     * @param graph
     * @param transitLayer          the currently active transit layer (may have real-time data
     *                              applied)
     * @param transitSearchTimeZero the point in time all times in seconds are counted from
     * @param request               the current routing request
     *//*

    public GreenRaptorPathToItineraryMapper(
            Graph graph,
            TransitLayer transitLayer,
            ZonedDateTime transitSearchTimeZero,
            RoutingRequest request
    ) {
        super(graph, transitLayer, transitSearchTimeZero, request);
    }

    private Leg mapTransitLeg(
            Leg prevTransitLeg,
            TransitPathLeg<TripSchedule> pathLeg,
            boolean firstLeg
    ) {
        TripSchedule tripSchedule = pathLeg.trip();

        // Find stop positions in pattern where this leg boards and alights.
        // We cannot assume every stop appears only once in a pattern, so we
        // have to match stop and time.
        int boardStopIndexInPattern = tripSchedule.findDepartureStopPosition(
                pathLeg.fromTime(), pathLeg.fromStop()
        );
        int alightStopIndexInPattern = tripSchedule.findArrivalStopPosition(
                pathLeg.toTime(), pathLeg.toStop()
        );

        Leg leg;
        if (tripSchedule.isFrequencyBasedTrip()) {
            int frequencyHeadwayInSeconds = tripSchedule.frequencyHeadwayInSeconds();
            leg = new GreenFrequencyTransitLeg(
                    tripSchedule.getOriginalTripTimes(),
                    tripSchedule.getOriginalTripPattern(),
                    boardStopIndexInPattern,
                    alightStopIndexInPattern,
                    createCalendar(pathLeg.fromTime() + frequencyHeadwayInSeconds),
                    createCalendar(pathLeg.toTime()),
                    tripSchedule.getServiceDate(),
                    transitSearchTimeZero.getZone().normalized(),
                    (prevTransitLeg == null ? null : prevTransitLeg.getTransferToNextLeg()),
                    (ConstrainedTransfer) pathLeg.getConstrainedTransferAfterLeg(),
                    toOtpDomainCost(pathLeg.generalizedCost()),
                    frequencyHeadwayInSeconds
            );
        } else {
            leg = new GreenScheduledTransitLeg(
                    tripSchedule.getOriginalTripTimes(),
                    tripSchedule.getOriginalTripPattern(),
                    boardStopIndexInPattern,
                    alightStopIndexInPattern,
                    createCalendar(pathLeg.fromTime()),
                    createCalendar(pathLeg.toTime()),
                    tripSchedule.getServiceDate(),
                    transitSearchTimeZero.getZone().normalized(),
                    (prevTransitLeg == null ? null : prevTransitLeg.getTransferToNextLeg()),
                    (ConstrainedTransfer) pathLeg.getConstrainedTransferAfterLeg(),
                    toOtpDomainCost(pathLeg.generalizedCost())
            );
        }

        AlertToLegMapper.addTransitAlertPatchesToLeg(
                graph,
                leg,
                firstLeg
        );

        return leg;
    }

}
*/
