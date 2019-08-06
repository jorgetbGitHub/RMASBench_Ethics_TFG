/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench;

import RSLBench.PlatoonAbstractAgent.Norm;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.AmbulanceTeam;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.properties.IntProperty;

/**
 * This class encapsulate every feature related with the global simulation's result.
 * @author jorgetb
 */
public class Analytics {
    
    private static final Logger Logger = LogManager.getLogger(Analytics.class);
    
    //Fieryness extinguished or not burnt levels to int values (Check StandardEntityConstants)
    private final static int BURNT_OUT = 8;
    private final static int SEVERE = 7;
    private final static int MODERATE = 6;
    private final static int MINOR = 5;
    private final static int WATER_IT = 4;
    
    //Actual CIVILIAN MAX LIFE
    private final static int CIVILIAN_MAX_HEALTH = 10000;
    
    private List<PlatoonFireAgent> fireAgents;
    private List<PlatoonPoliceAgent> policeAgents;
    private List<PlatoonAmbulanceAgent> ambulanceAgents;
   
    
    private int finalTime;
    private float finalScore;
    private int totalWaterConsumed;
    private int totalWaterRefilled;
    private int totalCiviliansPossibleSavedFromFire;
    private int totalCiviliansPossibleSavedFromInjuries;
    private int totalCiviliansPossibleSavedFromInjuries2;
    
    private List<Pair<EntityID, EntityID>> rescuedHumans;
    
    private StandardWorldModel lastWorld;
    
    /// Statistics per TIME ///
    private List<Pair<Integer, Integer>> clearedBlockadesPerTime;
    private List<Pair<Integer, Integer>> extinguishedFiresPerTime;
    private List<Pair<Integer, Integer>> rescuedInjuredsPerTime;
    
    public Analytics(StandardWorldModel lastWorld,
            List<PlatoonFireAgent> fireAgents,
            List<PlatoonPoliceAgent> policeAgents,
            List<PlatoonAmbulanceAgent> ambulanceAgents) {
        
        Logger.info("Analytics created");
        
        this.fireAgents = fireAgents;
        this.policeAgents = policeAgents;
        this.ambulanceAgents = ambulanceAgents;
        
        this.finalTime = 0;
        this.finalScore = 0;
        this.totalWaterConsumed = 0;
        this.totalWaterRefilled = 0;
        this.lastWorld = lastWorld;
        this.totalCiviliansPossibleSavedFromFire = 0;
        this.totalCiviliansPossibleSavedFromInjuries = 0;
        this.totalCiviliansPossibleSavedFromInjuries2 = 0;
        
        rescuedHumans = new ArrayList<>();
        
        clearedBlockadesPerTime = new ArrayList<>();
        extinguishedFiresPerTime = new ArrayList<>();
        rescuedInjuredsPerTime = new ArrayList<>();
    }

    
    public String GenerateReport() {
        String fileContent = "";
        
        try {
            /**
             * Features according to the area affected by fires
             */
            int totalArea = 0; //Total area based exclusively in buildings & refuges
            int totalAreaBurnt = 0; //Total area calcined or just burn it
            int totalAreaCalcined = 0; //Total area calcined
            int totalAreaSeverelyAffected = 0; //Total area severely affected
            int totalAreaModeratelyAffected = 0; //Total area moderately affected
            int totalAreaMinorlyAffected = 0; //Total area minorly affected
            
            
            /**
             * Features according to the level of Fieryness
             */
            int totalBuildings; //Total of building & refuges in the world
            int totalFires = 0; //Total fires ocurred (calcined buildings are included)
            int totalCalcined = 0; // Total calcined fires ocurred
            int totalSevereFires = 0; //Total severe fires ocurred
            int totalModerateFires = 0; //Total moderate fires ocurred
            int totalMinorFires = 0; //Total minor fires ocurred
            int totalWaterIt = 0; //Total buildings that were just water it
            
            Collection<StandardEntity> buildings = lastWorld.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
            int fieryness;
            for (StandardEntity se :buildings) {
                Building building = (Building)se;
                totalArea += building.getTotalArea();
                fieryness = building.getFieryness();
                if (fieryness >= MINOR) {
                    totalFires++;
                    totalAreaBurnt += building.getTotalArea();
                }
                switch(fieryness) {
                    case BURNT_OUT:
                        totalCalcined++;
                        totalAreaCalcined += building.getTotalArea();
                        break;
                    case SEVERE:
                        totalSevereFires++;
                        totalAreaSeverelyAffected += building.getTotalArea();
                        break;
                    case MODERATE:
                        totalModerateFires++;
                        totalAreaModeratelyAffected += building.getTotalArea();
                        break;
                    case MINOR:
                        totalMinorFires++;
                        totalAreaMinorlyAffected += building.getTotalArea();
                        break;
                    case WATER_IT:
                        totalWaterIt++;
                        break;
                }
            }
            
            //Exclusive treatment of Buildings of type Refuge
            int totalRefuges;
            int totalCalcinedRefuges = 0;
            int totalSevereDamagedRefuges = 0;
            int totalModerateDamagedRefuges = 0;
            int totalMinorDamagedRefuges = 0;
            
            Collection<StandardEntity> refuges = lastWorld.getEntitiesOfType(StandardEntityURN.REFUGE);
            
            for (StandardEntity se :refuges) {
                Refuge refuge = (Refuge)se;
                totalArea += refuge.getTotalArea();
                fieryness = refuge.getFieryness();
                if (fieryness >= MINOR) {
                    totalFires++;
                    totalAreaBurnt += refuge.getTotalArea();
                }
                switch(fieryness) {
                    case BURNT_OUT:
                        totalCalcinedRefuges++;
                        totalCalcined++;
                        totalAreaCalcined += refuge.getTotalArea();
                        break;
                    case SEVERE:
                        totalSevereDamagedRefuges++;
                        totalSevereFires++;
                        totalAreaSeverelyAffected += refuge.getTotalArea();
                        break;
                    case MODERATE:
                        totalModerateDamagedRefuges++;
                        totalModerateFires++;
                        totalAreaModeratelyAffected += refuge.getTotalArea();
                        break;
                    case MINOR:
                        totalMinorDamagedRefuges++;
                        totalMinorFires++;
                        totalAreaMinorlyAffected += refuge.getTotalArea();
                        break;
                }
            }
            
            totalBuildings = buildings.size() + refuges.size();
            totalRefuges = refuges.size();
            
            /**
             * Resources
             */
            int totalFireBrigades = lastWorld.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE).size();
            int totalPoliceForces = lastWorld.getEntitiesOfType(StandardEntityURN.POLICE_FORCE).size();
            int totalAmbulanceTeams = lastWorld.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM).size();
            int totalWaterConsumed = this.totalWaterConsumed;
            int totalTime = this.finalTime;
            
            /**
             * Human losses
             */
            int totalDeadPeople = 0;
            int totalDeadFireAgents = 0;
            int totalDeadPoliceAgents = 0;
            int totalDeadAmbulanceAgents = 0;
            int totalDeadCivilians = 0;
            
            /**
             * Civilian Injuries
             */
            int totalCivilians;
            int totalDamagedCivilians = 0; //HP < 100%
            int totalCloseToDieCivilians = 0; //HP <= 25%
            int totalSevereDamagedCivilians = 0; //25% < HP < 50%
            int totalModerateDamagedCivilians = 0; //50% < HP < 75%
            int totalMinorDamagedCivilians = 0; //75% < HP < 100%
            int totalNoDamagedCivilians = 0; //HP = 100%
            
            /**
             * Human buriedness
             */
            int totalPeopleBuried = 0;
            int totalCiviliansBuried = 0;
            int totalFireBrigadesBuried = 0;
            int totalPoliceForcesBuried = 0;
            int totalAmbulanceTeamsBuried = 0;
            
            Collection<StandardEntity> humans = this.lastWorld.getEntitiesOfType(
                    StandardEntityURN.FIRE_BRIGADE,
                    StandardEntityURN.POLICE_FORCE,
                    StandardEntityURN.AMBULANCE_TEAM,
                    StandardEntityURN.CIVILIAN);
            
            totalCivilians = humans.size();
            
            for (StandardEntity se :humans) {
                System.out.println(se.getFullDescription());
                if (se instanceof Human) {
                    Human human = (Human)se;
                    
                    if (human.getHP() <= 0) {
                        if (human instanceof FireBrigade) {
                            totalDeadFireAgents++;
                        }else if (human instanceof PoliceForce) {
                            totalDeadPoliceAgents++;
                        }else if (human instanceof AmbulanceTeam) {
                            totalDeadAmbulanceAgents++;
                        }else {
                            totalDeadCivilians++;
                        }
                        
                        totalDeadPeople++;
                    }
                    
                    if (human instanceof Civilian) {
                        if (human.getHP() < CIVILIAN_MAX_HEALTH && human.getHP() > 0) {
                            if (human.getHP() > CIVILIAN_MAX_HEALTH * 0.75) {
                                totalMinorDamagedCivilians++;
                            }else if (human.getHP() <= CIVILIAN_MAX_HEALTH * 0.75 && human.getHP() > CIVILIAN_MAX_HEALTH * 0.5) {
                                totalModerateDamagedCivilians++;
                            }else if (human.getHP() <= CIVILIAN_MAX_HEALTH * 0.5 && human.getHP() > CIVILIAN_MAX_HEALTH * 0.25) {
                                totalSevereDamagedCivilians++;
                            }else {
                                totalCloseToDieCivilians++;
                            }
                            totalDamagedCivilians++;
                        }
                        
                        if (human.getHP() == CIVILIAN_MAX_HEALTH) {
                            totalNoDamagedCivilians++;
                        }
                    }
                    
                    if (human.isBuriednessDefined() && human.getBuriedness() > 0) {
                        if (human instanceof Civilian) {
                            totalCiviliansBuried++;
                        }else if (human instanceof FireBrigade) {
                            totalFireBrigadesBuried++;
                        }else if (human instanceof PoliceForce) {
                            totalPoliceForcesBuried++;
                        }else if (human instanceof AmbulanceTeam) {
                            totalAmbulanceTeamsBuried++;
                        }else {
                            //Unkown type
                        }
                        
                        totalPeopleBuried++;
                    }
                }
            }
            
            //Ambulance Teams statistics
            int totalPeopleHospitalized = 0;
            int totalCiviliansHospitalized = 0;
            int totalFireBrigadesHospitalized = 0;
            int totalPoliceForcesHospitalized = 0;
            int totalAmbulanceTeamsHospitalized = 0;
            
            int totalPeopleRescued = 0;
            int totalCiviliansRescued = 0;
            int totalFireBrigadesRescued = 0;
            int totalPoliceForcesRescued = 0;
            int totalAmbulanceTeamsRescued = 0;
            
            int totalHumansRescuedFromDestroyedBuildings = 0;
            int totalHumansRescuedFromSevereDamagedBuildings = 0;
            int totalHumansRescuedFromModerateDamagedBuildings = 0;
            int totalHumansRescuedFromMinorDamagedBuildings = 0;
            
            for (PlatoonAmbulanceAgent ambulanceAgent :ambulanceAgents) {
                for (EntityID hospitalized :ambulanceAgent.humansHospitalized) {
                    totalPeopleHospitalized++;
                    Human h = (Human) lastWorld.getEntity(hospitalized);
                    if (h instanceof Civilian) {
                        totalCiviliansHospitalized++;
                    }else if (h instanceof FireBrigade) {
                        totalFireBrigadesHospitalized++;
                    }else if (h instanceof PoliceForce) {
                        totalPoliceForcesHospitalized++;
                    }else if (h instanceof AmbulanceTeam) {
                        totalAmbulanceTeamsHospitalized++;
                    }else {
                        //Unkown type
                    }
                }
            }
            
            for (Pair<EntityID, EntityID> rescued :rescuedHumans) {
                Human injured = (Human) lastWorld.getEntity(rescued.first());
                Building from = (Building) lastWorld.getEntity(rescued.second());
                
                if (from.isBrokennessDefined()) {
                    if (from.getBrokenness() <= 100 && from.getBrokenness() >= 75) {
                        totalHumansRescuedFromDestroyedBuildings++;
                    }else if (from.getBrokenness() < 75 && from.getBrokenness() >= 50) {
                        totalHumansRescuedFromSevereDamagedBuildings++;
                    }else if (from.getBrokenness() < 50 && from.getBrokenness() >= 25) {
                        totalHumansRescuedFromModerateDamagedBuildings++;
                    }else {
                        totalHumansRescuedFromMinorDamagedBuildings++;
                    }
                }
                
                if (injured instanceof Civilian) {
                    totalCiviliansRescued++;
                }else if (injured instanceof FireBrigade) {
                    totalFireBrigadesRescued++;
                }else if (injured instanceof PoliceForce) {
                    totalPoliceForcesRescued++;
                }else if (injured instanceof AmbulanceTeam) {
                    totalAmbulanceTeamsRescued++;
                }else {
                    //Unkown type
                }
                
                totalPeopleRescued++;
            }
            
            //Calculate total humans buried initially
            totalPeopleBuried += totalPeopleRescued;
            totalCiviliansBuried += totalCiviliansRescued;
            totalFireBrigadesBuried += totalFireBrigadesRescued;
            totalPoliceForcesBuried += totalPoliceForcesRescued;
            totalAmbulanceTeamsBuried += totalAmbulanceTeamsRescued;
            
            //Police Statistics
            int totalBlockades = 0;
            int totalBlockadesCleared = 0;
            float clearVelocity = 0;
            int totalPathsCleared = 0;
            int totalPathsToDestroyedBuilding = 0;
            int totalPathsToSevereDamagedBuilding = 0;
            int totalPathsToModerateDamagedBuilding = 0;
            int totalPathsToMinorDamagedBuilding = 0;
            int totalPathsDistanceCleared = 0;
            int totalCost = 0;
            
            for (PlatoonPoliceAgent policeAgent :policeAgents) {
                for (Pair<EntityID, Integer> blockade :policeAgent.clearedBlockades) {
                    totalBlockadesCleared++;
                    totalCost += blockade.second();
                }
                
                for (Pair<Pair<EntityID, EntityID>, Norm> path :policeAgent.clearedPaths) {
                    
                    totalPathsCleared++;
                    totalPathsDistanceCleared += lastWorld.getDistance(path.first().first(), path.first().second());
                    
                    Area area = (Area) lastWorld.getEntity(path.first().second());
                    if (area instanceof Building) {
                        Building goal = (Building) area;
                        
                        if (goal.isBrokennessDefined()) {
                            if (goal.getBrokenness() >= 75) {
                                totalPathsToDestroyedBuilding++;
                            }else if (goal.getBrokenness() >= 50) {
                                totalPathsToSevereDamagedBuilding++;
                            }else if (goal.getBrokenness() >= 25) {
                                totalPathsToModerateDamagedBuilding++;
                            }else {
                                totalPathsToMinorDamagedBuilding++;
                            }
                        }
                    }
                }
            }
            
            //Update here the final record of clearedBlockadesPerTime
            this.clearedBlockadesPerTime.add(new Pair(totalBlockadesCleared, finalTime));
            
            Collection<StandardEntity> blockades = lastWorld.getEntitiesOfType(StandardEntityURN.BLOCKADE);
            totalBlockades = totalBlockadesCleared + blockades.size();
            
            
            //Content
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            
            fileContent = date.toString() + "\n";
            
            if (totalBuildings != 0 && totalFires != 0) {
                fileContent += "\n" + "***Buildings & Fire (Refuges included)***" + "\n";
                fileContent += "Total Buildings: " + totalBuildings + "\n";
                fileContent += "Total damaged Buildings by fire: " + totalFires + " (" + 100*totalFires/(float)totalBuildings + "%) in respect of total buildgins" + "\n";
                fileContent += "Total calcined Buildings: " + totalCalcined + " (" + 100*totalCalcined/(float)totalFires + "%) in respect of total fires" + "\n";
                fileContent += "Total severe damaged Buildings: " + totalSevereFires + " (" + 100*totalSevereFires/(float)totalFires + "%) in respect of total fires" + "\n";;
                fileContent += "Total moderate damaged Buildings: " + totalModerateFires + " (" + 100*totalModerateFires/(float)totalFires + "%) in respect of total fires" + "\n";;
                fileContent += "Total minor damaged Buildings: " + totalMinorFires + " (" + 100*totalMinorFires/(float)totalFires + "%) in respect of total fires" + "\n";;
                fileContent += "Total Buildings water it: " + totalWaterIt + "\n";
            }
            
            fileContent += "\n" + "***Refuges***" + "\n";
            fileContent += "Total Refuges: " + totalRefuges + "\n";
            fileContent += "Total calcined Refuges: " + totalCalcinedRefuges + "\n";
            fileContent += "Total sereve damaged Refuges: " + totalSevereDamagedRefuges + "\n";
            fileContent += "Total moderate damaged Refuges: " + totalModerateDamagedRefuges + "\n";
            fileContent += "Total minor damaged Refuges: " + totalMinorDamagedRefuges + "\n";
            
            fileContent += "\n" + "***Area & Fires*** (area based exclusively in Buildings and Refuges)" + "\n";
            fileContent += "Total area: " + totalArea + "\n";
            fileContent += "Total area burnt: " + totalAreaBurnt + "\n";
            fileContent += "Total area calcined: " + totalAreaCalcined + "\n";
            fileContent += "Total area severely affected: " + totalAreaSeverelyAffected + "\n";
            fileContent += "Total area moderely affected: " + totalAreaModeratelyAffected + "\n";
            fileContent += "Total area minorly affected: " + totalAreaMinorlyAffected + "\n";
            
            fileContent += "\n" + "***Resources***" + "\n";
            fileContent += "Total Fire Brigades: " + totalFireBrigades + "\n";
            fileContent += "Total Police Forces: " + totalPoliceForces + "\n";
            fileContent += "Total Ambulance Team: " + totalAmbulanceTeams + "\n";
            fileContent += "Total Time: " + this.finalTime + "\n";
            
            fileContent += "\n" + "***Human losses***" + "\n";
            fileContent += "Total dead people: " + totalDeadPeople + "\n";
            fileContent += "Total dead Fire Brigades: " + totalDeadFireAgents + "\n";
            fileContent += "Total dead Police Forces: " + totalDeadPoliceAgents + "\n";
            fileContent += "Total dead Ambulance Teams: " + totalDeadAmbulanceAgents + "\n";
            fileContent += "Total dead Civilians: " + totalDeadCivilians + "\n";
            fileContent += "Total dead Civilians inside of extinguished buildings: " + this.totalCiviliansPossibleSavedFromFire + "\n";
            fileContent += "Total dead Civilians inside of ambulance: " + this.totalCiviliansPossibleSavedFromInjuries + "\n";
            fileContent += "Total dead Civilians inside of Refuge: " + this.totalCiviliansPossibleSavedFromInjuries2 + "\n";
            
            fileContent += "\n" + "***Civilian Injuries***" + "\n";
            fileContent += "Total Civilians: " + totalCivilians + "\n";
            fileContent += "Total Damaged Civilians: " + totalDamagedCivilians + "\n";
            fileContent += "Total Close to die Civilians: " + totalCloseToDieCivilians + " (HP < 25%)" + "\n";
            fileContent += "Total Severe Damaged Civilians: " + totalSevereDamagedCivilians + " (25% < HP <= 50%)" + "\n";
            fileContent += "Total Moderate Damaged Civilians: " + totalModerateDamagedCivilians + " (50% < HP <= 75%)" + "\n";
            fileContent += "Total Minor Damaged Civilians: " + totalMinorDamagedCivilians + " (75% < HP < 100%)" + "\n";
            fileContent += "Total No Damaged Civilians: " + totalNoDamagedCivilians + " (HP = 100%)" + "\n";
            
            fileContent += "\n" + "***Fire Brigades statistics***" + "\n";
            fileContent += "Total water consumed: " + this.totalWaterConsumed + "\n";
            fileContent += "Total water refilled: " + this.totalWaterRefilled + "\n";
            fileContent += "Time mean per building extintion: " + finalTime/(float)totalFires + " TIMEs per extintion" + "\n";
            fileContent += "Volume of work: " + totalFires/(float)totalFireBrigades + " fires per fire agent" + "\n";
            
            fileContent += "\n" + "***Ambulance Team statistics***" + "\n";
            fileContent += "Total humans hospitalized: " + totalPeopleHospitalized + "\n";
            fileContent += "Total Civilians hospitalized: " + totalCiviliansHospitalized + "\n";
            fileContent += "Total Fire Brigades hospitalized: " + totalFireBrigadesHospitalized + "\n";
            fileContent += "Total Police Forces hospitalized: " + totalPoliceForcesHospitalized + "\n";
            fileContent += "Total Ambulance Teams hospitalized: " + totalAmbulanceTeamsHospitalized + "\n";
            
            fileContent += "Total Humans buried: " + totalPeopleBuried + "\n";
            fileContent += "Total Humans rescued: " + totalPeopleRescued + " (" + 100*totalPeopleRescued/(float)totalPeopleBuried + "%)" + "\n";
            fileContent += "Total Civilians rescued: " + totalCiviliansRescued + " (" + 100*totalCiviliansRescued/(float)totalCiviliansBuried + "%)" + "\n";
            fileContent += "Total Fire Brigades rescued: " + totalFireBrigadesRescued + " (" + 100*totalFireBrigadesRescued/(float)totalFireBrigadesBuried + "%)" + "\n";
            fileContent += "Total Police Forces rescued: " + totalPoliceForcesRescued + " (" + 100*totalPoliceForcesRescued/(float)totalPoliceForcesBuried + "%)" + "\n";
            fileContent += "Total Ambulance Teams rescued: " + totalAmbulanceTeamsRescued + " (" + 100*totalAmbulanceTeamsRescued/(float)totalAmbulanceTeamsBuried + "%) "+ "\n";
            
            fileContent += "Total Humans rescued from destroyed buildings (brokenness >= 75%): " + totalHumansRescuedFromDestroyedBuildings + "\n";
            fileContent += "Total Humans rescued from severe damaged buildings (50% =< brokenness < 75%): " + totalHumansRescuedFromSevereDamagedBuildings + "\n";
            fileContent += "Total Humans rescued from moderate damaged buildings (25% =< brokenness < 50%): " + totalHumansRescuedFromModerateDamagedBuildings + "\n";
            fileContent += "Total Humans rescued from minor damaged buildings (brokenness < 25%): " + totalHumansRescuedFromMinorDamagedBuildings + "\n";

            fileContent += "\n" + "***Police Statistics***" + "\n";
            fileContent += "Total Blockades cleared: " + totalBlockadesCleared + " (" + 100*totalBlockadesCleared/(float)totalBlockades + "%)" + "\n";
            fileContent += "Total Paths cleared: " + totalPathsCleared + "\n";
            fileContent += "Total Path distance cleared: " + totalPathsDistanceCleared + "\n";
            fileContent += "Total Paths with goal destroyed building: " + totalPathsToDestroyedBuilding + "\n";
            fileContent += "Total Paths with goal severe damaged building: " + totalPathsToSevereDamagedBuilding + "\n";
            fileContent += "Total Paths with goal moderate damaged building: " + totalPathsToModerateDamagedBuilding + "\n";
            fileContent += "Total Paths with goal minor damaged building: " + totalPathsToMinorDamagedBuilding + "\n";
            
            fileContent += "\n" + "***Temporary Variables***" + "\n";
            
            for (Pair<Integer,Integer> rescues :this.rescuedInjuredsPerTime) {
                fileContent += "Total rescued: " + rescues.first() + "\n";
                fileContent += "Rescue velocity: " + rescues.first()/(float)rescues.second() + " rescue/TIME" + "\n";
                fileContent += "TIME: " + rescues.second() + "\n";
            }
            
            fileContent += "\n";
            
            for (Pair<Integer,Integer> extintions :this.extinguishedFiresPerTime) {
                fileContent += "Total extinguished: " + extintions.first() + "\n";
                fileContent += "Extintion velocity: " + extintions.first()/(float)extintions.second() + " extintion/TIME" + "\n";
                fileContent += "TIME: " + extintions.second() + "\n";
            }
            
            fileContent += "\n";
            
            for (Pair<Integer,Integer> cleared :this.clearedBlockadesPerTime) {
                fileContent += "Total cleared: " + cleared.first() + "\n";
                fileContent += "Clear velocity: " + cleared.first()/(float)cleared.second() + " cleared/TIME" + "\n";
                fileContent += "TIME: " + cleared.second() + "\n";
            }
            
            String nameFile = "";
            boolean done = false;
            int i = 0;
            while (!done) {
                nameFile = "report" + i + ".txt";
                File file = new File(nameFile);
                if (!file.exists()) {
                    done = true;
                }else {
                    i++;
                }
            }
            
            Path file = Paths.get(nameFile);
            Files.write(file, Arrays.asList(fileContent.split("\n")), Charset.forName("UTF-8"));
            
            if (false) {
                throw new IOException();
            }

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Analytics.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return fileContent;
    }
    
    
    public void updateAnalytics(int time, ChangeSet changes) {
        //Here world is not updated yet so is possible check differences
        if (time > 22) {
            this.finalTime = time - 23;
            updateWaterConsumed(changes);
            updatePossibleSavedFromFire(changes);
            updateRescuedPeople(changes);
            updateTemporaryVariables(finalTime, changes);
        }
    }
    
    private void updateWaterConsumed(ChangeSet changes) {
        Collection<StandardEntity> fireBrigades = this.lastWorld.getEntitiesOfType(StandardEntityURN.FIRE_BRIGADE);
        for (StandardEntity se :fireBrigades) {
            IntProperty waterConsumed = (IntProperty) changes.getChangedProperty(se.getID(), StandardPropertyURN.WATER_QUANTITY.toString());
            if (waterConsumed != null) {
                FireBrigade fireBrigade  = (FireBrigade)this.lastWorld.getEntity(se.getID());
                
                if (fireBrigade.getWater() > waterConsumed.getValue()) {
                    //Then now has less water than before
                    this.totalWaterConsumed += fireBrigade.getWater() - waterConsumed.getValue();
                }else if (fireBrigade.getWater() == waterConsumed.getValue()) {
                    //Then agent didn't consumed water
                }else {
                    //If agent has more water now than before agent is refilling
                    this.totalWaterRefilled += waterConsumed.getValue() - fireBrigade.getWater();
                }
            }
        }
    }
    
    private void updatePossibleSavedFromFire(ChangeSet changes) {
        Collection<StandardEntity> civils = this.lastWorld.getEntitiesOfType(StandardEntityURN.CIVILIAN);
        for (StandardEntity se :civils) {
            IntProperty hp = (IntProperty)changes.getChangedProperty(se.getID(), StandardPropertyURN.HP.toString());
            if (hp.getValue() <= 0) {
                Civilian civil = (Civilian)this.lastWorld.getEntity(se.getID());
                
                try {
                    if (civil.getHP() > 0) {
                        // Civilian died right now
                        EntityID pos = civil.getPosition();
                        Area areaPos = (Area)this.lastWorld.getEntity(pos);
                        if (areaPos instanceof Building && !(areaPos instanceof Refuge)) {
                            IntProperty fierynessNow = (IntProperty)changes.getChangedProperty(pos, StandardPropertyURN.FIERYNESS.toString());
                            IntProperty fierynessBefore = ((Building)areaPos).getFierynessProperty();

                            if (fierynessNow.getValue() > 3 && fierynessNow.getValue() != StandardEntityConstants.Fieryness.BURNT_OUT.getValue()
                                    && fierynessBefore.getValue() > 3 && fierynessBefore.getValue() != StandardEntityConstants.Fieryness.BURNT_OUT.getValue()) {
                                this.totalCiviliansPossibleSavedFromFire++;
                            }
                        }
                        
                        if (areaPos instanceof Refuge) {
                            this.totalCiviliansPossibleSavedFromInjuries2++;
                        }
                    }
                }catch (ClassCastException ex) {
                    this.totalCiviliansPossibleSavedFromInjuries++;
                }
                
            }
        }
    }
    
    private void updateRescuedPeople(ChangeSet changes) {
        Collection<StandardEntity> humans = lastWorld.getEntitiesOfType(StandardEntityURN.CIVILIAN,
                StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.AMBULANCE_TEAM, StandardEntityURN.POLICE_FORCE);
        for (StandardEntity se :humans) {
            Human human = (Human) se;
            IntProperty buriednness = (IntProperty) changes.getChangedProperty(human.getID(), StandardPropertyURN.BURIEDNESS.toString());
            if (human.isBuriednessDefined() && human.getBuriedness() > 0 && buriednness.getValue() == 0) {
                //This human was rescued this timestep
                rescuedHumans.add(new Pair<>(human.getID(), human.getPosition()));
            }
        }
    }
    
    private void updateTemporaryVariables(int time, ChangeSet changes) {
        //1.Update rescued injureds per time
        this.rescuedInjuredsPerTime.add(new Pair(rescuedHumans.size(), time));
        
        //2.Update fires extinguished per time
        Set<EntityID> entitiesChanged = changes.getChangedEntities();
        Iterator<EntityID> itr = entitiesChanged.iterator();
        int totalExtinguishedNow = 0; //Is necessary to check how many buildings are extinguished this timestep by changes
        int totalReburnNow = 0; //Is necessary to check how many buildings are re-burn this timestep by changes
        while(itr.hasNext()) {
            EntityID next = itr.next();
            IntProperty fieryness = (IntProperty) changes.getChangedProperty(next, StandardPropertyURN.FIERYNESS.toString());
            if (fieryness != null) {
                Building b = (Building) lastWorld.getEntity(next);
                
                if (b.isFierynessDefined() && (b.getFieryness() == 1 || b.getFieryness() == 2 || b.getFieryness() == 3)
                        && (fieryness.getValue() == MINOR || fieryness.getValue() == MODERATE || fieryness.getValue() == SEVERE)) {
                    totalExtinguishedNow++;
                }
                
                if (b.isFierynessDefined() && (b.getFieryness() == MINOR || b.getFieryness() == MODERATE || b.getFieryness() == SEVERE)
                        && (fieryness.getValue() == 1 || fieryness.getValue() == 2 || fieryness.getValue() == 3)) {
                    totalReburnNow++;
                }
                
            }
        }
        
        Collection<StandardEntity> buildings = lastWorld.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        int totalExtinguished = 0;
        for (StandardEntity se :buildings) {
            Building b = (Building) se;
            if (b.isFierynessDefined() && (b.getFieryness() == MINOR || b.getFieryness() == MODERATE || b.getFieryness() == SEVERE)) {
                totalExtinguished++;
            }
        }
        
        System.out.println("[ANALYTICS]: |totalExtinguishedNow] (before) = " + totalExtinguishedNow);
        totalExtinguishedNow = totalExtinguished + totalExtinguishedNow - totalReburnNow;
        System.out.println("[ANALYTICS]: |totalExtinguished = " + totalExtinguished + " |totalExtinguishedNow = " + totalExtinguishedNow + " |totalReburnNow = " + totalReburnNow);
        this.extinguishedFiresPerTime.add(new Pair(totalExtinguishedNow, time));
        
        
        //3.Update cleared blockades per time
        //Note: blockades cannot be update real-time so it will be updated from 1 TIME in the past and completed during report generation
        if (time - 1 > 0) {
            int totalCleared = 0;
            for (PlatoonPoliceAgent policeAgent :policeAgents) {
                totalCleared += policeAgent.clearedBlockades.size();
            }
            
            this.clearedBlockadesPerTime.add(new Pair(totalCleared, time - 1));
        }
        
    }

}
