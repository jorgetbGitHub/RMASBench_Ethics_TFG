/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Helpers.Utility;

import RSLBench.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 * Utility function that mimicks the pre-utility functions evaluation.
 * 
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class IgnoreFieryness extends AbstractUtilityFunction {
    private static final Logger Logger = LogManager.getLogger(IgnoreFieryness.class);

    @Override
    public double getFireUtility(EntityID agent, EntityID target) {
        double distance = world.getDistance(agent, target);
        return 1e12 / Math.pow(distance, 2);
        //return 100.0 / Math.pow(distance * Params.TRADE_OFF_FACTOR_TRAVEL_COST_AND_UTILITY, 2.0);
    }

    @Override
    public int getRequiredAgentCount(EntityID target) {
        StandardEntity e = world.getEntity(target);
        if (e == null || !(e instanceof Building)) {
            Logger.error("Requested the agent count of a non-building target.");
            System.exit(1);
        }
        
        Building b = (Building)e;
        if (!b.isOnFire()) {
            return 0;
        }
        
        int area = b.getTotalArea();
        double neededAgents = Math.ceil(area / config.getFloatValue(Constants.KEY_AREA_COVERED_BY_FIRE_BRIGADE));
        return (int) Math.round(neededAgents);
    }
    
}