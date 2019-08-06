package RSLBench.Search;

import RSLBench.Helpers.Logging.Markers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.worldmodel.EntityID;


public class AStar extends AbstractSearchAlgorithm
{
    private static final Logger Logger = LogManager.getLogger(AStar.class);

    @Override
    public SearchResults search(Area start, Collection<Area> goals, Graph graph, DistanceInterface distanceMatrix)
    {
        Logger.debug(Markers.GREEN, "start multi target search");

        PriorityQueue<SearchNode> openList = new PriorityQueue<>();
        Map<Area, SearchNode> closedList = new HashMap<>();

        // reverse search: add all goals to the open list
        for (Area id: goals)
        {
            int heuristicValue = distanceMatrix.getDistance(id.getID(), start.getID());
            openList.add(new SearchNode(id, null, 0, heuristicValue));
        }

        SearchNode currentNode = null;
        boolean searchComplete = false;
        Set<Area> neighbors;
        while (! openList.isEmpty() && ! searchComplete)
        {
            currentNode = openList.remove();
            if (currentNode.getNodeID().equals(start))
            {
                searchComplete = true;
                break;
            }

            // check if this is closed
            if (closedList.containsKey(currentNode.getNodeID()))
            {
                // check distance of the previous path found to this node
                SearchNode previousNode = closedList.get(currentNode.getNodeID());
                if (previousNode.getPathLength() > currentNode.getPathLength())
                {
                    continue;
                }
            }

            // put current node on close list
            closedList.put(currentNode.getNodeID(), currentNode);

            // expand node
            neighbors = graph.getNeighbors(currentNode.getNodeID());
            for (Area id: neighbors)
            {
                if (! closedList.containsKey(id))
                {
                    // if this neighbor is not closed, add it to the open list
                    int distanceToCurrentNode = distanceMatrix.getDistance(id.getID(), currentNode.getNodeID().getID());
                    int heuristicValue = distanceMatrix.getDistance(id.getID(), start.getID());
                    openList.add(new SearchNode(id, currentNode, distanceToCurrentNode, heuristicValue));
                }
            }
        }
        if (! searchComplete)
        {
            // no path found
            Logger.debug(Markers.RED, "no path found");
            return null;
        }

        // construct the path
        List<EntityID> pathEntities = new ArrayList<>();
        List<Blockade> blockers = new ArrayList<>();
        while (currentNode.getParent() != null)
        {
            Area current = currentNode.getNodeID();
            pathEntities.add(current.getID());
            addBlockers(graph, blockers, current);

            currentNode = currentNode.getParent();
        }
//        Logger.debugColor("path found. length: "+path.size(), Logger.BG_GREEN);

        Collections.reverse(pathEntities);
        Collections.reverse(blockers);

        SearchResults result = new SearchResults();
        result.setPathIds(pathEntities);
        result.setPathBlocks(blockers);

        return result;
    }

    @Override
    public SearchResults search(Area start, Collection<Area> goals, Graph graph, DistanceInterface distanceMatrix, Collection<EntityID> excludedAreas) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
