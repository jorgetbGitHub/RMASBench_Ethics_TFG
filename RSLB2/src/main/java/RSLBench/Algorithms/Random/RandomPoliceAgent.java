/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.Random;

import java.util.List;
import rescuecore2.worldmodel.EntityID;

/**
 * Police agent that picks a random target blockade among the currently available ones.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
class RandomPoliceAgent extends AbstractRandomAgent {

    @Override
    public List<EntityID> getAvailableTargets() {
        return getProblem().getPoliceAgentNeighbors(getID());
    }

}
