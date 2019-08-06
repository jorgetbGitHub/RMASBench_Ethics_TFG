/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.DSA.scoring;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractScoringFunction implements ScoringFunction {

    private long ccs = 0;

    /**
     * Count a constraint check.
     */
    protected void CC() {
        ccs++;
    }

    @Override
    public long getCCs() {
        long result = ccs;
        ccs = 0;
        return result;
    }

}
