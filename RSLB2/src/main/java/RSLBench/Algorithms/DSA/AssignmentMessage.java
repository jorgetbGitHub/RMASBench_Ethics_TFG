package RSLBench.Algorithms.DSA;

import RSLBench.Comm.Message;
import rescuecore2.worldmodel.EntityID;

/**
 * Message to notify neighbors about the target being chosen by an agent.
 */
public class AssignmentMessage implements Message {

    private EntityID agent;
    private EntityID target;

    /**
     * Builds a new assignment message.
     *
     * @param agent the agent id
     * @param target the target id
     */
    public AssignmentMessage(EntityID agent, EntityID target)
    {
        this.agent = agent;
        this.target = target;
    }

    /**
     * Get the agent identifier.
     * @return agent identifier
     */
    public EntityID getAgent()
    {
        return agent;
    }

    /**
     * Get the target identifier.
     * @return target identifier
     */
    public EntityID getTarget()
    {
        return target;
    }

    @Override
    public int getBytes() {
        return Message.BYTES_ENTITY_ID*2;
    }
}
