/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.Random;

import RSLBench.Assignment.Assignment;
import RSLBench.Assignment.DCOP.DefaultDCOPAgent;
import RSLBench.Helpers.Utility.ProblemDefinition;
import java.util.List;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.EntityID;

/**
 * Agent that chooses its target randomly.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractRandomAgent extends DefaultDCOPAgent {

    private java.util.Random random;

    @Override
    public void initialize(Config config, EntityID agentID, ProblemDefinition utility) {
        super.initialize(config, agentID, utility);
        random = config.getRandom();
    }

    public abstract List<EntityID> getAvailableTargets();

    @Override
    public boolean improveAssignment() {
        final EntityID id = getID();
        setTarget(Assignment.UNKNOWN_TARGET_ID);

        List<EntityID> targets = getAvailableTargets();
        final int nTargets = targets.size();
        if (nTargets > 0) {
            int choice = random.nextInt(nTargets);
            setTarget(targets.get(choice));
        }

        return true;
    }

}
