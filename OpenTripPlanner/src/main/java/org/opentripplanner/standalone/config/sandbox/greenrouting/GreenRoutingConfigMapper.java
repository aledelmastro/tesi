package org.opentripplanner.standalone.config.sandbox.greenrouting;

import org.opentripplanner.ext.greenrouting.configuration.GreenRoutingConfig;
import org.opentripplanner.standalone.config.NodeAdapter;

public class GreenRoutingConfigMapper {

    public static GreenRoutingConfig map(NodeAdapter c) {
        if(c.isEmpty()) {
            return null;
        }
        return new GreenRoutingConfig(
            c.asText("fileName"),
            c.asText("id")
        );
    }
}
