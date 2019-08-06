/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.MS;

import es.csic.iiia.ms.Communicator;
import es.csic.iiia.ms.functions.CostFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.config.Config;
import rescuecore2.misc.Pair;

/**
 * Communication channel for a Max-Sum agent.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSCommunicator implements Communicator<Identity> {
    private static final Logger Logger = LogManager.getLogger(MSCommunicator.class);

    /** Threshold below which messages are considered equal. */
    public static final double EPSILON = 1e-5/2.;

    /** The damping factor to employ */
    private final double DAMPING_FACTOR;

    private boolean converged;

    private List<MSMessage> outgoingMessages;
    private Map<Pair<Identity, Identity>, CostFunction> oldMessages;

    public MSCommunicator(Config config) {
        DAMPING_FACTOR = config.getFloatValue(MaxSum.KEY_MAXSUM_DAMPING);
        outgoingMessages = new ArrayList<>();
        oldMessages = new HashMap<>();
        converged = true;
    }

    public Collection<MSMessage> flushMessages() {
        Collection<MSMessage> result = outgoingMessages;
        outgoingMessages = new ArrayList<>();
        converged = true;
        return result;
    }

    @Override
    public void send(CostFunction message, Identity from, Identity to) {
        Logger.trace("Message from {} to {} : {}", new Object[]{from, to, message});
        outgoingMessages.add(new MSMessage(message, from, to));

        // Convergence check
        // The algorithm has converged unless there is at least one message
        // different from the previous iteration
        Pair<Identity, Identity> sr = new Pair<>(from, to);
        CostFunction oldMessage = oldMessages.get(sr);

        // Apply damping
        if (oldMessage != null) {
            double[] oldValues = oldMessage.getValues();
            double[] values = message.getValues();
            for (int i=0; i<values.length; i++) {
                values[i] = oldValues[i] * DAMPING_FACTOR + values[i] * (1 - DAMPING_FACTOR);
            }
            message.setValues(values);
        }

        if (oldMessage == null || !oldMessage.equals(message, EPSILON)) {
            converged = false;
        }
        oldMessages.put(sr, message);
    }

    /**
     * Returns true if all the messages sent in the current iteration are
     * <em>equal</em> to the messages sent in the previous one.
     *
     * @see #EPSILON
     * @return true if the algorithm has converged, or false otherwise.
     */
    public boolean isConverged() {
        return converged;
    }

}
