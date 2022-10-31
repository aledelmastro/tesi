package org.opentripplanner.routing.algorithm.filterchain.deletionflagger.sustain;

import java.util.function.Predicate;

import org.opentripplanner.model.plan.Itinerary;
import org.opentripplanner.model.plan.Leg;
import org.opentripplanner.routing.algorithm.filterchain.deletionflagger.ItineraryDeletionFlagger;
import org.opentripplanner.routing.core.TraverseMode;

/**
 * Il filtro flagga i percorsi in cui l'automobile viene utilizzata
 * per un periodo di tempo superiore a una certa percentuale del percorso totale.
 * @author https://github.com/aledelmastro
 */
public class MaxCarUsagePercFilter implements ItineraryDeletionFlagger {
    public static final String TAG = "car-usage-excedes-the-allowed-percentage";

    private final double limit;

    public MaxCarUsagePercFilter(double limitInPercentage) {
        this.limit = limitInPercentage;
    }

    @Override
    public String name() {
        return TAG;
    }

    @Override
    public Predicate<Itinerary> predicate() {
        return itinerary -> {
            double carDuration = itinerary.legs
                .stream()
                .filter(l -> l.getMode() == TraverseMode.CAR)
                .mapToDouble(Leg::getDuration)
                .sum();

            double totalDuration = itinerary.legs
                .stream()
                .mapToDouble(Leg::getDuration)
                .sum();

            return (carDuration / totalDuration) > limit;
        };
    }
}
