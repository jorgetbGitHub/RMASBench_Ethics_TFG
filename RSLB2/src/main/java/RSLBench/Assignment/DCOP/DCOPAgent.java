package RSLBench.Assignment.DCOP;

import java.util.Collection;

import rescuecore2.worldmodel.EntityID;

import RSLBench.Comm.Message;
import RSLBench.Helpers.Utility.ProblemDefinition;
import RSLBench.Comm.CommunicationLayer;
import rescuecore2.config.Config;
/**
 * This interface implements the actions that a single agent can perform in a DCOP algorithm.
 * The implementations of this interface are executed
 * by the implementations of the AssignmentInterface interface.
 */
public interface DCOPAgent
{
    /**
     * This method initializes the agent.
     * @param config: configuration being used by the solver.
     * @param agentID: the ID of the agent (as defined in the world model).
     * @param utility: a matrix that contains all the agent-target utilities
     * (for all the agents and alla the targets).
     */
    public void initialize(Config config, EntityID agentID, ProblemDefinition problem);

    /**
     * Considering all the messages received from other agents, tries to find
     * an improvement over the previous assignment of the agent.
     * @return true, if the assignment of this agent changed, false otherwise.
     */
    public boolean improveAssignment();

    /**
     * Returns the ID of the agent.
     * @return the ID of the agent.
     */
    public EntityID getID();

    /**
     * Returns the ID  of the currently assigned target.
     * @return the ID of the target.
     */
    public EntityID getTarget();

    /**
     * Sends a set of messages from an agent to all the recipients.
     * @param com: a communication simulator.
     * @return The set of messages that have been sent.
     */
    public Collection<? extends Message> sendMessages(CommunicationLayer com);

    /**
     * Receives a set of messages sent by some other agents.
     * @param messages: colletcion of messages received from other agents.
     */
    public void receiveMessages(Collection<Message> messages);

    /**
     * Returns the number of constraint checks performed during the latest
     * iteration.
     *
     * @return number of constraint checks
     */
    public long getConstraintChecks();
    
}
