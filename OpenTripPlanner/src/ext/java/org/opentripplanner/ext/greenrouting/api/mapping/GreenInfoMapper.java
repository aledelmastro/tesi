package org.opentripplanner.ext.greenrouting.api.mapping;

import java.util.ArrayList;
import org.opentripplanner.ext.greenrouting.api.model.ApiGreenEdgeInfo;
import org.opentripplanner.ext.greenrouting.api.model.ApiGreenInfo;
import org.opentripplanner.ext.greenrouting.model.plan.GreenStreetLeg;

public class GreenInfoMapper {

    public ApiGreenInfo mapGreenInfo(
            GreenStreetLeg leg
    ) {
        var apiSF = new ApiGreenInfo();

        var scores = leg.getNumerical();
        var features = leg.getBoolean();
        var arcsLength = leg.getArcsLength();

        var edgesInfo = new ArrayList<ApiGreenEdgeInfo>();

        for (int i = 0; i < scores.size(); i++)
            edgesInfo.add(new ApiGreenEdgeInfo(arcsLength.get(i), scores.get(i), features.get(i)));

        apiSF.apiEdgesInfo = edgesInfo;

        return apiSF;
    }
}
