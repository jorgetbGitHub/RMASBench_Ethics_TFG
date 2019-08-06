/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.Random;

import java.util.List;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class RandomFireAgent extends AbstractRandomAgent {

    @Override
    public List<EntityID> getAvailableTargets() {
        return getProblem().getFireAgentNeighbors(getID());
    }

}
