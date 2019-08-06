package RSLBench;

import RSLBench.Assignment.Assignment;
import java.util.Collection;
import java.util.List;

import rescuecore2.messages.Command;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import RSLBench.Helpers.Logging.Markers;
import RSLBench.Search.SearchResults;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.misc.geometry.GeometryTools2D;
import rescuecore2.misc.geometry.Line2D;
import rescuecore2.misc.geometry.Point2D;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Road;



/**
 * A sample fire brigade agent.
 */
public class PlatoonPoliceAgent extends PlatoonAbstractAgent<PoliceForce>
{
    private static final Logger Logger = LogManager.getLogger(PlatoonPoliceAgent.class);

    public static final String DISTANCE_KEY = "clear.repair.distance";

    private int range;
    
    //Included N185
    public EntityID roadToFocus_N185;
    public boolean N185;
    public List<EntityID> blockadesToPrioritize_N185 = new ArrayList<>();
    
    //Included N194
    public EntityID roadToFocus_N194;
    public boolean N194;
    public List<EntityID> blockadesToPrioritize_N194 = new ArrayList<>();
    
    //Included N206
    public EntityID mostDamagedBuilding_N206;
    public boolean N206;
    
    //Included N218
    public EntityID buildingToFocus_N218;
    public boolean N218;
    
    //Included N221
    public EntityID buildingToFocus_N221;
    public boolean N221;
    
    //Included N233
    public EntityID blockadeToFocus_N233;
    public boolean N233;
    
    
    //Included N241
    public EntityID blockadeToFocus_N241;
    public List<EntityID> blockadesToPrioritize_N241 = new ArrayList<>();
    
    //Included N242
    public boolean N242;
    
    //Included N244
    public EntityID blockadeToFocus_N244;
    public List<EntityID> blockadesToPrioritize_N244 = new ArrayList<>();
    
    //Included N245
    public boolean N245;

    /** EntityID of the road where the blockade that this agent should remove is located */
    private EntityID assignedTarget = Assignment.UNKNOWN_TARGET_ID;
    
    private Pair<EntityID, Integer> lastAssignedTarget = null;
    
    public Norm currentNorm = null;
    
    private List<Pair<EntityID, Norm>> listTargets;
    
    //Pair.first() = blockade position, Pair.second() = cost [Analytics purposes]
    public List<Pair<EntityID, Integer>> clearedBlockades = new ArrayList<>();
    
    //Pair.first() = (startPosition, endPosition Pair.second() = norm [Analytics purposes]
    public List<Pair<Pair<EntityID, EntityID>, Norm>> clearedPaths = new ArrayList<>();
    

    public PlatoonPoliceAgent() {
    	Logger.debug(Markers.BLUE, "Platoon Police Agent CREATED");
        
        roadToFocus_N185 = null;
        roadToFocus_N194 = null;
        mostDamagedBuilding_N206 = null;
        buildingToFocus_N218 = null;
        buildingToFocus_N221 = null;
        blockadeToFocus_N233 = null;
        blockadeToFocus_N241 = null;
        blockadeToFocus_N244 = null;
        
        Random rand = new Random();
        
        N185 = rand.nextDouble() <= 0.5; //This agent will follow N185
        N194 = rand.nextDouble() <= 0.5; //This agent will follow N194
        N206 = rand.nextDouble() <= 0.5; //This agent will follow N206
        N218 = rand.nextDouble() <= 0.5; //This agent will follow N218
        N221 = rand.nextDouble() <= 0.5; //This agent will follow N221
        N233 = rand.nextDouble() <= 0.5; //This agent will follow N233
        N242 = rand.nextDouble() <= 0.5; //This agent will follow N242
        N245 = rand.nextDouble() <= 0.5; //This agent will follow N245
        
        listTargets = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Police force";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        model.indexClass(StandardEntityURN.ROAD);
        range = config.getIntValue(DISTANCE_KEY);
        Logger.info("{} connected: clearing distance = {}", this, range);
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {

        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to station channel
            Logger.info("PoliceAgent [" + getID() + "] sending subscription to radio channel " + Constants.STATION_CHANNEL);
            sendSubscribe(time, Constants.STATION_CHANNEL);
        }

        if (time < config.getIntValue(Constants.KEY_START_EXPERIMENT_TIME)) {
            return;
        }

        if (time == config.getIntValue(Constants.KEY_END_EXPERIMENT_TIME)) {
            System.exit(0);
        }
        

        // Wait until the station sends us an assignment
        ////////////////////////////////////////////////////////////////////////
        Logger.debug("Agent {} waiting for command.", getID());
        assignedTarget = fetchAssignment();
        if (assignedTarget != null) {
            Logger.debug("Agent {} got target {}", getID(), assignedTarget);
        } else {
            Logger.warn("Agent {} unable to fetch its assignment.", getID());
            assignedTarget = Assignment.UNKNOWN_TARGET_ID;
        }

        // Start to act
        // /////////////////////////////////////////////////////////////////////
        
        FireAllRules();
        if (this.assignedTarget == null) {
            FireAllRules();
            chooseRandomTarget();
            if (this.assignedTarget == null) {
                this.assignedTarget = Assignment.UNKNOWN_TARGET_ID;
            }
        }
        
        Blockade b = (Blockade) this.model.getEntity(assignedTarget);
        if (this.lastAssignedTarget == null || this.lastAssignedTarget.first().equals(Assignment.UNKNOWN_TARGET_ID)) {
            if (this.assignedTarget.equals(Assignment.UNKNOWN_TARGET_ID) == false) {
                this.lastAssignedTarget = new Pair(new EntityID(this.assignedTarget.getValue()), b.getRepairCost());
            }
        }else {
            if (this.lastAssignedTarget.equals(this.assignedTarget) == false) {
                //Check if last target was cleared
                if (this.model.getEntity(this.lastAssignedTarget.first()) == null) {
                    try {
                        //Then generate cleared blockade message
                        ClearedBlockadeMessage clearedMessage = new ClearedBlockadeMessage(this, this.lastAssignedTarget.first());
                        this.enqueueMessageToSend(clearedMessage);
                        
                        clearedBlockades.add(new Pair<>(lastAssignedTarget.first(), lastAssignedTarget.second()));
                    } catch (Exception ex) {
                        //java.util.logging.Logger.getLogger(PlatoonPoliceAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                if (b != null) {
                    this.lastAssignedTarget = new Pair(new EntityID(this.assignedTarget.getValue()), b.getRepairCost());
                }
            }
        }

        // If we have a target, approach or clear it
        // ///////////////////////////////
        if (assignedTarget != null && !assignedTarget.equals(Assignment.UNKNOWN_TARGET_ID)) {
            EntityID bID = assignedTarget;
            Blockade target = (Blockade)model.getEntity(bID);
            if (target != null) {
                assignedTarget = target.getPosition();

                // Clear if in range
                if (inRange(target)) {
                    Logger.debug(Markers.BLUE, "Police force {} clearing ASSIGNED target {}", getID(), assignedTarget);
                    clear(time, assignedTarget);
                    return;
                }

                // Approach it otherwise
                if (approach(time, assignedTarget)) {
                    return;
                }
            }
        }

        // If agents can independently choose targets, do it
        if (!config.getBooleanValue(Constants.KEY_AGENT_ONLY_ASSIGNED)) {
            EntityID myPosition = me().getPosition();

            // Pick the closest blockade
            Double minDistance = Double.MAX_VALUE;
            EntityID bestTarget = null;
            for (StandardEntity entity : model.getEntitiesOfType(StandardEntityURN.BLOCKADE)) {
                EntityID blockadePosition = ((Blockade)entity).getPosition();
                double d = model.getDistance(myPosition, blockadePosition);
                if (d < minDistance) {
                    minDistance = d;
                    bestTarget = blockadePosition;
                }
            }

            if (bestTarget == null) {
                Logger.info(Markers.BLUE, "Unassigned police force {} can't find any target.", getID());
                explore(time);
                return;
            }

            // Clear if in range
            Blockade target = (Blockade)model.getEntity(bestTarget);
            if (inRange(target)) {
                Logger.debug(Markers.BLUE, "Police force {} clearing self-assigned target {}", getID(), bestTarget);
                clear(time, bestTarget);
                return;
            }

            if (approach(time, bestTarget)) {
                return;
            }
        }

        explore(time);
    }
    
    
    public void setNewTarget(EntityID newTarget, Norm currentNorm) {
        //debug
        /*if (newTarget != null && currentNorm != null) {
            String s = "";
            for (EntityID eID :this.blockadesToPrioritize_N244) {
                s += this.model.getEntity(eID).getFullDescription() + "\n";
            }
            Logger.info("DATA N244 " + s);
            Logger.info("[" + getID() + "] setting new Target: targets is "
                            + this.model.getEntity(newTarget).getFullDescription()
                            + " by following current norm " + currentNorm.toString());
        }else {
            Logger.info("[" + getID() + "] newTarget and currentNorm setted to null");
        }*/
        //debug
        
        this.assignedTarget = newTarget;
        this.currentNorm = currentNorm;
        
        if (assignedTarget != null && currentNorm != null && this.model.getEntity(assignedTarget) != null) {
            listTargets.add(new Pair<>(assignedTarget, currentNorm));
        }
    }
    
    private void chooseRandomTarget() {
        Random rand = new Random();
        Pair<EntityID, Norm> target = listTargets.get(rand.nextInt(listTargets.size()));
        
        if (assignedTarget == null && (target.second() == Norm.N185 || target.second() == Norm.N206
                || target.second() == Norm.N218 || target.second() == Norm.N221)) {
            EntityID goal = null;
            
            switch (target.second()) {
                    case N185:
                        goal = roadToFocus_N185;
                        break;
                    case N206:
                        goal = mostDamagedBuilding_N206;
                        break;
                    case N218:
                        goal = buildingToFocus_N218;
                        break;
                    default:
                        goal = buildingToFocus_N221;
                        break;
            }
            
            if (goal != null) {
                if (clearedPaths.isEmpty()) {
                    clearedPaths.add(new Pair(new Pair(me().getPosition(), goal), target.second()));
                }else {
                    Pair<Pair<EntityID, EntityID>, Norm> lastElement = clearedPaths.get(clearedPaths.size() - 1);
                    if (lastElement.second() == target.second()) {
                        //Check if goal is distinct
                        if (!lastElement.first().first().equals(goal)) {
                            //If false follow this norm was choosed again and oter path will be cleaned following the same norm
                            clearedPaths.add(new Pair(new Pair(me().getPosition(), goal), target.second()));
                            
                        }
                    }else {
                        clearedPaths.add(new Pair(new Pair(me().getPosition(), goal), target.second()));
                    }
                }
            }
        }
        
        assignedTarget = target.first();
        currentNorm = target.second();
        listTargets = new ArrayList<>(); 
    }

    private boolean approach(int time, EntityID target) {
        SearchResults path = planPathToRoad(target);

        List<EntityID> steps = path.getPathIds();
        if (steps == null) {
            Logger.warn(Markers.RED, "Police force {} can't find a path to ASSIGNED target {}. Moving randomly.", getID(), target);
            sendMove(time, randomWalk());
            return false;
        }

        List<Blockade> blocks = path.getPathBlocks();
        if (!blocks.isEmpty() && inRange(blocks.get(0))) {
            Blockade block = blocks.get(0);
            Logger.debug(Markers.MAGENTA, "Police force {} clearing blockade {} to reach ASSIGNED target {} through {}", getID(), block, target, path);
            sendClear(time, block.getID());
            return true;
        }

        Logger.debug(Markers.MAGENTA, "Police force {} approaching ASSIGNED target {} through {}", getID(), target, path);
        sendMove(time, steps);
        return true;
    }

    private boolean inRange(Blockade target) {
        Point2D agentLocation = new Point2D(me().getX(), me().getY());
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Line2D line : GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(target.getApexes()), true)) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, agentLocation);
            double distance = GeometryTools2D.getDistance(agentLocation, closest);
            if (distance < bestDistance) {
                bestDistance = distance;
            }
        }
        Logger.debug("Distance: {} (clear range: {})", bestDistance, range);
        return bestDistance < range;
    }
    
    private boolean inRange(Blockade target, Pair<Integer,Integer> coordinates) {
        Point2D agentLocation = new Point2D(coordinates.first(), coordinates.second());
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Line2D line : GeometryTools2D.pointsToLines(GeometryTools2D.vertexArrayToPoints(target.getApexes()), true)) {
            Point2D closest = GeometryTools2D.getClosestPointOnSegment(line, agentLocation);
            double distance = GeometryTools2D.getDistance(agentLocation, closest);
            if (distance < bestDistance) {
                bestDistance = distance;
            }
        }
        Logger.debug("Distance: {} (clear range: {})", bestDistance, range);
        return bestDistance < range;
    }

    private void explore(int time) {
        // If the agen't can do nothing else, try to explore or just randomly walk around.
        List<EntityID> path = randomExplore();
        if (path != null) {
            Logger.debug(Markers.BLUE, "Police force {} exploring", getID());
        } else {
            path = randomWalk();
            Logger.debug(Markers.BLUE, "Police force {} moving randomly", getID());
        }

        sendMove(time, path);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.POLICE_FORCE);
    }

    /**
     * Given a target, calls the chosen algorothm to plan the path to the target
     * @param target: the target
     * @return a list of EntityID representing the path to the target
     */
    private SearchResults planPathToRoad(EntityID target) {
        SearchResults path = search.search(me().getPosition(), target,
                connectivityGraph, distanceMatrix);
        return path;
    }

    /**
     * Failed attempt at using AKClearArea messages. Reverting to AKClear instead.
     * @param time
     * @param road  F
     *
    private void clear(int time, EntityID road) {
        StandardEntity entity = model.getEntity(road);
        if (!(entity instanceof Road)) {
            Logger.warn("Police {} tried to clear non-road {}", getID(), road);
            return;
        }
        StandardEntity bEntity = model.getEntity(((Road)entity).getBlockades().get(0));
        if (!(bEntity instanceof Blockade)) {
            Logger.warn("Police {} tried to clear road {}, but it contains no blockades", getID(), road);
        }

        Blockade target = (Blockade)bEntity;
        Logger.warn("Target apexes: {}", target.getApexes());
        List<Point2D> vertices = GeometryTools2D.vertexArrayToPoints(target.getApexes());
        double best = Double.MIN_VALUE;
        Point2D bestPoint = null;
        Point2D origin = new Point2D(me().getX(), me().getY());
        for (Point2D vertex : vertices) {
            double d = GeometryTools2D.getDistance(origin, vertex);
            if (d > best) {
                best = d;
                bestPoint = vertex;
            }
        }
        sendClear(time, (int)(bestPoint.getX()), (int)(bestPoint.getY()));
    }*/

    private void clear(int time, EntityID road) {
        StandardEntity entity = model.getEntity(road);
        if (!(entity instanceof Road)) {
            Logger.warn("Police {} tried to clear non-road {}", getID(), road);
            return;
        }
        List<EntityID> blockades = ((Road)entity).getBlockades();
        if (blockades.isEmpty()) {
            Logger.warn("Police {} tried to clear road {}, but it contains no blockades", getID(), road);
            return;
        }

        sendClear(time, blockades.get(0));
    }
    
    public Collection<Building> getMostDamagedInhabitedBuildings() {
        Collection<StandardEntity> buildings = this.model.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        Collection<Building> mostDamagedBuildings = new ArrayList<>();
        int maxDamage = -1;
        
        for (StandardEntity se :buildings) {
            Building building = (Building) se;
            if (building.isBrokennessDefined() && !this.isBuildingEmpty(building)) {
                Logger.info("[" + getID() + "] building inhabited and damaged " + building.getFullDescription());
                if (maxDamage == -1) {
                    maxDamage = building.getBrokenness();
                    mostDamagedBuildings.add(building);
                }else {
                    if (maxDamage < building.getBrokenness()) {
                        maxDamage = building.getBrokenness();
                        mostDamagedBuildings = new ArrayList<>();
                        mostDamagedBuildings.add(building);
                    }else {
                        if (maxDamage == building.getBrokenness()) {
                            mostDamagedBuildings.add(building);
                        }
                    }
                }
            }
        }
        
        return mostDamagedBuildings;
    }
    
    public Collection<Building> getDamagedBuildingsByRange(int maxBrokenness, int minBrokenness) {
        Collection<StandardEntity> buildings = this.model.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        Collection<Building> damagedBuildings = new ArrayList<>();
        
        for (StandardEntity se :buildings) {
            Building building = (Building) se;
            if (building.isBrokennessDefined()) {
                if (building.getBrokenness() >= minBrokenness && building.getBrokenness() <= maxBrokenness) {
                    damagedBuildings.add(building);
                }
            }
        }
        
        return damagedBuildings;
    }
    
    public boolean isBlockadeUnnattended(Blockade blockade) {
        Collection<StandardEntity> polices = this.model.getEntitiesOfType(StandardEntityURN.POLICE_FORCE);
        for (StandardEntity se :polices) {
            if (se.getID().equals(this.getID()) == false) { //Don't estimate including oneself
                PoliceForce police = (PoliceForce) se;
                Pair<Integer,Integer> coordinates = new Pair<>(police.getX(), police.getY());
                if (inRange(blockade, coordinates)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public Collection<Blockade> mostBlockingBlockades() {
        Collection<StandardEntity> blockades = this.model.getEntitiesOfType(StandardEntityURN.BLOCKADE);
        List<Blockade> mostBlockingBlockades = new ArrayList<>();
        int maxBlockages = 0;
        int blockages = 0;
        for (StandardEntity se :blockades) {
            Blockade blockade = (Blockade) se;
            Road position = (Road) this.model.getEntity(blockade.getPosition());
            List<EntityID> neighboursID = position.getNeighbours();
            for (EntityID neighbourID :neighboursID) {
                StandardEntity neighbour = this.model.getEntity(neighbourID);
                if (neighbour instanceof Road) {
                    blockages++;
                }
            }
            
            Logger.info("[" + getID() + "] blockade " + blockade + " is blocking " + blockages + " roads. Currently maxBlockades is " + maxBlockages);
            if (blockages > maxBlockages) {
                maxBlockages = blockages;
                mostBlockingBlockades = new ArrayList<>();
                mostBlockingBlockades.add(blockade);
            }
            
            if (blockages == maxBlockages) {
                mostBlockingBlockades.add(blockade);
            }
            
            blockages = 0;
        }
        
        return mostBlockingBlockades;
    }
    
    public boolean anyBlockadeInRange() {
        Collection<StandardEntity> blockades = this.model.getEntitiesOfType(StandardEntityURN.BLOCKADE);
        for (StandardEntity se :blockades) {
            if (inRange((Blockade)se)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Collection<Blockade> getBlockadesInRange() {
        Collection<StandardEntity> blockades = this.model.getEntitiesOfType(StandardEntityURN.BLOCKADE);
        Collection<Blockade> blockadesInRange = new ArrayList<>();
        for (StandardEntity se :blockades) {
            if (inRange((Blockade)se)) {
                blockadesInRange.add((Blockade)se);
            }
        }
        
        return blockadesInRange;
    }

    @Override
    protected void generateSpecificMessage(EntityID entityChanged, ChangeSet change) {
        //Noop
    }
}