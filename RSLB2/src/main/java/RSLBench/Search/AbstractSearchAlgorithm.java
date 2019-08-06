/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class AbstractSearchAlgorithm implements SearchAlgorithm {
    private static final Logger Logger = LogManager.getLogger(AbstractSearchAlgorithm.class);

    @Override
    public SearchResults search(EntityID start, EntityID goal, Graph graph, DistanceInterface distanceMatrix) {
        Area init = getArea(graph, start);
        List<Area> ends = toAreas(graph, goal);
        SearchResults result = search(init, ends, graph, distanceMatrix);
        return result;
    }

    public abstract SearchResults search(Area start, Collection<Area> goals, Graph graph, DistanceInterface distanceMatrix);

    @Override
    public SearchResults search(EntityID start, Collection<EntityID> goals, Graph graph, DistanceInterface distanceMatrix) {
        Area init = getArea(graph, start);
        List<Area> ends = toAreas(graph, goals);
        SearchResults result = search(init, ends, graph, distanceMatrix);
        return result;
    }

    protected Area getArea(Graph graph, EntityID id) {
        StandardEntity entity = graph.getWorld().getEntity(id);
        if (entity instanceof Area) {
            return (Area)entity;
        }
        Logger.error("Entity {} is not an area.", entity);
        throw new RuntimeException("Not an area.");
    }

    private List<EntityID> toEntityIDs(List<Area> path) {
        List<EntityID> idpath = new ArrayList<>(path.size());
        for (Area a : path) {
            idpath.add(a.getID());
        }
        return idpath;
    }

    private List<Area> toAreas(Graph graph, Collection<EntityID> goals) {
        List<Area> ends = new ArrayList<>(goals.size());
        for (EntityID goal : goals) {
            ends.add(getArea(graph, goal));
        }
        return ends;
    }

    private List<Area> toAreas(Graph graph, EntityID goal) {
        List<Area> ends = new ArrayList<>(1);
        ends.add(getArea(graph, goal));
        return ends;
    }

    protected void addBlockers(Graph graph, List<Blockade> blockers, Area zone) {
        // Check whether the path is blocked
        if (zone.isBlockadesDefined()) {
            Logger.debug("Blockades detected in {}.", zone);
            for (EntityID b : zone.getBlockades()) {
                Blockade blockade = (Blockade)(graph.getWorld().getEntity(b));
                Logger.trace("Blockade {} (cost: {})", blockade, blockade.getRepairCost());
                if (blockade.getRepairCost() > 0) {
                    blockers.add(blockade);
                }
            }
        }
    }

}
