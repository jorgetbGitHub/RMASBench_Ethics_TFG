/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.BMS.factor;

import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.factors.SelectorFactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BMSSelectorFactor<T> extends SelectorFactor<T> {
    private static final Logger Logger = LogManager.getLogger(BMSSelectorFactor.class);

    private T fixedNeighbor = null;

    @Override
    public void receive(double message, T sender) {
        if (Double.isInfinite(message)) {
            // If it is not a neighbor, this means that we have already removed it before.
            if (getNeighbors().contains(sender)) {
                Logger.debug("{} receives {} from {}", this, message, sender);
                reduce(sender, getMaxOperator().getWorstValue() != message);
                send(message, sender);
            }
            return;
        }
        super.receive(message, sender);
    }

    private void reduce(T neighbor, boolean active) {
        Logger.debug("{} fixing neighbor {} to {}", this, neighbor, active);

        // If the neighbor is fixed to active, we turn into a constant fixed to this neighbor
        if (active) {
            Logger.debug("{} is now fixed to {}", this, neighbor);
            fixedNeighbor = neighbor;
        }

        // In any case, we need to remove the neighbor
        Logger.debug("{} removes neighbor {}", this, neighbor);
        removeNeighbor(neighbor);

        // Now, if we are not fixed, 3 things may happen:
        if (fixedNeighbor == null) {
            final int remainingNeighbors = getNeighbors().size();

            // 1. There are no remaining neighbors, so this is an error
            if (remainingNeighbors == 0) {
                throw new RuntimeException("This selector is left out of possible variables to activate!");

            // 2. Only one neighbor remains, so we require it to be fixed to active
            } else if (remainingNeighbors == 1) {
                Logger.debug("{} prepares fix to neighbor {} because its the only remaining one", this, getNeighbors().get(0));
                fixedNeighbor = getNeighbors().get(0);
            }

            // 3. Two or more neighbors remain, so we keep operating normally
        }
    }

    @Override
    public long run() {
        final MaxOperator op = getMaxOperator();
        final int nNeighbors = getNeighbors().size();

        if (fixedNeighbor != null) {
            for (T neighbor : getNeighbors()) {
                if (neighbor.equals(fixedNeighbor)) {
                    send(op.inverse().getWorstValue(), neighbor);
                } else {
                    send(op.getWorstValue(), neighbor);
                }
            }
            return nNeighbors;
        }

        return super.run();
    }

    @Override
    public T select() {
        return fixedNeighbor != null ? fixedNeighbor : super.select();
    }

    @Override
    public String toString() {
        return "Selector[" + getIdentity() + "]";
    }

}
