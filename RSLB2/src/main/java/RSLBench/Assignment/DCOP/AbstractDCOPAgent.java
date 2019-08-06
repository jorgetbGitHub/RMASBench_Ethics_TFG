/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Assignment.DCOP;

import RSLBench.Helpers.Utility.ProblemDefinition;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractDCOPAgent implements DCOPAgent {
    private EntityID id;
    private EntityID target;
    private ProblemDefinition problem;

    @Override
    public EntityID getID() {
        return id;
    }

    @Override
    public EntityID getTarget() {
        return target;
    }

    protected void setTarget(EntityID target) {
        this.target = target;
    }

    @Override
    public void initialize(Config config, EntityID agentID, ProblemDefinition utility) {
        problem = utility;
        this.id = agentID;
    }

    /**
     * @return the problem
     */
    protected ProblemDefinition getProblem() {
        return problem;
    }

}
