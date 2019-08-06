/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.BMS.factor;

import es.csic.iiia.bms.factors.StandardFactor;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BMSStandardFactor<T> extends StandardFactor<T> {
    private static final Logger Logger = LogManager.getLogger(BMSStandardFactor.class);

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

        // Now we need to trim the potential and fetch only that part where neighbor = active
        final double[] oldPotential = getPotential();
        Iterator<Integer> it = getTabularPotential().getIterator(neighbor, active);
        double[] newPotential = new double[oldPotential.length/2];
        for (int i=0; i<newPotential.length; i++) {
            newPotential[i] = oldPotential[it.next()];
        }

        // Remove the neighbor and update the potential
        removeNeighbor(neighbor);
        setPotential(newPotential);
    }

    @Override
    public String toString() {
        return "Standard[" + getIdentity() + "]";
    }

}
