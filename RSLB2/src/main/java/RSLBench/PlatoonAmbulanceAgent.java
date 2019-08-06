/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench;

import RSLBench.Assignment.Assignment;
import RSLBench.Search.SearchResults;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.messages.AKLoad;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.EntityRefProperty;

/**
 *
 * @author jorgetb
 */
public class PlatoonAmbulanceAgent extends PlatoonAbstractAgent<AmbulanceTeam>{
    
    private static final Logger Logger = LogManager.getLogger(PlatoonAmbulanceAgent.class);
    
    private EntityID assignedTarget = Assignment.UNKNOWN_TARGET_ID;
    private EntityID hospital;
    private Human loadedHuman = null;
    private List<EntityID> listTargets;
    
    public EntityID mainTarget = null;
    public EntityID roadMostTrapped = null;
    
    public List<EntityID> excludedAreas = new ArrayList<>(); // Included
    
    public boolean isActiveN169 = false;
    
    //Included N110
    public boolean N110;
    
    //Included N116
    public boolean N116;
    
    //Rules counter [Analytics purposes]
    public Map<Norm, Integer> rulesCounter = new HashMap<>();
    
    //Humans unloaded in the hospital [Analytics purposes]
    public List<EntityID> humansHospitalized = new ArrayList<>();
    
    //Humans helped [Analytics purposes]
    public List<EntityID> humansRescued = new ArrayList<>();
    
    
    @Override
     protected void postConnect() {
         super.postConnect();
         
         Random rand = new Random();
         N110 = rand.nextDouble() <= 0.5;
         N116 = rand.nextDouble() <= 0.5;
         
         hospital = this.getRefuges().get(0).getID();
         listTargets = new ArrayList<>();
     }
    
    @Override
    public String toString() {
        return "Ambulance agent";
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.AMBULANCE_TEAM);
    }

    @Override
    protected void think(int time, ChangeSet changes, Collection<Command> heard) {
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to station channel
            Logger.info("AmbulanceAgent [" + getID() + "] sending subscription to radio channel " + Constants.STATION_CHANNEL);
            sendSubscribe(time, Constants.STATION_CHANNEL);
        }
        
        //debug
        /*String debug = "";
        Collection<StandardEntity> civilians = this.model.getEntitiesOfType(StandardEntityURN.CIVILIAN);
        for (StandardEntity se :civilians) {
            debug += se.getFullDescription() + "\n";
        }
        Logger.info("[" + getID() + "] debug AT -> " + debug);
        
        Logger.info("[" + getID() + "] excluded areas -> " + this.excludedAreas);
        
        String injuriesWarnedContent = "";
        
        if (!injuriesWarned.isEmpty()) {
            for (EntityID key : injuriesWarned.keySet()) {
                injuriesWarnedContent += String.valueOf(key.getValue()) + " " + injuriesWarned.get(key) + "  ";
            }
            Logger.info("[AmbulanceAgent] " + "["+ getID() + "]  injuriesWarnedContent= " + injuriesWarnedContent);
        }*/
        //debug
        
        if (time < config.getIntValue(Constants.KEY_START_EXPERIMENT_TIME)) {
            return;
        }

        if (time == config.getIntValue(Constants.KEY_END_EXPERIMENT_TIME)) {
            System.exit(0);
        }
        
        // Wait until the station sends us an assignment
        ////////////////////////////////////////////////////////////////////////
        assignedTarget = fetchAssignment();
        Logger.info("[" + getID() + "] Solved assigned to me the follow target -> " + this.assignedTarget);
        
        // Start to act
        // /////////////////////////////////////////////////////////////////////
        AmbulanceTeam me = me();
        
        if (me.isBuriednessDefined() && me.getBuriedness() > 0) {
            return;
        }
        
        if (mainTarget == null) {
            FireAllRules();
            chooseRandomTarget();
            //Logger.info("[" + getID() + "] mainTarget after FireAllRules -> " + mainTarget);
            if (mainTarget == null) {
                //Default target is the closest target
                mainTarget = this.closestTarget();
                //Logger.info("mainTarget choosed by closestTarget() is " + mainTarget);
            }
        }else {
            this.assignedTarget = mainTarget;
        }
        
        
        //LOGIC: MOVE:TARGET->RESCUE->LOAD->MOVE:HOSPITAL->UNLOAD
        if (loadedHuman != null) {
            if (loadedHuman.getPosition().equals(getID())) {
                //Ambulance Agent is loading a human
                if (!me.getPosition().equals(hospital)) {
                    //If agent is not in the hospital he must go to it
                    path = this.generatePathToTarget(hospital);
                    if (path == null) {
                        path = new ArrayList<>();
                    }else {
                        if (this.isActiveN169) {
                            if (!path.isEmpty()) {
                                Area nextArea = (Area) this.model.getEntity(path.get(0));
                                if (nextArea instanceof Road && isBlocked((Road)nextArea)) {
                                    excludedAreas.add(nextArea.getID());
                                    path = this.generatePathToTarget(hospital);
                                }
                            }
                        }
                    }

                    this.sendMove(time, path);
                }else {
                    //If agent is in the hospital he must unload
                    this.sendUnload(time);
                    //Logger.info("[" + getID() + "] mainTarget->" + mainTarget + " was hopitalized");
                    loadedHuman = null;
                    if (!humansHospitalized.contains(mainTarget)) {
                       humansHospitalized.add(mainTarget);
                    }

                    mainTarget = null;
                }
            }else {
                //Ambulance Agent is not really loading a human
                //Logger.info("[" + getID() + "] mainTarget is null now cause is not loaded correctly");
                loadedHuman = null;
                mainTarget = null;
            }
        }else {
            //If no loaded human agent must go to rescue the target
            Human target = (Human) this.model.getEntity(this.assignedTarget);
            if (target != null) {
                if (!me.getPosition().equals(target.getPosition())){
                    //If ambulance agent is not adjacent of assigned target must move to it
                    path = this.generatePathToTarget(target.getPosition());
                    if (path == null) {
                        //Logger.info("[" + getID() + "] target" + target + "is null now cause no loaded human was unreachable");
                        this.mainTarget = null;
                    }else {
                        this.sendMove(time, path);
                    }
                }else {
                    //If ambulance agent is adjacent of assigned target
                    if (target.isBuriednessDefined() && target.getBuriedness() > 0) {
                        //Is necessary to rescue the target
                        //Logger.info("[" + getID() + "] attempting to rescue " + target);
                        this.sendRescue(time, this.assignedTarget);

                        if (!humansRescued.contains(this.assignedTarget)) {
                            humansRescued.add(assignedTarget);
                        }
                    }else {
                        //Logger.info("[" + getID() + "] attempting to load " + target);
                        this.sendLoad(time, this.assignedTarget);
                        loadedHuman = target;
                    }
                }
            }
        }
        
    }
    
    public EntityID closestTarget() {
        Collection<StandardEntity> humans = this.getAllHumans();
        Pair<EntityID, Integer> closest = new Pair<>(null, -1);
        for (StandardEntity se :humans) {
            Human human = (Human) se;
            if (human.getID().equals(getID()) == false && isInjured(human.getID())) {
                //Calculate distance
                int distance = getWorld().getDistance(getID(), human.getID());
                if (closest.second() == -1) {
                    closest = new Pair<>(human.getID(), distance);
                }else {
                    if (closest.second() > distance) {
                        closest = new Pair<>(human.getID(), distance);
                    }
                }
            }
        }
        
        return closest.first();
    }
    
    public List<EntityID> generatePathToTarget(EntityID target) {
        try {
            AmbulanceTeam me = this.me();
            Area start = (Area) this.model.getEntity(me.getPosition());
            Area goal = (Area) this.model.getEntity(target);
            Collection<Area> goals = new ArrayList<>();
            goals.add(goal);

            if (start == null || goals == null) {
                return null;
            }

            SearchResults sr = this.search.search(start, goals, connectivityGraph, distanceMatrix, excludedAreas);

            if (sr != null) {
                return sr.getPathIds();
            }else {
                return null;
            }
        }catch (ClassCastException ex) {
            if (loadedHuman != null && !loadedHuman.getPosition().equals(getID())) {
                mainTarget = null;
                loadedHuman = null;
            }
            return null;
        }
    }
    
    public Collection<StandardEntity> getAllHumans() {
        return this.model.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE,
                StandardEntityURN.POLICE_FORCE,
                StandardEntityURN.AMBULANCE_TEAM,
                StandardEntityURN.CIVILIAN);
    }
    
    public List<EntityID> getTrappedHumans(EntityID position) {
        Collection<StandardEntity> humans = getAllHumans();
        List<EntityID> hInside = new ArrayList<>();
        for (StandardEntity se: humans) {
            Human human = (Human) se;
            if (human.getPosition().equals(position)) {
                if (isInjured(human.getID())) {
                    hInside.add(human.getID());
                }
            }
        }
        
        return hInside;
    }
    
    public Collection<Building> getMostDamagedInhabitedBuildings() {
        Collection<StandardEntity> buildings = this.model.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        Collection<Building> mostDamagedBuildings = new ArrayList<>();
        int maxDamage = -1;
        
        for (StandardEntity se :buildings) {
            Building building = (Building) se;
            if (building.isBrokennessDefined() && !this.isBuildingEmpty(building)) {
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
    
    //Returns the buildings where is located the highest number of trapped people
    public List<Building> getBuildingsMostTrapped() {
       Collection<StandardEntity> humans = getAllHumans();
       Map<EntityID, Integer> m = new HashMap<>();
       List<Building> mostTrapped = new ArrayList<>();
       int max = 0;
       for (StandardEntity se :humans) {
           Human human = (Human) se;
           if (human.isBuriednessDefined() && human.getBuriedness() > 0) {
               if (m.containsKey(human.getPosition())) {
                   int newValue = m.get(human.getPosition()) + 1;
                   
                   if (newValue == max) {
                       mostTrapped.add((Building)this.model.getEntity(human.getPosition()));
                   }
                   
                   if (newValue > max) {
                       max = newValue;
                       mostTrapped = new ArrayList<>();
                       mostTrapped.add((Building)this.model.getEntity(human.getPosition()));
                   }
                   
                   m.put(human.getPosition(), newValue);
               }else {
                   m.put(human.getPosition(), 1);
                   if (max == 0) {
                       max = 1;
                   }
                   
                   if (max == 1) {
                       mostTrapped.add((Building)this.model.getEntity(human.getPosition()));
                   }
                   
               }
           }
       }
       
       return mostTrapped;
    }
    
    public List<AmbulanceTeam> getBuriedAmbulances() {
        Collection<StandardEntity> ambulances = this.model.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);
        List<AmbulanceTeam> buriedAmbulances = new ArrayList<>();
        for (StandardEntity se :ambulances) {
            AmbulanceTeam ambulance = (AmbulanceTeam) se;
            if (ambulance.isBuriednessDefined() && ambulance.getBuriedness() > 0) {
                buriedAmbulances.add(ambulance);
            }
        }
        
        return buriedAmbulances;
    }
    
    //Will return every ambulance agent with hp level or less
    public List<AmbulanceTeam> getDamagedAmbulances(int hp) {
        Collection<StandardEntity> ambulances = this.model.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM);
        List<AmbulanceTeam> damagedAmbulances = new ArrayList<>();
        for (StandardEntity se :ambulances) {
            AmbulanceTeam ambulance = (AmbulanceTeam) se;
            if (ambulance.isHPDefined() && ambulance.getHP() <= hp && ambulance.getHP() > 0) {
                damagedAmbulances.add(ambulance);
            }
        }
        
        return damagedAmbulances;
    }
    
    public void setNewTarget(EntityID newTarget) {
        this.listTargets.add(newTarget);
        this.assignedTarget = newTarget;
        this.mainTarget = newTarget;
    }
    
    public EntityID getTarget() {
        return this.mainTarget;
    }
    
    @Override
    protected void handleDiscoveredBlockadeMessageReceived(DiscoveredBlockadeMessage discoveredBlockadeMsg) {
        super.handleDiscoveredBlockadeMessageReceived(discoveredBlockadeMsg);
        
        if (this.isActiveN169) {
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
    }
    
    private boolean isBlocked(Road road) {
        List<EntityID> blockades = road.getBlockades();
        if (!blockades.isEmpty()) {
            Blockade blockade = (Blockade) this.model.getEntity(blockades.get(0));
            Polygon shapeRoad = (Polygon) road.getShape();
            Polygon shapeBlockade = (Polygon) blockade.getShape();

            List<Integer> roadPointsY = Arrays.asList(ArrayUtils.toObject(shapeRoad.ypoints));
            List<Integer> blockadePointsY = Arrays.asList(ArrayUtils.toObject(shapeBlockade.ypoints));

            if (Objects.equals(Collections.max(roadPointsY), Collections.max(blockadePointsY))
                    && Objects.equals(Collections.min(roadPointsY), Collections.min(roadPointsY))) {
                return true;
            }else {
                return false;
            }
        }
        
        return false;
    }
    
    @Override
    protected void handleClearedBlockadeMessageReceived(ClearedBlockadeMessage clearedBlockadeMsg) {
        super.handleClearedBlockadeMessageReceived(clearedBlockadeMsg);
        
        if (this.isActiveN169) {
            Blockade blockade = (Blockade) this.model.getEntity(clearedBlockadeMsg.getBlockadeID());
            if (this.excludedAreas.contains(blockade.getPosition())) {
                    this.excludedAreas.remove(blockade.getPosition());
            }
        }
    }

    @Override
    protected void generateSpecificMessage(EntityID entityChanged, ChangeSet change) {
        if (mainTarget != null && mainTarget.equals(entityChanged)) {
            Human human = (Human) this.model.getEntity(entityChanged);
            EntityRefProperty ref = (EntityRefProperty) change.getChangedProperty(entityChanged, StandardPropertyURN.POSITION.toString());
            if (ref.getValue().equals(human.getPosition()) == false) {
                //Position changed so now check if is loaded or not
                if (ref.getValue().equals(getID())) {
                    //Then human is inside of ambulance
                    try {
                        //Time to generate Attended Injured Message
                        AttendedInjuredMessage attendedMessage = new AttendedInjuredMessage(this, loadedHuman.getID());
                        enqueueMessageToSend(attendedMessage);
                    } catch (Exception ex) {
                        //java.util.logging.Logger.getLogger(PlatoonAmbulanceAgent.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
    
    public void updateRulesCounter(Norm norm) {
        System.out.println("updateRulesCounter -> " + norm);
        if (this.rulesCounter.containsKey(norm)) {
            rulesCounter.put(norm, rulesCounter.get(norm) + 1);
        }else {
            rulesCounter.put(norm, 1);
        }
    }
    
    private void chooseRandomTarget() {
        Random rand = new Random();
        if (listTargets.isEmpty() == false) {
            EntityID newTarget = listTargets.get(rand.nextInt(listTargets.size()));
            this.assignedTarget = newTarget;
            this.mainTarget = newTarget;
            listTargets = new ArrayList<>();
        }
    }
    
}