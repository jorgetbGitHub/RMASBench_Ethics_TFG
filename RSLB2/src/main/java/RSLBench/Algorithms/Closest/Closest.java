/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.Closest;

import RSLBench.Algorithms.Greedy.*;
import RSLBench.Assignment.DCOP.DCOPAgent;
import RSLBench.Assignment.DCOP.DCOPSolver;
import rescuecore2.standard.entities.StandardEntityURN;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Closest extends DCOPSolver {

    @Override
    protected DCOPAgent buildAgent(StandardEntityURN type) {
        switch(type) {
            case FIRE_BRIGADE:
                return new ClosestFireAgent();
            case POLICE_FORCE:
                return new ClosestPoliceAgent();
            default:
                throw new UnsupportedOperationException("The Closest solver does not support agents of type " + type);
        }
    }

    @Override
    public String getIdentifier() {
        return "Closest";
    }

}
