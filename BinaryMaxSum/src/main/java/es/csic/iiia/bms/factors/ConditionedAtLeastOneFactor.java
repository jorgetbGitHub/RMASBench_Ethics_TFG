/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013-2014 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.bms.factors;

import es.csic.iiia.bms.MaxOperator;
import es.csic.iiia.bms.util.BestValuesTracker;
import java.util.Map;

/**
 * Conditioned AtLeastOne factor.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class ConditionedAtLeastOneFactor<T> extends AbstractFactor<T> {

    private BestValuesTracker<T> tracker;

    private T conditionNeighbor;

    @Override
    public void setMaxOperator(MaxOperator maxOperator) {
        super.setMaxOperator(maxOperator);
        tracker = new BestValuesTracker<T>(maxOperator);
    }

    /**
     * Get the neighbor that represents the conditional variable.
     *
     * @return the Neighbor that represents the conditional variable.
     */
    public T getConditionNeighbor() {
        return conditionNeighbor;
    }

    /**
     * Set the neighbor that represents the conditional variable.
     *
     * @param condition conditional variable.
     */
    public void setConditionNeighbor(T condition) {
        this.conditionNeighbor = condition;
    }

    @Override
    protected double eval(Map<T, Boolean> values) {
        final MaxOperator op = getMaxOperator();
        final boolean active = values.get(conditionNeighbor);

        int nActiveNeighbors = 0;
        for (T neighbor : getNeighbors()) {
            if (neighbor.equals(conditionNeighbor)) {
                continue;
            }

            if (values.get(neighbor)) {
                nActiveNeighbors++;
            }
        }

        if (active && nActiveNeighbors > 0 ) {
            return 0;
        }
        if (!active && nActiveNeighbors == 0) {
            return 0;
        }

        return op.getWorstValue();
    }

    @Override
    public long run() {
        final MaxOperator op = getMaxOperator();

        // Compute the maximums between the dependent variables and the cumulative sum of positive
        // messages
        double sum = 0;
        tracker.reset();
        for (T f : getNeighbors()) {
            // Skip the condition neighbor
            if (f.equals(conditionNeighbor)) {
                continue;
            }

            final double message = getMessage(f);
            tracker.track(f, message);
            sum += op.max(0, message);
        }

        // Send messages
        for (T f : getNeighbors()) {
            if (f.equals(conditionNeighbor)) {
                sendMessageToConditionNeighbor(f, sum);
            } else {
                sendMessageToDependentNeighbor(f, sum);
            }
        }

        return getNeighbors().size()*2;
    }

    private void sendMessageToConditionNeighbor(T neighbor, double sum) {
        final double best = tracker.getBestValue();
        final double message = best + exclude(sum, best);
        send(message, neighbor);
    }

    private void sendMessageToDependentNeighbor(T neighbor, double sum) {
        final MaxOperator op = getMaxOperator();
        final double msg = getMessage(neighbor);
        final double v_star = tracker.getComplementary(neighbor);
        final double v_c = getMessage(conditionNeighbor);

        final double mu_0 = op.max(0, v_c + v_star + exclude(sum, v_star, msg));
        final double mu_1 = v_c + exclude(sum, msg);
        send(mu_1 - mu_0, neighbor);
    }

    private double exclude(double sum, double value) {
        final MaxOperator op = getMaxOperator();
        return sum - op.max(0, value);
    }

    private double exclude(double sum, double v1, double v2) {
        return exclude(exclude(sum, v1), v2);
    }

}
