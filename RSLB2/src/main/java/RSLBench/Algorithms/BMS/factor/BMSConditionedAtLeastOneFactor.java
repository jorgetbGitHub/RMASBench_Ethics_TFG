/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.BMS.factor;

import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.factors.ConditionedSelectorFactor;
import es.csic.iiia.bms.util.BestValuesTracker;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BMSConditionedAtLeastOneFactor<T> extends ConditionedSelectorFactor<T> {
    private static final Logger Logger = LogManager.getLogger(BMSVariableFactor.class);

    private enum Mode {
        NORMAL, FIXED, ATLEASTONE, ANYTHING
    }
    private Mode mode = Mode.NORMAL;

    private List<Double> fixedValues = null;

    @Override
    public boolean removeNeighbor(T neighbor) {
        if (fixedValues != null) {
            int index = getNeighbors().indexOf(neighbor);
            if (index >= 0) {
                fixedValues.remove(index);
            }
        }
        return super.removeNeighbor(neighbor);
    }

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

        switch(mode) {
            case FIXED:
                reduceFixed(neighbor, active);
                break;

            case ATLEASTONE:
                reduceAtLeastOne(neighbor, active);
                break;

            case ANYTHING:
                // Nothing to be done here
                break;

            case NORMAL:
            default:
                reduceNormal(neighbor, active);
        }

        // In any case, we need to remove the neighbor
        Logger.debug("{} removes neighbor {}", this, neighbor);
        removeNeighbor(neighbor);
    }

    private void reduceNormal(T neighbor, boolean active) {
        // There are 2 cases when fixing the condition variable:
        if (neighbor.equals(getConditionNeighbor())) {
            // 1. The factor turns into an AtLeastOne
            if (active) {
                Logger.debug("{} turns into a selector", this);
                mode = Mode.ATLEASTONE;
                return;
            }

            // 2. The factor turns into an "allZeros"
            fixAllZeros();
            return;
        }

        // There are 3 cases when fixing a conditioned variable:

        // 1. The variable is fixed to on => the factor turns into an "anything" factor
        if (active) {
            mode = Mode.ANYTHING;
            return;
        }

        // 2. The variable is fixed to off *and* it is the latest conditioned variable, so
        //    the factor turns into allzeros.
        if (getNeighbors().size() == 2) {
            fixAllZeros();
        }

        // 3. The factor stays as-is but with one less neighbor
    }

    private void fixAllZeros() {
        final double inactiveValue = getMaxOperator().getWorstValue();
        final int nNeighbors = getNeighbors().size();

        mode = Mode.FIXED;
        fixedValues = new ArrayList<>(nNeighbors);
        for (int i=0; i<nNeighbors; i++) {
            fixedValues.add(inactiveValue);
        }
        Logger.debug("{} turns into an AllZeroes", this);
    }

    private void fixSelected(T neighbor) {
        final MaxOperator op = getMaxOperator();

        mode = Mode.FIXED;
        fixedValues = new ArrayList<>(getNeighbors().size());
        for (T n : getNeighbors()) {
            if (n.equals(neighbor) || n.equals(getConditionNeighbor())) {
                fixedValues.add(op.inverse().getWorstValue());
            } else {
                fixedValues.add(op.getWorstValue());
            }
        }
        Logger.debug("{} activates the condition {} and conditioned {}", this, getConditionNeighbor(), neighbor);
    }

    private void reduceAtLeastOne(T neighbor, boolean active) {
        // If the neighbor is fixed to active, we turn into an "anything" factor
        if (active) {
            Logger.debug("{} activates variable {}", this, neighbor);
            mode = Mode.ANYTHING;
            return;
        }

        // Otherwise, 3 things may happen:
        final int remainingNeighbors = getNeighbors().size()-1;

        // 1. There are no remaining neighbors, so this is an error
        if (remainingNeighbors == 0) {
            throw new RuntimeException("This AtLeastOne is left out of possible variables to activate!");
        }

        // 2. Only one neighbor remains, so we require it to be fixed to active
        if (remainingNeighbors == 1) {
            final int remainingCandidate = (getNeighbors().indexOf(neighbor)+1)%2;
            T candidate = getNeighbors().get(remainingCandidate);
            Logger.debug("{} fixes neighbor {} to active because its the only remaining candidate", this, candidate);
            fixSelected(candidate);
        }

        // 3. Two or more neighbors remain, so we keep operating normally
    }

    private void reduceFixed(T neighbor, boolean active) {
        final MaxOperator op = getMaxOperator();

        // Safety check: the neighbor must be fixed to the value we have already chosen
        double desiredValue = active ? op.inverse().getWorstValue() : op.getWorstValue();
        if (fixedValues.get(getNeighbors().indexOf(neighbor)) != desiredValue) {
            throw new RuntimeException("Neighbor " + neighbor + " fixed to both active and inactive!");
        }
    }

    @Override
    public long run() {
        switch(mode) {
            case ATLEASTONE:
                return runAtLeastOne();

            case FIXED:
                return runFixed();

            case ANYTHING:
                return runAnything();

            case NORMAL:
            default:
                return super.run();
        }
    }

    private long runAtLeastOne() {
        // Compute the minimums
        BestValuesTracker<T> tracker = new BestValuesTracker<>(getMaxOperator());
        tracker.reset();
        for (T f : getNeighbors()) {
            tracker.track(f, getMessage(f));
        }

        // Send messages
        for (T f : getNeighbors()) {
            final double best = - tracker.getComplementary(f);
            final double value = getMaxOperator().max(0, best) - best;
            send(value, f);
        }

        return getNeighbors().size()*2;
    }

    private long runAnything() {
        for (T f : getNeighbors()) {
            send(0, f);
        }
        return getNeighbors().size();
    }

    private long runFixed() {
        for (int i=0; i<getNeighbors().size(); i++) {
            send(fixedValues.get(i), getNeighbors().get(i));
        }
        return getNeighbors().size();
    }

    @Override
    public String toString() {
        return "ConditionedSelector[" + getIdentity() + "]";
    }

}
