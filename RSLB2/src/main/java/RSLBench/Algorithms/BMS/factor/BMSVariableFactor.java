/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.BMS.factor;

import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.factors.VariableFactor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BMSVariableFactor<T> extends VariableFactor<T> {
    private static final Logger Logger = LogManager.getLogger(BMSVariableFactor.class);

    private Double fixedValue = null;

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
        final MaxOperator op = getMaxOperator();
        final double value = active ? op.inverse().getWorstValue() : op.getWorstValue();

        if (fixedValue != null && value != fixedValue) {
            throw new RuntimeException("Attempted to fix a variable node to two different values!");
        }

        Logger.debug("{} now fixed to {}", this, active);
        fixedValue = value;
        removeNeighbor(neighbor);
    }

    @Override
    public long run() {
        if (fixedValue != null) {
            for (T neighbor : getNeighbors()) {
                send(fixedValue, neighbor);
            }
            return getNeighbors().size();
        }

        return super.run();
    }

    @Override
    public String toString() {
        return "Variable[" + getIdentity() + "]";
    }

}
