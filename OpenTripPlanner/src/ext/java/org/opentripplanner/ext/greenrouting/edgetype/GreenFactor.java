package org.opentripplanner.ext.greenrouting.edgetype;

import java.util.Map;

public interface GreenFactor {

    double getGreenyness();

    void setGreenyness(double greenyness);

    Map<String, Double> getVariables();

    void setVariables(Map<String, Double> variables);

    void putVariable(String variable, Double value);
}
