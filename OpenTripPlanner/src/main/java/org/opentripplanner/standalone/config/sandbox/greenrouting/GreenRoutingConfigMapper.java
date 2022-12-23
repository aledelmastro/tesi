package org.opentripplanner.standalone.config.sandbox.greenrouting;

import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig.GreenMappingMode;
import org.opentripplanner.standalone.config.NodeAdapter;

public class GreenRoutingConfigMapper {

    public static GreenRoutingConfig map(NodeAdapter c) {
        if(c.isEmpty()) {
            return null;
        }

        GreenMappingMode mode = null;
        var mappingType = c.asText("mappingType", "fast");
        switch (mappingType) {
            case "fast":
                mode = GreenMappingMode.FAST;
                break;
            case "weighted":
                mode = GreenMappingMode.WEIGHTED;
                break;
            case "fit":
                mode = GreenMappingMode.FIT_DATA_SEGMENTS;
                break;
        }


        return new GreenRoutingConfig(
            c.asText("fileName"),
            c.asText("id"),
            c.asText("score"),
            mode
        );
    }
}
