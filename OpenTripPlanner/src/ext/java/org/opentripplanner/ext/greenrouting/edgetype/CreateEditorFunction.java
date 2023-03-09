package org.opentripplanner.ext.greenrouting.edgetype;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.graph.Edge;

public interface CreateEditorFunction {
    StateEditor apply(State s0, Edge edge, TraverseMode mode, boolean bicycleWalking);
}
