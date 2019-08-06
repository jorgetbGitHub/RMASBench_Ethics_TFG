package RSLBench.Assignment;

import RSLBench.Helpers.Utility.ProblemDefinition;
import java.util.List;
import rescuecore2.config.Config;
import rescuecore2.standard.entities.StandardWorldModel;

/**
 * This interface represents the executor of the computation.
 * It executes the initialization, the dispatch and the receiving
 * of the messages and the improvment of the assignment for each agent.
 */
public interface Solver
{

    /**
     * Initializes the solver to operate on the specified world, and with the
     * specified config.
     * @param world world on which this solver operates.
     * @param config configuration to use.
     */
    public void initialize(StandardWorldModel world, Config config);
    
    /**
     * Get the identifier of this solver.
     * 
     * The identifier should contain both the solver type and the particular
     * solving algorithm being employed. I.e.: <em>DCOP_MaxSum</em>
     * @return 
     */
    public String getIdentifier();

    /**
     * Solves the given problem description.
     *
     * @param time current simulation time (step).
     * @param utility utility object describing the allocation problem.
     * @return assignment of agents to targets.
     */
    public Assignment solve(int time, ProblemDefinition utility);

    /**
     * Get the list of configuration keys employed by this solver.
     *
     * This list will be used to output the configuration values when writing
     * the results file.
     * @return list of configuration keys used by this solver.
     */
    public List<String> getUsedConfigurationKeys();

    /**
     * Set the maximum time (in ms) that this solver is allowed to run at
     * each simulation step.
     * @param time time (in ms) that this solver has per simulation step.
     */
    public void setMaxTime(int time);

    /**
     * Get the maximum time (in ms) that this solver is allowed to run at
     * each simulation step.
     * @return time (in ms) that this solver has per simulation step.
     */
    public int getMaxTime();
    
}
