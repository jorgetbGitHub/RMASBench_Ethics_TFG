package RSLBench.Assignment;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import rescuecore2.worldmodel.EntityID;

/**
 * This class represents the assignment map between agents and targets.
 */
public class Assignment {

    /** EntityID to use when an agent is not assigned to any target */
    public static final EntityID UNKNOWN_TARGET_ID = new EntityID(-1);

    // Mapping from Agents to Targets
    private LinkedHashMap<EntityID, EntityID> map;

    /**
     * Build a new agents to fires assignment
     */
    public Assignment() {
        map = new LinkedHashMap<>();
    }

    /**
     * Build a copy of the given agents to fires assignment
     * @param assignment assignment to copy
     */
    public Assignment(Assignment assignment) {
        this();
        for (EntityID agent : assignment.getAgents()) {
            map.put(agent, assignment.getAssignment(agent));
        }
    }

    /**
     * Adds an agent to fire assignment.
     *
     * @param agent the agent ID
     * @param target the target ID
     */
    public void assign(EntityID agent, EntityID target) {
    	map.put(agent, target);
    }

    /**
     * Returns the assignment of an agent
     * @param agent the agent ID
     * @return the entity ID of the target assigned to the agent represented by the agentID
     */
    public EntityID getAssignment(EntityID agent) {
        EntityID result = map.get(agent);
        if (result == null) {
            result = UNKNOWN_TARGET_ID;
        }
        return result;
    }

    /**
     * Returns an arraylist of the entityID's of the agents
     * @return entityID of all the agents
     */
    public Set<EntityID> getAgents() {
    	return map.keySet();
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Assignments:\n");
        Iterator<Entry<EntityID,EntityID>> it = map.entrySet().iterator();
        while (it.hasNext()) {
        	Entry<EntityID,EntityID> pair = it.next();
        	buffer.append("agent ").append(pair.getKey()).append(" > ").append(pair.getValue()).append("\n");
        }
        return buffer.toString();
    }
}
