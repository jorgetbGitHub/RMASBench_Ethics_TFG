/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Helpers.Utility;

import RSLBench.Constants;
import RSLBench.Helpers.Distance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.Building;
import rescuecore2.worldmodel.EntityID;

/**
 * Utility function that mimicks the pre-utility functions evaluation.
 * 
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class FirstUtilityFunction extends AbstractUtilityFunction {
    private static final Logger Logger = LogManager.getLogger(FirstUtilityFunction.class);
    
    @Override
    public double getFireUtility(EntityID agent, EntityID target) {
        Building b = (Building) world.getEntity(target);
        double f = b.getFieryness();
        double utility = 1.0;
        if (f == 1.0) {
            utility = 1E9;
        } else if (f == 2.0) {
            utility = 1E6;
        } else if (f == 3.0) {
            utility = 100.0;
        }

        double distance = Distance.humanToBuilding(agent, target, world);
        double tradeoff = config.getFloatValue(Constants.KEY_UTIL_TRADEOFF);
        utility = utility / Math.pow(distance * tradeoff, 2.0);
        return utility;
    }

    @Override
    public int getRequiredAgentCount(EntityID target) {
        Building b = (Building) world.getEntity(target);
        
        int area = b.getTotalArea();
        double neededAgents = Math.ceil(area / config.getFloatValue(Constants.KEY_AREA_COVERED_BY_FIRE_BRIGADE));

        if (b.getFieryness() == 1) {
            neededAgents *= 1.5;
        } else if (b.getFieryness() == 2) {
            neededAgents *= 3.0;
        }
        //Logger.debugColor("BASE: " + base + " | FIERYNESS: " + b.getFieryness() + " | NEEEDED AGENTS: " + neededAgents, Logger.BG_RED);

        return (int) Math.round(neededAgents);
    }
    
}