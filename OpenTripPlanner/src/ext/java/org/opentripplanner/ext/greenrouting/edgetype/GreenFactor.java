package org.opentripplanner.ext.greenrouting.edgetype;

import java.util.Map;

public interface GreenFactor {

    double getGreenyness();

    void setGreenyness(double greenyness);

    Map<String, Double> getScores();

    void setScores(Map<String, Double> scores);

    void putScore(String label, Double value);

    Map<String, Boolean> getFeatures();

    void setFeatures(Map<String, Boolean> features);

    void putFeature(String label, boolean value);


}
