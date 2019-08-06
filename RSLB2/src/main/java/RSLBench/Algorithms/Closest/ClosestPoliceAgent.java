/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.Closest;

import RSLBench.Assignment.Assignment;
import RSLBench.Assignment.DCOP.DefaultDCOPAgent;
import RSLBench.Helpers.Distance;
import RSLBench.PlatoonPoliceAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 * Implementation of a firefighter that simply moves to the closest known fire.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class ClosestPoliceAgent extends DefaultDCOPAgent {

    private static final Logger Logger = LogManager.getLogger(ClosestPoliceAgent.class);

    @Override
    public boolean improveAssignment() {
        final StandardWorldModel world = getProblem().getWorld();
        final EntityID id = getID();
        final double threshold = getProblem().getConfig().getFloatValue(PlatoonPoliceAgent.DISTANCE_KEY);

        // Pick the closest fire
        setTarget(Assignment.UNKNOWN_TARGET_ID);
        double minDistance = Double.POSITIVE_INFINITY;
        for (EntityID blockade : getProblem().getPoliceAgentNeighbors(id)) {
            double d = Distance.humanToBlockade(id, blockade, world, threshold);
            if (d < minDistance) {
                minDistance = d;
                setTarget(blockade);
            }
        }
        Logger.debug("Police force {} choses blockade {} (d={})", id, getTarget(), minDistance);

        return false;
    }

}
