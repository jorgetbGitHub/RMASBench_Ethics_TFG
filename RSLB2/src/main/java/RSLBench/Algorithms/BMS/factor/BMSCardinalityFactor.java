/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.BMS.factor;

import es.csic.iiia.bms.factors.CardinalityFactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BMSCardinalityFactor<T> extends CardinalityFactor<T> {
    private static final Logger Logger = LogManager.getLogger(BMSCardinalityFactor.class);

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

        // If the neighbor inactive, then there's nothing to do.
        if (active) {
            // ... but if its fixed to active, we need to account for that.
            final CardinalityFunction oldFunction = getFunction();
            setFunction(new CardinalityFunction() {
                @Override
                public double getCost(int nActiveVariables) {
                    return oldFunction.getCost(nActiveVariables+1);
                }
            });
        }

        // Finally remove the neighbor and notify it
        removeNeighbor(neighbor);
    }

    @Override
    public String toString() {
        return "Cardinality[" + getIdentity() + "]";
    }

}
