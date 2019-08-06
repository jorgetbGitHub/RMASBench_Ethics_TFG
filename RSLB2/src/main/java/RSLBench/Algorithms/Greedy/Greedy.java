/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.Greedy;

import RSLBench.Assignment.DCOP.DCOPAgent;
import RSLBench.Assignment.DCOP.DCOPSolver;
import rescuecore2.standard.entities.StandardEntityURN;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Greedy extends DCOPSolver {

    @Override
    protected DCOPAgent buildAgent(StandardEntityURN type) {
        switch(type) {
            case FIRE_BRIGADE:
                return new GreedyFireAgent();
            case POLICE_FORCE:
                return new GreedyPoliceAgent();
            default:
                throw new UnsupportedOperationException("The Greedy solver does not support agents of type " + type);
        }
    }

    @Override
    public String getIdentifier() {
        return "Greedy";
    }

}
