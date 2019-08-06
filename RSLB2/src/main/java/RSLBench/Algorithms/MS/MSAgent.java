/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.MS;

import RSLBench.Assignment.DCOP.AbstractDCOPAgent;
import RSLBench.Comm.CommunicationLayer;
import RSLBench.Comm.Message;
import RSLBench.Helpers.Utility.ProblemDefinition;
import es.csic.iiia.ms.Variable;
import es.csic.iiia.ms.functions.CostFunction;
import es.csic.iiia.ms.functions.CostFunctionFactory;
import es.csic.iiia.ms.functions.MasterIterator;
import es.csic.iiia.ms.node.FunctionNode;
import es.csic.iiia.ms.node.Node;
import es.csic.iiia.ms.node.VariableNode;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import rescuecore2.config.Config;
import rescuecore2.worldmodel.EntityID;

/**
 * Agent using the (classic) Max-Sum algorithm.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSAgent extends AbstractDCOPAgent {
    private static final org.apache.logging.log4j.Logger Logger = LogManager.getLogger(MSAgent.class);

    private long nConstraintChecks;

    private CostFunctionFactory cfFactory = new MSCostFunctionFactory();
    private MSCommunicator communicator;

    private VariableNode variableNode;
    private Map<Identity, EntityID> nodeLocations = new HashMap<>();
    private Map<Identity, Node> localNodes = new HashMap<>();

    private Variable getVariable(EntityID fireAgent) {
        final ProblemDefinition problem = getProblem();
        final int nCandidateFires = problem.getFireAgentNeighbors(fireAgent).size();
        return new Variable(new Identity(fireAgent), nCandidateFires);
    }

    private CostFunction buildFireAgentPotential(EntityID fireAgent, Variable variable) {
        final ProblemDefinition problem = getProblem();
        final List<EntityID> candidates = problem.getFireAgentNeighbors(fireAgent);

        // The potential is a tabular function of just one variable (a vector), with
        // the unary costs of each candidate.
        CostFunction potential = cfFactory.buildCostFunction(new Variable[]{variable}, 0);
        for (int i=0; i<candidates.size(); i++) {
            final double value = problem.getFireUtility(fireAgent, candidates.get(i));
            potential.setValue(i, value);
        }

        return potential;
    }

    private CostFunction buildFirePotential(EntityID fire) {
        final ProblemDefinition problem = getProblem();
        final List<EntityID> fireAgents = problem.getFireAgentNeighbors(fire);
        final int nFireAgents = fireAgents.size();

        // List of variables involved in this factor
        final Variable[] variables = new Variable[nFireAgents];
        // List of values where the variable is assigned to this factor's fire
        final int[] assignments = new int[nFireAgents];
        for (int i=0; i<nFireAgents; i++) {
            final EntityID fireAgent = fireAgents.get(i);
            variables[i] = getVariable(fireAgent);
            assignments[i] = problem.getFireAgentNeighbors(fireAgent).indexOf(fire);
        }

        // Build the potential CostFunction and set the values
        CostFunction f = cfFactory.buildCostFunction(variables, 0);
        for (MasterIterator it = f.masterIterator(); it.hasNext();) {
            final long idx = it.next();
            final int[] indices = it.getIndices();

            int nActiveCandidates = 0;
            for (int i=0; i<nFireAgents; i++) {
                if (indices[i] == assignments[i]) {
                    nActiveCandidates++;
                }
            }

            f.setValue(idx, -problem.getUtilityPenalty(fire, nActiveCandidates));
        }

        return f;
    }

    private void buildFactorNodes() {
        final ProblemDefinition problem = getProblem();
        final List<EntityID> fires = problem.getFires();

        final int nAgent  = problem.getFireAgents().indexOf(getID());
        final int nFireAgents = problem.getNumFireAgents();
        final int nFires = fires.size();

        for (int i=nAgent; i<nFires; i+=nFireAgents) {
            final EntityID fire = fires.get(i);

            Logger.trace("Creating factor for fire {}, candidates: {}", fire, problem.getFireNeighbors(fire));
            Identity functionId = new Identity(fire);
            FunctionNode function = new FunctionNode(functionId, communicator, buildFirePotential(fire));
            localNodes.put(functionId, function);

            // Link the fire node with all its neighboring agents
            for (EntityID fireAgent : problem.getFireNeighbors(fire)) {
                function.addNeighbor(new Identity(fireAgent), getVariable(fireAgent));
            }
        }
    }

    private void computeNodeLocations() {
        final ProblemDefinition problem = getProblem();
        final List<EntityID> fireAgents = problem.getFireAgents();
        final List<EntityID> fires = problem.getFires();
        final int nFires = fires.size();
        final int nFireAgents = fireAgents.size();

        // Compute the location of the fire agents
        for (EntityID fireAgent : fireAgents) {
            nodeLocations.put(new Identity(fireAgent), fireAgent);
        }

        // And now the location of the fires
        for (int i=0; i<nFires; i++) {
            final int nAgent = i % nFireAgents;
            nodeLocations.put(new Identity(fires.get(i)), fireAgents.get(nAgent));
        }
    }

    private void buildVariableNode() {
        final EntityID id = getID();
        final List<EntityID> fires = getProblem().getFireAgentNeighbors(id);

        communicator = new MSCommunicator(getProblem().getConfig());
        Identity variableId = new Identity(id);
        Variable variable = getVariable(id);
        CostFunction potential = buildFireAgentPotential(id, variable);
        Logger.trace("Local variable: {}, potential: {}", variable, potential);
        variableNode = new VariableNode(variableId, communicator, potential);
        localNodes.put(variableId, variableNode);

        // Link the variable with all its neighbors
        for (EntityID fire : fires) {
            variableNode.addNeighbor(new Identity(fire), getVariable(id));
        }
    }

    @Override
    public void initialize(Config config, EntityID id, ProblemDefinition problem) {
        super.initialize(config, id, problem);

        // Create the variable node for this agent
        buildVariableNode();

        // Create the factor nodes handled by this agent
        buildFactorNodes();

        // And compute the locations of neighboring nodes, so we can message them
        computeNodeLocations();
    }



    @Override
    public boolean improveAssignment() {
        final ProblemDefinition problem = getProblem();
        final EntityID id = getID();
        final List<EntityID> candidates = problem.getFireAgentNeighbors(id);

        // Let all nodes run
        nConstraintChecks = 0;
        for (Node node : localNodes.values()) {
            nConstraintChecks += node.getBelief().getSize();
            node.run();
        }

        // Make a choice
        if (candidates.isEmpty()) {
            // If we got no candidates because of pruning, just go to the most preferred fire
            setTarget(problem.getHighestTargetForFireAgent(id));
        } else {
            // Otherwise make the variable node choose
            final int choice = variableNode.select();
            setTarget(candidates.get(choice));
        }

        return !communicator.isConverged();
    }

    @Override
    public Collection<MSMessage> sendMessages(CommunicationLayer com) {
        Collection<MSMessage> messages = communicator.flushMessages();

        // Send them
        for (MSMessage message : messages) {
            EntityID recipientAgent = nodeLocations.get(message.recipient);
            com.send(recipientAgent, message);
        }

        return messages;
    }

    @Override
    public void receiveMessages(Collection<Message> messages) {
        if (messages == null) {
            return;
        }
        for (Message message : messages) {
            if (message == null) {
                Logger.warn("Received null message {}", message);
                continue;
            }
            receiveMessage(message);
        }
    }

    private void receiveMessage(Message message) {
        if (!(message instanceof MSMessage)) {
            throw new IllegalArgumentException("Max-Sum agents msut receive MSMessages only.");
        }

        MSMessage msg = (MSMessage)message;
        Node recipientNode = localNodes.get(msg.recipient);
        if (recipientNode == null) {
            Logger.error("Agent {} received message {} for the non-local node {}",
                    getID(), msg, msg.recipient);
            System.exit(0);
        }

        recipientNode.receive(msg.message, msg.sender);
    }

    @Override
    public long getConstraintChecks() {
        return nConstraintChecks;
    }

}
