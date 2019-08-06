package RSLBench.Search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rescuecore2.misc.collections.LazyMap;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;

public final class Graph
{
    private Map<Area, Set<Area>> graph;
    private StandardWorldModel world;

    private static final Map<StandardWorldModel, Graph> instanceMap = new HashMap<>();

    public static Graph getInstance(StandardWorldModel world) {
        if (!instanceMap.containsKey(world)) {
            instanceMap.put(world, new Graph(world));
        }
        return instanceMap.get(world);
    }

    private Graph(StandardWorldModel world) {
        this.world = world;
        graph = new LazyMap<Area, Set<Area>>()
        {
            @Override
            public Set<Area> createValue()
            {
                return new HashSet<>();
            }
        };
        for (Entity next : world)
        {
            if (!(next instanceof Area)) {
                continue;
            }

            Area area = (Area)next;
            for (EntityID neighbor : area.getNeighbours()) {
                StandardEntity entity = world.getEntity(neighbor);
                graph.get(area).add((Area) entity);
            }
        }
    }

    /**
     * Retrieves all neighbors of a specific node in the graph.
     *
     * @param id
     *            the entity id of the node.
     * @return set containing the entity ids of all neighbor nodes.
     */
    public Set<Area> getNeighbors(Area id)
    {
        return graph.get(id);
    }

    public Set<Area> getNeighbors(EntityID id) {
        StandardEntity entity = world.getEntity(id);
        if (!(entity instanceof Area)) {
            throw new RuntimeException("Requested neighbors of a non-area entity " + id);
        }

        return getNeighbors((Area)entity);
    }

    public StandardWorldModel getWorld() {
        return world;
    }

}
