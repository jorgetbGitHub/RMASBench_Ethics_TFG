/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.DSA.scoring;

import RSLBench.Algorithms.DSA.TargetScores;
import RSLBench.Helpers.Utility.ProblemDefinition;
import rescuecore2.worldmodel.EntityID;

/**
 * Interface of a function that specifies the penalty of assigning a given number of
 * agents to a target.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public interface ScoringFunction {

    /**
     * Get the score differential between assigning the agent to the specified agent and not
     * assigning it anywhere given the neighbor's choices as reflected in scores.
     *
     * @param agent agent whose target is under consideration.
     * @param target target to evaluate.
     * @param scores neighbor choices as collected from incoming messages.
     * @param problem current problem definition.
     * @return score differential between assigning the target to that agent and not assigning it
     * anywhere.
     */
    public double score(EntityID agent, EntityID target, TargetScores scores, ProblemDefinition problem);

    public long getCCs();

}
