/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.DSA;

import RSLBench.Assignment.DCOP.DCOPAgent;
import RSLBench.Assignment.DCOP.DCOPSolver;
import RSLBench.Constants;
import java.util.List;
import rescuecore2.standard.entities.StandardEntityURN;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class DSA extends DCOPSolver {

    /**
     * The probability that an agent changes his assigned target.
     */
    public static final String KEY_DSA_PROBABILITY = "dsa.probability";

    /**
     * How the algorithm chooses the initial target for the algorithm.
     */
    public static final String KEY_DSA_INITIAL_TARGET = "dsa.initial_target";

    /**
     * Choose the best individual target as the starting one.
     * @see #KEY_DSA_INITIAL_TARGET
     */
    public static final String TARGET_BEST = "best";

    /**
     * Choose the initial target randomly.
     * @see #KEY_DSA_INITIAL_TARGET
     */
    public static final String TARGET_RANDOM = "random";

    /**
     * Choose the target selected in the last iteration.
     * @see #KEY_DSA_INITIAL_TARGET
     */
    public static final String TARGET_LAST = "last";

    @Override
    public String getIdentifier() {
        return "DSA";
    }

    @Override
    protected DCOPAgent buildAgent(StandardEntityURN type) {
        final boolean team = config.getBooleanValue(Constants.KEY_INTERTEAM_COORDINATION);

        switch(type) {
            case FIRE_BRIGADE:
                return team ? new DSATeamFireAgent() : new DSAFireAgent();
            case POLICE_FORCE:
                return team ? new DSATeamPoliceAgent() : new DSAPoliceAgent();
            default:
                throw new UnsupportedOperationException("The DSA solver does not support agents of type " + type);
        }
    }

    @Override
    public List<String> getUsedConfigurationKeys() {
        List<String> keys = super.getUsedConfigurationKeys();
        keys.add(KEY_DSA_PROBABILITY);
        keys.add(KEY_DSA_INITIAL_TARGET);
        return keys;
    }



}