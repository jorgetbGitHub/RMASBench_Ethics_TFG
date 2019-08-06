/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.bms.factors;

import es.csic.iiia.bms.Factor;
import es.csic.iiia.bms.MaxOperator;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class ConditionedAtLeastOneFactorTest extends CrossFactorTestAbstract {

    @Override
    public Factor[] buildFactors(MaxOperator op, Factor[] neighbors) {
        Factor conditionNeighbor = neighbors[getRandomIntValue(neighbors.length)];
        return new Factor[] {
            buildSpecificFactor(op, neighbors, conditionNeighbor),
            buildStandardFactor(op, neighbors, conditionNeighbor)
        };
    }

    private Factor buildSpecificFactor(MaxOperator op, Factor[] neighbors, Factor conditionNeighbor) {
        ConditionedAtLeastOneFactor factor = new ConditionedAtLeastOneFactor();
        factor.setMaxOperator(op);
        factor.setConditionNeighbor(conditionNeighbor);
        link(factor, neighbors);
        return factor;
    }

    private Factor buildStandardFactor(MaxOperator op, Factor[] neighbors, Factor conditionNeighbor) {
        StandardFactor factor = new StandardFactor();
        factor.setMaxOperator(op);
        link(factor, neighbors);
        factor.setPotential(buildPotential(op, neighbors, conditionNeighbor));
        return factor;
    }

    public double[] buildPotential(MaxOperator op, Factor[] neighbors, Factor conditionNeighbor) {
        final int nNeighbors = neighbors.length;

        // Find the condition neighbor index
        int conditionIdx = -1;
        for (int i = 0; i < nNeighbors; i++) {
            if (neighbors[i] == conditionNeighbor) {
                conditionIdx = nNeighbors - i -1;
                break;
            }
        }

        double[] values = new double[1 << nNeighbors];
        final int conditionMask = 1 << conditionIdx;
        for (int configurationIdx = 0; configurationIdx < values.length; configurationIdx++) {
            final int bitCount = Integer.bitCount(configurationIdx);

            if (bitCount == 0) {
                // condition == 0, dependents == 0
                values[configurationIdx] = 0;
            } else if (bitCount == 1) {
                // either condition = 1, dependents = 0 or vice-versa
                values[configurationIdx] = op.getWorstValue();
            } else if ((conditionMask & configurationIdx) > 0) {
                // condition = 1, dependents >= 1
                values[configurationIdx] = 0;
            } else {
                // condition = 0, dependents > 1
                values[configurationIdx] = op.getWorstValue();
            }
        }

        return values;
    }

}