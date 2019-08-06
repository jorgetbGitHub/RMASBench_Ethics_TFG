/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.Closest;

import RSLBench.Assignment.Assignment;
import RSLBench.Assignment.DCOP.DefaultDCOPAgent;
import RSLBench.Helpers.Distance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 * Implementation of a firefighter that simply moves to the closest known fire.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class ClosestFireAgent extends DefaultDCOPAgent {

    private static final Logger Logger = LogManager.getLogger(ClosestFireAgent.class);

    @Override
    public boolean improveAssignment() {
        final StandardWorldModel world = getProblem().getWorld();
        final EntityID id = getID();

        // Pick the closest fire
        setTarget(Assignment.UNKNOWN_TARGET_ID);
        double minDistance = Double.POSITIVE_INFINITY;
        for (EntityID fire : getProblem().getFireAgentNeighbors(id)) {
            double d = Distance.humanToBuilding(id, fire, world);
            if (d < minDistance) {
                minDistance = d;
                setTarget(fire);
            }
        }
        Logger.debug("Fire brigade {} choses fire {} (d={})", id, getTarget(), minDistance);

        return false;
    }

}
