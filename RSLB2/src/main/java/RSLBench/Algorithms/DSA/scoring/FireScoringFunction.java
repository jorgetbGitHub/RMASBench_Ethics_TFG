/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.DSA.scoring;

import RSLBench.Algorithms.DSA.TargetScores;
import RSLBench.Constants;
import RSLBench.Helpers.Utility.ProblemDefinition;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class FireScoringFunction extends AbstractScoringFunction {

    @Override
    public double score(EntityID agent, EntityID target, TargetScores scores, ProblemDefinition problem) {
        final int nAgents = scores.getAgentCount(target);
        CC();

        // Compute the difference in penalty between going to that fire and not going there
        final double penalty = problem.getUtilityPenalty(target, nAgents+1)
                - problem.getUtilityPenalty(target, nAgents);
        CC();CC();

        // Compute the individual utility of going to that fire
        double utility = problem.getFireUtility(agent, target);

        // Subtract the corresponding penalty if that fire is blocked
        if (problem.isFireAgentBlocked(agent, target)) {
                utility -= problem.getConfig().getFloatValue(Constants.KEY_BLOCKED_FIRE_PENALTY);
        }
        CC();

        // The score is the individual utility gain minus the increase in penalty
        return  utility - penalty;
    }

}
