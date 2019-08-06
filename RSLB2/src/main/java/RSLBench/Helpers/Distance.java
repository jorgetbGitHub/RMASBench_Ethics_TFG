/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Helpers;

import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Distance {

    public static double humanToBuilding(EntityID agent, EntityID target, StandardWorldModel model) {
        Human hagent = (Human)model.getEntity(agent);
        EntityID position = hagent.getPosition();
        return model.getDistance(agent, target);
    }

    public static double humanToBlockade(EntityID agent, EntityID target, StandardWorldModel model, double threshold) {
        Human hagent = (Human)model.getEntity(agent);
        Blockade blockade = (Blockade)model.getEntity(target);
        if (inRange(hagent, blockade, threshold)) {
            return 0;
        }
        EntityID position2 = blockade.getPosition();
        return model.getDistance(agent, position2);
    }

    private static boolean inRange(Human human, Blockade target, double range) {
        Point2D agentLocation = new Point2D(human.getX(), human.getY());
        for (Line2D line : GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(target.getApexes()), true)) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, agentLocation);
            double distance = GeometryTools2D.getDistance(agentLocation, closest);
            if (distance < range) {
                return true;
            }
        }
        return false;
    }

}
