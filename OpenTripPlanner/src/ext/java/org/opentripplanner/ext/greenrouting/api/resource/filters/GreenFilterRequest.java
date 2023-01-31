package org.opentripplanner.ext.greenrouting.api.resource.filters;

import java.util.ArrayList;
import java.util.List;

public class GreenFilterRequest {

    public List<ScoreDescription> scoreDescriptions = new ArrayList<>();
    public List<FeatureDescription> featureDescriptions = new ArrayList<>();

    public GreenFilterRequest(){}

    public List<ScoreDescription> getScores() {
        return this.scoreDescriptions;
    }

    public List<FeatureDescription> getFeatures() {
        return this.featureDescriptions;
    }

}
