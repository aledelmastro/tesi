package org.opentripplanner.ext.greenrouting.api.resource.filters;

import java.util.ArrayList;
import java.util.List;

public class GreenFilterRequest {

    private final List<ScoreDescription> scoreDescriptions = new ArrayList<>();
    private final List<FeatureDescription> featureDescriptions = new ArrayList<>();
    private final List<ScoreDescription> preScoreDescriptions = new ArrayList<>();
    private final List<FeatureDescription> preFeatureDescriptions = new ArrayList<>();

    public GreenFilterRequest(){}

    public List<ScoreDescription> getScores() {
        return this.scoreDescriptions;
    }

    public List<FeatureDescription> getFeatures() {
        return this.featureDescriptions;
    }

    public List<ScoreDescription> getPreScores() {
        return this.preScoreDescriptions;
    }

    public List<FeatureDescription> getPreFeatures() {
        return this.preFeatureDescriptions;
    }

}
