package RSLBench.Search;

import RSLBench.PlatoonPoliceAgent;
import RSLBench.PlatoonFireAgent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;


import rescuecore2.worldmodel.EntityID;

public class BreadthFirstSearch extends AbstractSearchAlgorithm
{
    private static final Logger Logger = LogManager.getLogger(BreadthFirstSearch.class);

    @Override
    public SearchResults search(Area start, Collection<Area> goals, Graph graph, DistanceInterface distanceMatrix)
    {
        List<Area> open = new LinkedList<>();
        Map<Area, Area> ancestors = new HashMap<>();
        open.add(start);
        Area next = null;
        Area goal = null;
        boolean found = false;
        ancestors.put(start, start);
        do
        {
            next = open.remove(0);

            if (isGoal(next, goals)) {

                goal = next;
                found = true;
                break;
            }

            Collection<Area> neighbours = graph.getNeighbors(next);
            if (neighbours.isEmpty()) {
                continue;
            }

            for (Area neighbour : neighbours) {

                if (isGoal(neighbour, goals)) {
                    ancestors.put(neighbour, next);
                    next = neighbour;
                    found = true;
                    break;
                } else {
                    if (!ancestors.containsKey(neighbour)) {
                        open.add(neighbour);
                        ancestors.put(neighbour, next);
                    }
                }
            }

        } while (!found && !open.isEmpty());

        if (!found) {
            // No path
            return null;
        }

        // Walk back from goal to start
        Area current = next;
        SearchResults result = new SearchResults();
        List<Blockade> blockers = new ArrayList<>();
        List<Area> path = new ArrayList<>();
        List<EntityID> entityPath = new ArrayList<>();
        do
        {
            path.add(current);
            entityPath.add(current.getID());
            addBlockers(graph, blockers, current);

            current = ancestors.get(current);
            if (current == null)
            {
                throw new RuntimeException("Found a node with no ancestor! Something is broken.");
            }
        } while (current != start);
        addBlockers(graph, blockers, start);

        Collections.reverse(path);
        Collections.reverse(entityPath);
        Collections.reverse(blockers);
        
        // START Debug information
        
        /*String debugPath = "";
        int cont = 0;
        debugPath += "*** Neighboors of the start point (Building) ***\n";
        Set<Area> startNeighboors = graph.getNeighbors(start);
        for (Area area: startNeighboors) {
            debugPath += "[Neighboor] ";
            debugPath += "[" + String.valueOf(cont + 1) + "] "; 
            if (area instanceof Road) {
                debugPath += "Road ";
            }
            if (area instanceof Building) {
                debugPath += "Building ";
            }
            debugPath += area.getFullDescription();
            debugPath += "\n";
            cont++;
        }
        
        debugPath += "\n";
        cont = 0;
        for (Blockade block : blockers) {
            debugPath += "[Blockades] ";
            debugPath += "[" + String.valueOf(cont + 1) + "] ";
            debugPath += block.getFullDescription();
            debugPath += " (entity associated with position attribute) -> " + graph.getWorld().getEntity(block.getPosition()).getFullDescription() + " \n";
            cont++;
        }
        
        debugPath += "\n";
        cont = 0;
        for (Area area : path) {
            debugPath += "[Path] ";
            debugPath += "[" + String.valueOf(cont + 1) + "] "; 
            if (area instanceof Road) {
                debugPath += "Road ";
            }
            if (area instanceof Building) {
                debugPath += "Building ";
            }
            debugPath += area.getFullDescription();
            debugPath += "\n";
            cont++;
        }
        
        
        if ( goal != null) {
            Logger.debug(start.getFullDescription() +
                "The path computed from the start " + " to the goal " + goal.getFullDescription() + "Full Path information: " + debugPath);
        }else {
            Logger.debug(start.getFullDescription() + 
                "The path computed from the start " + " to the goal NULL  " + "Full Path information: " + debugPath);
        }
        
        
        if ( goal != null) {
            Logger.debug(start.getFullDescription() +
                "The path computed from the start " + " to the goal " + goal.getFullDescription() + "Full Path information: " + debugPath);
        }else {
            Logger.debug(start.getFullDescription() + 
                "The path computed from the start " + " to the goal NULL  " + "Full Path information: " + debugPath);
        }
        
        // END debug information
        */

        result.setPathIds(entityPath);
        result.setPathBlocks(blockers);
        return result;
    }
    
    @Override
    public SearchResults search(Area start, Collection<Area> goals, Graph graph, DistanceInterface distanceMatrix, Collection<EntityID> excludedAreas)
    {
        try {
            List<Area> open = new LinkedList<>();
            Map<Area, Area> ancestors = new HashMap<>();
            open.add(start);
            Area next = null;
            Area goal = null;
            boolean found = false;
            ancestors.put(start, start);
            do
            {
                next = open.remove(0);

                if (isGoal(next, goals)) {

                    goal = next;
                    found = true;
                    break;
                }

                Collection<Area> neighbours = graph.getNeighbors(next);
                if (neighbours.isEmpty()) {
                    continue;
                }

                for (Area neighbour : neighbours) {

                    if (isGoal(neighbour, goals)) {
                        ancestors.put(neighbour, next);
                        next = neighbour;
                        found = true;
                        break;
                    } else {
                        if (!ancestors.containsKey(neighbour) && !isExcludedArea(neighbour, excludedAreas)) {
                            open.add(neighbour);
                            ancestors.put(neighbour, next);
                        }
                    }
                }

            } while (!found && !open.isEmpty());

            if (!found) {
                // No path
                return null;
            }

            // Walk back from goal to start
            Area current = next;
            SearchResults result = new SearchResults();
            List<Blockade> blockers = new ArrayList<>();
            List<Area> path = new ArrayList<>();
            List<EntityID> entityPath = new ArrayList<>();
            do
            {
                path.add(current);
                entityPath.add(current.getID());
                addBlockers(graph, blockers, current);

                current = ancestors.get(current);
                if (current == null)
                {
                    throw new RuntimeException("Found a node with no ancestor! Something is broken.");
                }
            } while (current != start);
            addBlockers(graph, blockers, start);

            Collections.reverse(path);
            Collections.reverse(entityPath);
            Collections.reverse(blockers);

            // START Debug information

            String debugPath = "";
            int cont = 0;
            debugPath += "*** Neighboors of the start point (Building) ***\n";
            Set<Area> startNeighboors = graph.getNeighbors(start);
            for (Area area: startNeighboors) {
                debugPath += "[Neighboor] ";
                debugPath += "[" + String.valueOf(cont + 1) + "] "; 
                if (area instanceof Road) {
                    debugPath += "Road ";
                }
                if (area instanceof Building) {
                    debugPath += "Building ";
                }
                debugPath += area.getFullDescription();
                debugPath += "\n";
                cont++;
            }

            debugPath += "\n";
            cont = 0;
            for (Blockade block : blockers) {
                debugPath += "[Blockades] ";
                debugPath += "[" + String.valueOf(cont + 1) + "] ";
                debugPath += block.getFullDescription();
                debugPath += " (entity associated with position attribute) -> " + graph.getWorld().getEntity(block.getPosition()).getFullDescription() + " \n";
                cont++;
            }

            debugPath += "\n";
            cont = 0;
            for (Area area : path) {
                debugPath += "[Path] ";
                debugPath += "[" + String.valueOf(cont + 1) + "] "; 
                if (area instanceof Road) {
                    debugPath += "Road ";
                }
                if (area instanceof Building) {
                    debugPath += "Building ";
                }
                debugPath += area.getFullDescription();
                debugPath += "\n";
                cont++;
            }

            // END debug information

            if ( goal != null) {
                Logger.debug(start.getFullDescription() +
                    "The path computed from the start " + " to the goal " + goal.getFullDescription() + "Full Path information: " + debugPath);
            }else {
                Logger.debug(start.getFullDescription() + 
                    "The path computed from the start " + " to the goal NULL  " + "Full Path information: " + debugPath);
            }

            result.setPathIds(entityPath);
            result.setPathBlocks(blockers);
            return result;
        }
        catch (Exception ex) {
            Logger.info("[Search path with exclusion failed] Err: " + ex.getMessage());
            return null;
        }
    }

    private boolean isGoal(Area e, Collection<Area> test)
    {
        return test.contains(e);
    }
    
    //Included
    private boolean isExcludedArea(Area e, Collection<EntityID> excludedAreas) {
        return excludedAreas.contains(e.getID());
    }
}
