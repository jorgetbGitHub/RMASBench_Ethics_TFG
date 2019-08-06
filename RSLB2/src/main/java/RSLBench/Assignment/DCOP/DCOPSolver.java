package RSLBench.Assignment.DCOP;

import RSLBench.Assignment.AbstractSolver;
import RSLBench.Assignment.Assignment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import RSLBench.Comm.Message;
import RSLBench.Comm.CommunicationLayer;
import RSLBench.Helpers.Logging.Markers;
import RSLBench.Helpers.Utility.ProblemDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;

import rescuecore2.worldmodel.EntityID;

/**
 * @see AssignmentInterface
 */
public abstract class DCOPSolver extends AbstractSolver {

    /**
     * The number of iterations max that an algorithm can perform before the
     * agents take a definitive decision for each timestep.
     */
    public static final String KEY_DCOP_ITERATIONS = "dcop.iterations";

    /** Configuration key to enable/disable usage of anytime assignments. */
    public static final String KEY_ANYTIME = "dcop.anytime";

    /**
     * Configuration key to enable/disable the sequential greedy correction of
     * assignments.
     */
    public static final String KEY_GREEDY_CORRECTION = "dcop.greedy_correction";

    private static final Logger Logger = LogManager.getLogger(DCOPSolver.class);
    private List<DCOPAgent> agents;
    private List<Double> utilities;

    public DCOPSolver() {
        utilities = new ArrayList<>();
    }

    @Override
    public List<String> getUsedConfigurationKeys() {
        List<String> keys = super.getUsedConfigurationKeys();
        keys.add(KEY_DCOP_ITERATIONS);
        keys.add(KEY_ANYTIME);
        keys.add(KEY_GREEDY_CORRECTION);
        return keys;
    }

    @Override
    public Assignment compute(ProblemDefinition problem) {
        long startTime = System.currentTimeMillis();
        boolean ranOutOfTime = false;
        CommunicationLayer comLayer = new CommunicationLayer();
        initializeAgents(problem);

        int totalNccc = 0;
        long bMessages = 0;
        int nMessages = 0;

        int MAX_ITERATIONS = getConfig().getIntValue(KEY_DCOP_ITERATIONS);
        boolean done = false;
        int iterations = 0;
        Assignment finalAssignment = null, bestAssignment = null;
        double bestAssignmentUtility = Double.NEGATIVE_INFINITY;
        long iterationTime = System.currentTimeMillis();
        while (!done && iterations < MAX_ITERATIONS) {
            finalAssignment = new Assignment();

            // send messages
            for (DCOPAgent agent : agents) {
                Collection<? extends Message> messages = agent.sendMessages(comLayer);
                //collect the byte size of the messages exchanged between agents
                nMessages = nMessages + messages.size();
                for (Message msg : messages) {
                    bMessages += msg.getBytes();
                }
            }

            // receive messages
            for (DCOPAgent agent : agents) {
                agent.receiveMessages(comLayer.retrieveMessages(agent.getID()));
            }

            // try to improve assignment
            done = true;
            long nccc = 0;
            for (DCOPAgent agent : agents) {
                boolean improved = agent.improveAssignment();
                nccc = Math.max(nccc, agent.getConstraintChecks());
                done = done && !improved;

                // Collect assignment
                finalAssignment.assign(agent.getID(), agent.getTarget());
            }

            // Collect the best assignment visited
            double assignmentUtility = getUtility(problem, finalAssignment);
            utilities.add(assignmentUtility);
            totalNccc += nccc;
            iterations++;

            // Check the maximum time requirements
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime >= maxTime) {
                Logger.info("Solver {} ran out of time (got {}, took {} to do {} iterations)",
                        getIdentifier(), maxTime, elapsedTime, iterations);
                ranOutOfTime = true;
                break;
            }

            Logger.trace("Assignment util: {}, values: ", assignmentUtility, finalAssignment);
            if (assignmentUtility > bestAssignmentUtility || Double.isInfinite(bestAssignmentUtility)) {
                bestAssignmentUtility = assignmentUtility;
                bestAssignment = finalAssignment;
            }

            long time = System.currentTimeMillis();
            Logger.trace("Iteration {} took {}ms.", iterations, time-iterationTime);
            iterationTime = time;
        }
        Logger.debug("Done with iterations. Needed {} in {}ms.", iterations,
                System.currentTimeMillis() - startTime);

        // Recompute this because its not saved from the solving loop
        double finalAssignmentUtility = getUtility(problem, finalAssignment);

        // Perform greedy improvement on the latest assignment if time permits
        Assignment finalGreedy = finalAssignment;
        double finalGreedyU = finalAssignmentUtility;
        if (!ranOutOfTime) {
                 finalGreedy = greedyImprovement(problem, finalAssignment);
                 finalGreedyU = getUtility(problem, finalGreedy);
        }
        // saftey check, because this should never happen
        if (finalAssignmentUtility > finalGreedyU) {
            Logger.error("Final assignment utility went from {} to {}",
                    finalAssignmentUtility, finalGreedyU);
        }

        Logger.trace("{} final {}", getIdentifier(), finalAssignment);
        Logger.trace("{} utility: {}", getIdentifier(), finalAssignmentUtility);

        // Perform greedy improvement on the anytime best assignment if time permits
        Assignment bestGreedy = bestAssignment;
        double bestGreedyU = bestAssignmentUtility;
        if (!ranOutOfTime) {
            bestGreedy = greedyImprovement(problem, bestAssignment);
            bestGreedyU = getUtility(problem, bestGreedy);
        }
        // saftey check, because this should never happen
        if (bestAssignmentUtility > bestGreedyU) {
            Logger.error("Greedy improvement lowered utility from {} to {}",
                    bestAssignmentUtility, bestGreedyU);
        }

        long algBMessages = bMessages;
        int  algNMessages = nMessages;
        // TODO: See below
        for (DCOPAgent agent : agents) {
            nMessages += 0; // This should be the number of messages sent to prune the problem,
            bMessages += 0; // provided by the ProblemDefinition class itself.
        }

        int  nOtherMessages = nMessages - algNMessages;
        long bOtherMessages = bMessages - algBMessages;

        // Report statistics
        stats.report("iterations", iterations);
        stats.report("NCCCs", totalNccc);
        stats.report("MessageNum", nMessages);
        stats.report("MessageBytes", bMessages);
        stats.report("OtherNum", nOtherMessages);
        stats.report("OtherBytes", bOtherMessages);
        stats.report("final", finalAssignmentUtility);
        stats.report("best", bestAssignmentUtility);
        if (!ranOutOfTime) {
            stats.report("final_greedy", finalGreedyU);
            stats.report("best_greedy", bestGreedyU);
        } else {
            stats.report("final_greedy", Double.NaN);
            stats.report("best_greedy", Double.NaN);
        }
        reportUtilities();

        // Return the assignment depending on the configuration settings
        boolean anytime = config.getBooleanValue(KEY_ANYTIME);
        boolean greedy  = config.getBooleanValue(KEY_GREEDY_CORRECTION);
        if (anytime && greedy && !ranOutOfTime) {
            return bestGreedy;
        } else if (anytime && bestAssignment != null) {
            return bestAssignment;
        } else if (greedy && !ranOutOfTime) {
            return finalGreedy;
        }
        return finalAssignment;
    }

    private void reportUtilities() {
        StringBuilder buf = new StringBuilder();
        String prefix = "";
        for (double utility : utilities) {
            buf.append(prefix).append(utility);
            prefix = ",";
        }
        stats.report("utilities", buf.toString());
        utilities.clear();
    }

    /**
     * This method initializes the agents for the simulation (it calls the
     * initialize method of the specific DCOP algorithm used for the
     * computation)
     *
     * @param problem the problem definition.
     */
    protected void initializeAgents(ProblemDefinition problem) {
        agents = new ArrayList<>();
        final long startTime = System.currentTimeMillis();
        initializeAgentType(problem, problem.getFireAgents());
        initializeAgentType(problem, problem.getPoliceAgents());
        Logger.debug(Markers.BLUE, "Initialized {} {} agents in {}ms.",
                agents.size(), getIdentifier(), System.currentTimeMillis() - startTime);
    }

    private void initializeAgentType(ProblemDefinition problem, List<EntityID> ids) {
        for (EntityID agentID : ids) {
            StandardEntity entity = problem.getWorld().getEntity(agentID);
            DCOPAgent agent = buildAgent(entity.getStandardURN());
            // @TODO: if required give only local problem view to each agent!
            agent.initialize(config, agentID, problem);
            agents.add(agent);
        }
    }

    protected abstract DCOPAgent buildAgent(StandardEntityURN type);

    /**
     * Operate on the (sequential) greedy algorithm.
     *
     * This gives the agent an opportunity to orderly reconsider their choices.
     *
     * @param initial current assignment.
     */
    public Assignment greedyImprovement(ProblemDefinition problem,
            Assignment initial)
    {
        Assignment result = new Assignment(initial);
        double bestUtility = getUtility(problem, initial);
        Logger.debug("Initiating greedy improvement. Initial value {}", bestUtility);

        // Allow each fire agent to try to improve
        for (EntityID fireAgent : problem.getFireAgents()) {
            Assignment tested = new Assignment(result);
            for (EntityID fire : problem.getFires()) {
                tested.assign(fireAgent, fire);
                double utility = getUtility(problem, tested);
                if (utility > bestUtility) {
                    result.assign(fireAgent, fire);
                    bestUtility = utility;
                }
            }
        }

        // Allow each police agent to try to improve
        for (EntityID police : problem.getPoliceAgents()) {
            Assignment tested = new Assignment(result);
            for (EntityID blockade : problem.getBlockades()) {
                tested.assign(police, blockade);
                double utility = getUtility(problem, tested);
                if (utility > bestUtility) {
                    result.assign(police, blockade);
                    bestUtility = utility;
                }
            }
        }

        Logger.debug("Finished greedy improvement. Final value {}", bestUtility);
        return result;
    }

}
