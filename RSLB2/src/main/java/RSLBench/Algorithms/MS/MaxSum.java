/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.MS;

import RSLBench.Algorithms.BMS.BinaryMaxSum;
import RSLBench.Assignment.DCOP.DCOPAgent;
import RSLBench.Assignment.DCOP.DCOPSolver;
import RSLBench.Constants;
import java.util.List;
import rescuecore2.standard.entities.StandardEntityURN;
import static rescuecore2.standard.entities.StandardEntityURN.FIRE_BRIGADE;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MaxSum extends DCOPSolver {

    public static final String KEY_MAXSUM_DAMPING = BinaryMaxSum.KEY_MAXSUM_DAMPING;

    @Override
    protected DCOPAgent buildAgent(StandardEntityURN type) {
        final boolean team = config.getBooleanValue(Constants.KEY_INTERTEAM_COORDINATION);
        if (team) {
            throw new UnsupportedOperationException("The Max-Sum algorithm does not support coordinated teams yet.");
        }

        switch(type) {
            case FIRE_BRIGADE:
                return new MSAgent();
            default:
                throw new UnsupportedOperationException("The Max-Sum solver does not support agents of type " + type);
        }
    }

    @Override
    public String getIdentifier() {
        return "MaxSum";
    }

    @Override
    public List<String> getUsedConfigurationKeys() {
        List<String> result = super.getUsedConfigurationKeys();
        result.add(KEY_MAXSUM_DAMPING);
        return result;
    }

}