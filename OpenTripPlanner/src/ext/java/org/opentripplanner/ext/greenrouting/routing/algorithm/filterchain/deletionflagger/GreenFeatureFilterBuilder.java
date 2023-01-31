package org.opentripplanner.ext.greenrouting.routing.algorithm.filterchain.deletionflagger;

import org.opentripplanner.ext.greenrouting.api.resource.filters.FeatureDescription;
import org.opentripplanner.ext.greenrouting.api.resource.filters.ScoreDescription;

public class GreenFeatureFilterBuilder {
    private final GreenFeatureFilter gff;

    public GreenFeatureFilterBuilder() {
        this.gff = new GreenFeatureFilter();
    }

    public GreenFeatureFilterBuilder withNumberFilter(ScoreDescription scoreDescription) {
        if (scoreDescription.isBelow())
            this.gff.addNumFilter(new BelowTheThresholdFilter<>(m -> m.get(scoreDescription.getName()), scoreDescription.getThreshold()));
        else
            this.gff.addNumFilter(new AboveTheThresholdFilter<>(m -> m.get(scoreDescription.getName()), scoreDescription.getThreshold()));

        return this;
    }

    public GreenFeatureFilterBuilder withBooleanFilter(FeatureDescription featureDescription) {
        // Se la leg non è green, risponde con un valore che contribuisce a non filtrare l'itinerario
        if (featureDescription.isPresence())
            this.gff.addBooleanFilter(new AtLeastOneTrueFilter<>(m -> m.get(featureDescription.getName())));
        else
            this.gff.addBooleanFilter(new AllTrueFilter<>(m -> !m.get(featureDescription.getName())));

        return this;
    }

    public GreenFeatureFilter build() {
        return this.gff;
    }

}
