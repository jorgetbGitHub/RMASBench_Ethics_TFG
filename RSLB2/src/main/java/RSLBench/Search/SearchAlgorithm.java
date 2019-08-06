package RSLBench.Search;

import java.util.Collection;
import rescuecore2.standard.entities.Area;

import rescuecore2.worldmodel.EntityID;

public interface SearchAlgorithm
{
    /**
     * Do a search from one location to the closest of a set of goals.
     *
     * @param start
     *            The location we start at.
     * @param goal
     *            The goals we want to reach.
     * @return The path from start to one of the goals, or null if no path can
     *         be found.
     * @param graph
     *            a connectivity graph of all the places in the world
     * @param distanceMatrix
     *            A matrix containing the pre-computed distances between each
     *            two entities in the world.
     * @return The path from start to one of the goals, or null if no path can be found.
     */
    public SearchResults search(EntityID start, EntityID goal,
            Graph graph, DistanceInterface distanceMatrix);

    /**
     * Do a search from one location to the closest of a set of goals.
     *
     * @param start
     *            The location we start at.
     * @param goals
     *            The set of possible goals.
     * @return The path from start to one of the goals, or null if no path can
     *         be found.
     * @param graph
     *            a connectivity graph of all the places in the world
     * @param distanceMatrix
     *            A matrix containing the pre-computed distances between each
     *            two entities in the world.
     * @return The path from start to one of the goals, or null if no path can be found.
     */
    public SearchResults search(EntityID start, Collection<EntityID> goals,
            Graph graph, DistanceInterface distanceMatrix);
    
    
    /**
     * Search with exclusion of certain areas to look for alternative paths, or path with constraints
     * @param start
     * @param goals
     * @param graph
     * @param distanceMatrix
     * @param excludedAreas
     * @return 
     */
    //Included
    public SearchResults search(Area start, Collection<Area> goals,
            Graph graph, DistanceInterface distanceMatrix, Collection<EntityID> excludedAreas);
}
