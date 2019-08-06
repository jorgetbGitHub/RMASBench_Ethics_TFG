package RSLBench;

import RSLBench.Assignment.Assignment;
import static rescuecore2.misc.Handy.objectsToIDs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import RSLBench.Helpers.DistanceSorter;
import RSLBench.Helpers.Logging.Markers;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.worldmodel.properties.IntProperty;


/**
 * A sample fire brigade agent.
 */
public class PlatoonFireAgent extends PlatoonAbstractAgent<FireBrigade>
{
    private static final Logger Logger = LogManager.getLogger(PlatoonFireAgent.class);

    public static final String MAX_WATER_KEY = "fire.tank.maximum";
    public static final String MAX_DISTANCE_KEY = "fire.extinguish.max-distance";
    public static final String MAX_POWER_KEY = "fire.extinguish.max-sum";

    private int maxWater;
    private int maxDistance;
    private int maxPower;
    private EntityID assignedTarget = Assignment.UNKNOWN_TARGET_ID;
    
    public List<EntityID> alternativePath = null; // Included
    public List<EntityID> excludedAreas = new ArrayList<>(); // Included
    public List<EntityID> currentTargets; // Included
    public boolean pathAssigned = false; // Included
    
    public boolean N41 = false; //Included n41, this will permit to know if agent decided to follow or not norm 41
    

    public PlatoonFireAgent() {
    	Logger.debug(Markers.BLUE, "Platoon Fire Agent CREATED");
    }

    @Override
    public String toString() {
        return "Sample fire brigade";
    }

    @Override
    protected void postConnect() {
        super.postConnect();
        maxWater = config.getIntValue(MAX_WATER_KEY);
        maxDistance = config.getIntValue(MAX_DISTANCE_KEY);
        maxPower = config.getIntValue(MAX_POWER_KEY);
        Logger.info("{} connected: max extinguish distance = {}, " +
               "max power = {}, max tank = {}",
                this, maxDistance, maxPower, maxWater);
        
        
        //Will this agent follow norm 41 or not?
        Random rand = new Random();
        if (rand.nextDouble() <= 0.5) {
            //This agent will follow norm 41 until its end. That's gonna rescue Refuge
            N41 = true; 
        }
        
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {
        
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to station channel
            Logger.info("FireAgent [" + getID() + "] sending subscription to radio channel " + Constants.STATION_CHANNEL);
            sendSubscribe(time, Constants.STATION_CHANNEL);
        }
        
        /*if (me().getPosition().equals(new EntityID(35014))) {
            nearBuildings();
        }*/
        
        //debug
        /*String blockadesWarnedContent = "";
        if (!blockadesWarned.isEmpty()) {
            for (EntityID key : blockadesWarned.keySet()) {
                blockadesWarnedContent += String.valueOf(key.getValue()) + " " + blockadesWarned.get(key) + "  ";
            }
            Logger.info("[FIRE AGENT] " + "["+ getID() + "]  blockadesWarnedContent= " + blockadesWarnedContent);
        }*/
        //debug
        
        currentTargets = null;

        Logger.debug(Markers.MAGENTA,"Fire agent thinking");

        if (time < config.getIntValue(Constants.KEY_START_EXPERIMENT_TIME)) {
            return;
        }

        if (time == config.getIntValue(Constants.KEY_END_EXPERIMENT_TIME))
            System.exit(0);

        // Wait until the station sends us an assignment
        ////////////////////////////////////////////////////////////////////////
        assignedTarget = fetchAssignment();

        // Start to act
        // /////////////////////////////////////////////////////////////////////
        FireBrigade me = me();
        
        // Are we currently filling with water?
        // //////////////////////////////////////
        if (me.isWaterDefined() && me.getWater() < maxWater
                && location() instanceof Refuge) {
            Logger.debug(Markers.MAGENTA, "Filling with water at " + location());
            sendRest(time);
            return;
        }

        // Are we out of water?
        // //////////////////////////////////////
        /*if (me.isWaterDefined() && me.getWater() == 0) { N70
            // Head for a refuge
           currentTargets = refugeIDs;
           path = search.search(me().getPosition(), refugeIDs,
                    connectivityGraph, distanceMatrix).getPathIds();
            if (path != null) {
                // Logger.debugColor("Moving to refuge", //Logger.FG_MAGENTA);
                sendMove(time, path);
                return;
            } else {
                // Logger.debugColor("Couldn't plan a path to a refuge.",
                // //Logger.BG_RED);
                path = randomWalk();
                // Logger.debugColor("Moving randomly", //Logger.FG_MAGENTA);
                sendMove(time, path);
                return;
            }
        }*/
        
        //Logger.info("FireAgent [" + this.getID() + "] has level of water = " + me.getWater());
        if (me.isWaterDefined() && me.getWater() == 0) {
            path = randomWalk();
            sendMove(time, path);
            return;
        }

        // Find all buildings that are on fire
        Collection<EntityID> burning = getBurningBuildings();

        // Try to plan to assigned target
        // ///////////////////////////////

        // Ensure that the assigned target is still burning, and unassign the
        // agent if it is not.
        if (!burning.contains(assignedTarget)) {
            assignedTarget = Assignment.UNKNOWN_TARGET_ID;
        }

        
        FireAllRules(); // Habrá que reorganizar esto pero funciona...
        
        if (!assignedTarget.equals(Assignment.UNKNOWN_TARGET_ID)) {
             
            // Extinguish if the assigned target is in range
            if (targetAssignedInRange()) {
                Logger.debug(Markers.MAGENTA, "Agent {} extinguishing ASSIGNED target {} test", getID(), assignedTarget);
                sendExtinguish(time, assignedTarget, maxPower);
                // sendSpeak(time, 1, ("Extinguishing " + next).getBytes());
                return;
            }

            // Try to approach the target (if we are here, it is not yet in range)
            path = planPathToFire(assignedTarget);
            if (path != null) {
                Logger.debug(Markers.MAGENTA, "Agent {} approaching ASSIGNED target {}", getID(), assignedTarget);
                sendMove(time, path);
            } else {
                Logger.warn(Markers.RED, "Agent {} can't find a path to ASSIGNED target {}. Moving randomly.", getID(), assignedTarget);
                sendMove(time, randomWalk());
            }
            return;
        }

        // If agents can independently choose targets, do it
        if (!config.getBooleanValue(Constants.KEY_AGENT_ONLY_ASSIGNED)) {
            for (EntityID next : burning) {
                path = planPathToFire(next);
                if (path != null) {
                    Logger.info(Markers.MAGENTA, "Unassigned agent {} choses target {} by itself", getID(), next);
                    sendMove(time, path);
                    return;
                }
            }
            if (!burning.isEmpty()) {
                Logger.info(Markers.MAGENTA, "Unassigned agent {} can't reach any of the {} burning buildings", getID(), burning.size());
            }
        }

        // If the agen't can do nothing else, try to explore or just randomly
        // walk around.
        path = randomExplore();
        if (path != null) {
            Logger.debug(Markers.MAGENTA, "Agent {} exploring", getID());
        } else {
            path = randomWalk();
            Logger.debug(Markers.MAGENTA, "Agent {} moving randomly", getID());
        }

        sendMove(time, path);
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
    }

    /**
     * Returns the burning buildings.
     * @return a collection of burning buildings.
     */
    private Collection<EntityID> getBurningBuildings() {
        Collection<StandardEntity> e = model
                .getEntitiesOfType(StandardEntityURN.BUILDING);
        List<Building> result = new ArrayList<>();
        for (StandardEntity next : e) {
            if (next instanceof Building) {
                Building b = (Building) next;
                if (b.isOnFire()) {
                    result.add(b);
                }
            }
        }
        // Sort by distance
        Collections.sort(result, new DistanceSorter(location(), model));
        return objectsToIDs(result);
    }

    /**
     * Given a target, calls the chosen algorothm to plan the path to the target
     * @param target: the target
     * @return a list of EntityID representing the path to the target
     */
    private List<EntityID> planPathToFire(EntityID target) {
        Area area = (Area) connectivityGraph.getWorld().getEntity(target);
        Collection<StandardEntity> targets = model.getObjectsInRange(target,
                maxDistance / 2);
        
        currentTargets = new ArrayList<>();
        currentTargets.add(target);
        
        return search.search(me().getPosition(), target,
                connectivityGraph, distanceMatrix).getPathIds();
    }
    
    @Override
    protected void sendMove(int time, List<EntityID> path) {
        this.path = path;
        if (path != null) {
            pathAssigned = true;
            // Time to FIRE rule N79, N70
            FireAllRules();
            
            if (alternativePath != null && !alternativePath.isEmpty()) {
                if (this.alternativePath.contains(me().getPosition())){
                   /*EntityID[] e = Arrays.copyOfRange((EntityID[])this.alternativePath.toArray(), this.alternativePath.indexOf(me().getPosition()), this.alternativePath.size()-1, EntityID[].class);
                   ArrayList<EntityID> a = (ArrayList<EntityID>) Arrays.asList(e);
                   this.alternativePath = a;*/
                   ArrayList<EntityID> a = new ArrayList<>();
                   for (int i = this.alternativePath.indexOf(me().getPosition()) + 1; i < this.alternativePath.size(); i++) {
                       a.add(this.alternativePath.get(i));
                   }
                   
                   this.alternativePath = a;
                }
                
                super.sendMove(time, this.alternativePath);
            }else{
                super.sendMove(time, this.path);
            }
        }
        
        this.path = null;
        this.pathAssigned = false;
    }
    
    @Override
    protected void sendExtinguish(int time, EntityID target, int water) {
        //FireAllRules();
        if (assignedTarget != null) {
            super.sendExtinguish(time, assignedTarget, water);
        }else {
            super.sendExtinguish(time, target, water);
        }
    }
    
    //Included (OLD IMPLEMENTATION using OMNIPOTENT KNOWLEDGEMENT
    /*public List<EntityID> getBurningRefuges() {
        Logger.info("[PlatoonFireAgent] [" + this.getID() + "] getBurningRefuges called");
        List<EntityID> burningRefuges;
        burningRefuges = new ArrayList<>();
        for (EntityID refugeID: this.refugeIDs) {
            Refuge refuge = (Refuge) this.getGraph().getWorld().getEntity(refugeID);
            if (refuge.isOnFire() || this.getOnFireNeighbours(refuge).size() > 1) { //Preventive burning
                burningRefuges.add(refugeID);
            }
        }
        
        Logger.info("[PlatoonFireAgent] [" + this.getID() + "] burningRefuges->" + burningRefuges);
        return ((List<EntityID>)burningRefuges);
    }*/
    
    //Included (NEW IMPLEMENTATION)
    public Collection<EntityID> getBurningRefuges() {
        Collection<EntityID> c = new ArrayList<>();
        for (EntityID refugeID :this.refugeIDs) {
            if (firesWarned.get(refugeID)) {
                Refuge refuge = (Refuge)this.connectivityGraph.getWorld().getEntity(refugeID);
                c.add(refugeID);
            }
        }
        return c;
    }
    
    public Collection<EntityID> getRefugesInDanger() {
        Collection<EntityID> refugesOnFire = getBurningRefuges();
        Collection<EntityID> refugesInDanger = new ArrayList<>(refugesOnFire);
        for (EntityID refugeID :this.refugeIDs) {
            if (!refugesInDanger.contains(refugeID)) {
                Map<Integer,Collection<EntityID>> map = this.nearBurningBuildings(refugeID, 100000);
                if ((map.get(StandardEntityConstants.Fieryness.HEATING.getValue()) != null && !map.get(StandardEntityConstants.Fieryness.HEATING.getValue()).isEmpty())
                        || (map.get(StandardEntityConstants.Fieryness.BURNING.getValue()) != null && !map.get(StandardEntityConstants.Fieryness.BURNING.getValue()).isEmpty())
                        || (map.get(StandardEntityConstants.Fieryness.INFERNO.getValue()) != null && !map.get(StandardEntityConstants.Fieryness.INFERNO.getValue()).isEmpty())) {
                    refugesInDanger.add(refugeID);
                }
            }
        }
        
        return refugesInDanger;
    }
    
    //Included (n283)
    public int getMaxDistance() {
        return this.maxDistance;
    }
    
    //Included
    public boolean targetAssignedInRange() {
        return (model.getDistance(me().getPosition(), assignedTarget) <= maxDistance);
    }
    
    //Included
    public boolean isInRange(EntityID entity) {
        return (model.getDistance(me().getPosition(), entity) <= maxDistance);
    }
    
    //Included
    public EntityID getAssignedTarget() {
        return this.assignedTarget;
    }
    
    //Included
    public void setNewTarget(EntityID entity) {
        this.assignedTarget = entity;
    }
    
    /**
     * Allow to gather every fire near of a specific entity using a determined max distance between 
     * fires and the entity (Included n40)
     * @param entity
     * @param distance
     * @return A HashMap where key is fieryness level and the value a collection of building's ids with such severity level
     */
    public Map<Integer,Collection<EntityID>> nearBurningBuildings(EntityID entity, int distance) {
        Map<Integer,Collection<EntityID>> nearBuildings = new HashMap<>();
        Collection<EntityID> burningBuildings = this.getBurningBuildings();
        StandardEntity se = this.connectivityGraph.getWorld().getEntity(entity);
        if (se instanceof Building && ((Building)se).isOnFire()) {
            //Check if it's included
            if (!burningBuildings.contains(entity)) {
                burningBuildings.add(entity);
            }
        }
        Collection<EntityID> l;
        for (EntityID burning: burningBuildings) {
            if (model.getDistance(entity, burning) <= distance) {
                Building b = (Building)this.connectivityGraph.getWorld().getEntity(burning);
                if (nearBuildings.containsKey(b.getFieryness())) {
                    //Update value associated
                    l = nearBuildings.get(b.getFieryness());
                    l.add(burning);
                    nearBuildings.put(b.getFieryness(), l);
                }else{
                    //Generate a new entry
                    l = new ArrayList<>();
                    l.add(burning);
                    nearBuildings.put(b.getFieryness(), l);
                }
            }
        }
        
        return nearBuildings;
    }
    
    private void nearBuildings() {
        Collection<StandardEntity> buildings = this.model.getEntitiesOfType(StandardEntityURN.BUILDING,
                StandardEntityURN.REFUGE);
        for (StandardEntity se :buildings) {
            Building b = (Building) se;
            if (this.model.getDistance(getID(), b.getID()) <= 100000) {
                System.out.println("[" + getID() + "] <scenario:civilian scenario:location=\"" + b.getID() + "\"");
            }
        }
    }
    
    /**
     * Using agent's information check if there's some refuge on fire
     * @return false if any refuge is on fire, false otherwise
     */
    public boolean isRefugeOnFire() {
        for (EntityID refugeID :this.refugeIDs) {
            if (firesWarned.get(refugeID)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Collection<Building> getOnFireNeighbours(Building building) {
        Collection<Building> c = new ArrayList<>();
        List<EntityID> neighbours = building.getNeighbours();
        for (EntityID neighbourID :neighbours) {
            StandardEntity se = this.connectivityGraph.getWorld().getEntity(neighbourID);
            if (se instanceof Building && ((Building)se).isOnFire()) {
                c.add((Building)se);
            }
        }
        
        return c;
    }

    public static void printMap(Map mp) {
    Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
    
    
    /**
     * Time to handle blockade messages.
     * - Update excludedAreas
     **/
    
    @Override
    protected void handleDiscoveredBlockadeMessageReceived(DiscoveredBlockadeMessage discoveredBlockadeMsg) {
        super.handleDiscoveredBlockadeMessageReceived(discoveredBlockadeMsg);
        Blockade blockade = (Blockade) this.model.getEntity(discoveredBlockadeMsg.getBlockadeID());
        if (!this.excludedAreas.contains(blockade.getPosition())) {
            Road road = (Road) this.model.getEntity(blockade.getPosition());
            Polygon shapeRoad = (Polygon) road.getShape();
            Polygon shapeBlockade = (Polygon) blockade.getShape();
            
            List<Integer> roadPointsY = Arrays.asList(ArrayUtils.toObject(shapeRoad.ypoints));
            List<Integer> blockadePointsY = Arrays.asList(ArrayUtils.toObject(shapeBlockade.ypoints));
                
            if (Objects.equals(Collections.max(roadPointsY), Collections.max(blockadePointsY))
                    && Objects.equals(Collections.min(roadPointsY), Collections.min(roadPointsY))) {
                this.excludedAreas.add(blockade.getPosition());
            }

            /*OLD CHECK
            if (Arrays.equals(shapeRoad.xpoints, shapeBlockade.xpoints)
                    && Arrays.equals(shapeRoad.ypoints, shapeBlockade.ypoints)) {
                this.excludedAreas.add(blockade.getPosition());
            }*/
        }
    }
    
    @Override
    protected void handleClearedBlockadeMessageReceived(ClearedBlockadeMessage clearedBlockadeMsg) {
        super.handleClearedBlockadeMessageReceived(clearedBlockadeMsg);
        Blockade blockade = (Blockade) this.model.getEntity(clearedBlockadeMsg.getBlockadeID());
        if (this.excludedAreas.contains(blockade.getPosition())) {
            this.excludedAreas.remove(blockade.getPosition());
        }
    }

    @Override
    protected void generateSpecificMessage(EntityID entityChanged, ChangeSet change) {
        StandardEntity se = this.model.getEntity(entityChanged);
        if (se != null && (se instanceof Building || se instanceof Refuge)) {
            //Check if in changes set is extinguished
            Building building = (Building) se;
            IntProperty fieryness = (IntProperty) change.getChangedProperty(entityChanged, StandardPropertyURN.FIERYNESS.toString());
            if (fieryness.getValue() == StandardEntityConstants.Fieryness.MINOR_DAMAGE.getValue()
                    || fieryness.getValue() == StandardEntityConstants.Fieryness.MODERATE_DAMAGE.getValue()
                    || fieryness.getValue() == StandardEntityConstants.Fieryness.SEVERE_DAMAGE.getValue()) {
                //Then check if building is on fire in the current model
                if (building.getFieryness() == StandardEntityConstants.Fieryness.HEATING.getValue()
                        || building.getFieryness() == StandardEntityConstants.Fieryness.BURNING.getValue()
                        || building.getFieryness() == StandardEntityConstants.Fieryness.INFERNO.getValue()) {
                    try {
                        //Time to generate Extinguished Fire Message
                        ExtinguishedFireMessage extinguishedMessage = new ExtinguishedFireMessage(this, building);
                        enqueueMessageToSend(extinguishedMessage);
                    } catch (Exception ex) {
                        //java.util.logging.Logger.getLogger(PlatoonFireAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
}