package RSLBench.Algorithms.DSA;

import RSLBench.Algorithms.DSA.scoring.FireScoringFunction;
import RSLBench.Assignment.Assignment;
import RSLBench.Helpers.Utility.ProblemDefinition;

import java.util.HashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rescuecore2.worldmodel.EntityID;

/**
 * Fire agent that coordinates using the DSA algorithm to pick a target.
 */
public class DSAFireAgent extends DSAAbstractAgent {
    private static final Logger Logger = LogManager.getLogger(DSAFireAgent.class);

    @Override
    protected HashSet<EntityID> computeNeighbors() {
        final ProblemDefinition problem = getProblem();
        final EntityID id = getID();

        // The neighbors of this agent are all candidates of all eligible fires
        HashSet<EntityID> neighbors = new HashSet<>();
        for (EntityID fire : problem.getFireAgentNeighbors(id)) {
            neighbors.addAll(problem.getFireNeighbors(fire));
        }
        return neighbors;
    }

    @Override
    protected List<EntityID> computeCandidates() {
        return getProblem().getFireAgentNeighbors(getID());
    }

    @Override
    protected FireScoringFunction buildScoringFunction() {
        return new FireScoringFunction();
    }

    @Override
    protected EntityID getBestTarget() {
        EntityID bestTarget = getTargetScores().getBestTarget(getCandidateTargets());

        // In the case of firefighters, it is always better to do something, so we go to the
        // most preferred fire if we are left without candidates during the pruning
        if (bestTarget == Assignment.UNKNOWN_TARGET_ID) {
            bestTarget = getProblem().getHighestTargetForFireAgent(getID());
        }

        return bestTarget;
    }

    @Override
    protected EntityID getPreferredTarget() {
        return getProblem().getHighestTargetForFireAgent(getID(), getCandidateTargets());
    }

}