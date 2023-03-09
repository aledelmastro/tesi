package org.opentripplanner.ext.greenrouting.api.resource.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GreenRequest {

    private final List<ScoreDescription> scoreDescriptions = new ArrayList<>();
    private final List<FeatureDescription> featureDescriptions = new ArrayList<>();
    private final List<ScoreDescription> preScoreDescriptions = new ArrayList<>();
    private final List<FeatureDescription> preFeatureDescriptions = new ArrayList<>();
    private String formula;
    private final Set<String> variables = new HashSet<>();

    public GreenRequest(){}

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

    public Set<String> getVariables() {
        return this.variables;
    }

    public String getFormula() {
        return this.formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }
}
