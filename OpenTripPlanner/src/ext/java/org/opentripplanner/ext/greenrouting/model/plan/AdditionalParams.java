package org.opentripplanner.ext.greenrouting.model.plan;

import java.util.List;
import java.util.Map;

public interface AdditionalParams {
    void setNumerical(List<Map<String, Double>> numericalP);
    void setBoolean(List<Map<String, Boolean>> booleanP);
    List<Map<String,Double>> getNumerical();
    List<Map<String,Boolean>> getBoolean();

}
