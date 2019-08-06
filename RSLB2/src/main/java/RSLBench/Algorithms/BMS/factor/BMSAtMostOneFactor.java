/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.BMS.factor;

import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.factors.AtMostOneFactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BMSAtMostOneFactor<T> extends AtMostOneFactor<T> {
    private static final Logger Logger = LogManager.getLogger(BMSAtMostOneFactor.class);

    private T fixedNeighbor = null;
    private boolean isFixed = false;

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
            isFixed = true;
        }

        // In any case, we need to remove the neighbor
        Logger.debug("{} removes neighbor {}", this, neighbor);
        removeNeighbor(neighbor);

        // Now, if we are not fixed, 2 things may happen:
        if (!isFixed) {
            final int remainingNeighbors = getNeighbors().size();

            // 1. There are no remaining neighbors, so the factor is fixed to noone
            if (remainingNeighbors == 0) {
                isFixed = true;
            }

            // 2. One or more neighbors remain, so we keep operating normally
        }
    }

    @Override
    public long run() {
        final MaxOperator op = getMaxOperator();
        final int nNeighbors = getNeighbors().size();

        if (isFixed) {
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
        return isFixed ? fixedNeighbor : super.select();
    }

    @Override
    public String toString() {
        return "AtMostOne[" + getIdentity() + "]";
    }

}
