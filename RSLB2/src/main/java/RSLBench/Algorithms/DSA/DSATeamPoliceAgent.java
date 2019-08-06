/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.DSA;

import RSLBench.Algorithms.DSA.scoring.BlockadeTeamScoringFunction;
import RSLBench.Algorithms.DSA.scoring.ScoringFunction;
import RSLBench.Helpers.Utility.ProblemDefinition;

import java.util.HashSet;
import java.util.List;
import rescuecore2.misc.Pair;

import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class DSATeamPoliceAgent extends DSAAbstractAgent {

    @Override
    protected HashSet<EntityID> computeNeighbors() {
        final EntityID id = getID();
        final ProblemDefinition problem = getProblem();

        HashSet<EntityID> neighbors = new HashSet<>();
        for (EntityID blockade : problem.getPoliceAgentNeighbors(id)) {
            neighbors.addAll(problem.getBlockadeNeighbors(blockade));
            for (Pair<EntityID, EntityID> info : problem.getFireAgentsBlockedByBlockade(blockade)) {
                final EntityID fireAgent = info.first();
                neighbors.add(fireAgent);
            }
        }
        return neighbors;
    }

    @Override
    protected List<EntityID> computeCandidates() {
        return getProblem().getPoliceAgentNeighbors(getID());
    }

    @Override
    protected ScoringFunction buildScoringFunction() {
        return new BlockadeTeamScoringFunction();
    }

    @Override
    protected EntityID getBestTarget() {
        return getTargetScores().getBestTarget(getCandidateTargets());
    }

    @Override
    protected EntityID getPreferredTarget() {
        return getProblem().getHighestTargetForPoliceAgent(getID(), getCandidateTargets());
    }

}
