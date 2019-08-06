package RSLBench;


import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import rescuecore2.messages.Command;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

import RSLBench.Assignment.Assignment;
import RSLBench.Assignment.CompositeSolver;
import RSLBench.Assignment.Solver;
import RSLBench.Helpers.Exporter;
import RSLBench.Helpers.Logging.Markers;
import RSLBench.Helpers.PathCache.PathDB;
import RSLBench.Helpers.Utility.UtilityFactory;
import RSLBench.Helpers.Utility.ProblemDefinition;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import rescuecore2.messages.control.KASense;
import rescuecore2.misc.Pair;
import rescuecore2.misc.WorkerThread;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.WorldModelListener;

/**
 * It is a "fake" agent that does not appears in the graphic simulation, but that serves as a "station"
 * for all the other agent. It is the agent that starts and updates the simulation and that
 * communicates the new target to each PlatoonFireAgent.
 */
public class CenterAgent extends StandardAgent<Building> {
    private static final Logger Logger = LogManager.getLogger(CenterAgent.class);

    /** Base config key to solver configurations */
    public static final String CONF_KEY_SOLVER= "solver";

    /** Config key of a solver class to run */
    public static final String CONF_KEY_CLASS = "class";

    /** Config key to the maximum time allowed for the main solver */
    public static final String CONF_KEY_TIME = "time";

    private Solver solver = null;
    private Exporter exporter = null;
    private ArrayList<EntityID> fireAgentsIDs = new ArrayList<>();
    private ArrayList<EntityID> policeAgentsIDs = new ArrayList<>();
    private ArrayList<EntityID> ambulanceAgentsIDs = new ArrayList<>();
    private Assignment lastAssignment = new Assignment();
    private List<PlatoonFireAgent> fireAgents;
    private List<PlatoonPoliceAgent> policeAgents;
    private List<PlatoonAmbulanceAgent> ambulanceAgents;
    private List<Blockade> blockades = new ArrayList<>();
    private StatefulKnowledgeSession ksession;
    private Analytics analytics = null;
    
    /**
     * The computation of the road with the highest number trapped people is too expensive to
     * replicate this operation with police forces.
     * Police agents will access to this roadToFocus_N185 if they choose to follow N185.
     * Ambulance Teams will visualize roadToFocus_N185 too to apply norm N94
     */
    private EntityID roadToFocus_N185 = null;
    private EntityID roadToFocus_N194 = null;

    public CenterAgent(List<PlatoonFireAgent> fireAgents,
            List<PlatoonPoliceAgent> policeAgents,
            List<PlatoonAmbulanceAgent> ambulanceAgents,
            StatefulKnowledgeSession ksession) {
    	Logger.info(Markers.BLUE, "Center Agent CREATED");
        
        this.ksession = ksession;
        this.fireAgents = fireAgents;
        for (PlatoonFireAgent fagent : fireAgents) {
            fireAgentsIDs.add(fagent.getID());
            
            // Addition of fire agents into Drools working space 
            //ksession.insert(fagent); if sync implementation works...
            fagent.ksession = ksession;
        }
        this.policeAgents = policeAgents;
        for (PlatoonPoliceAgent pagent : policeAgents) {
            policeAgentsIDs.add(pagent.getID());
            pagent.ksession = ksession;
        }
        this.ambulanceAgents = ambulanceAgents;
        for (PlatoonAmbulanceAgent aagent :ambulanceAgents) {
            ambulanceAgentsIDs.add(aagent.getID());
            aagent.ksession = ksession;
        }
        
        
        //SyncRulesApplication sync = new SyncRulesApplication(fireAgents, ksession); //
        //sync.start();
    }

    @Override
    public String toString()
    {
        return "Center Agent";
    }

    /**
     * Sets up the center agent.
     *
     * At this point, the center agent already has a world model, and has
     * laoded the kernel's configuration. Hence, it is ready to setup the
     * assignment solver(s).
     */
    @Override
    public void postConnect() {
        super.postConnect();

        model.addWorldModelListener(new WorldModelListener<StandardEntity>() {
            @Override
            public void entityAdded(WorldModel<? extends StandardEntity> model,
                    StandardEntity e) {
                if (e instanceof Blockade) {
                    Logger.debug("New blockade introduced: " + e);
                    blockades.add((Blockade)e);
                }
            }

            /**
             * Notifies all agents that a blockade has been removed (cleared).
             * This is necessary because the kernel never informs agents about
             * this fact.
             *
             * The alternative would be to add an EntityListener to each road
             * and work out from there, but memory requirements and efficiency
             * would be much worse that way.
             */
            @Override
            public void entityRemoved(WorldModel<? extends StandardEntity> model,
                    StandardEntity e) {
                if (e instanceof Blockade) {
                    Blockade blockade = (Blockade)e;
                    Logger.debug("Blockade removed: " + e);
                    for (PlatoonFireAgent fireAgent : fireAgents) {
                        fireAgent.removeBlockade(blockade);
                    }
                    for (PlatoonPoliceAgent police : policeAgents) {
                        police.removeBlockade(blockade);
                    }
                    for (PlatoonAmbulanceAgent ambulance : ambulanceAgents) {
                        ambulance.removeBlockade(blockade);
                    }
                }
            }
        });

        initializeParameters();

        if (config.getBooleanValue(Constants.KEY_EXPORT)) {
            exporter = new Exporter();
            exporter.initialize(model, config);
        }

        // Initialize the path cache
        PathDB.initialize(config, model);

        solver = buildSolver();
        solver.initialize(model, config);
        
        //Initialize firesWarned (Included n82 & n85)
        firesWarned = new HashMap<>();
        Collection<StandardEntity> buildings = model.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        for (StandardEntity se :buildings) {
            firesWarned.put(se.getID(), false);
        }
        
        this.blockadesWarned = new HashMap<>();
        this.injuriesWarned = new HashMap<>();
    }

    private void initializeParameters() {
        // Set a UUID for this run
        if (!config.isDefined(Constants.KEY_RUN_ID)) {
            Logger.warn("Setting run id to generated value.");
            config.setValue(Constants.KEY_RUN_ID, UUID.randomUUID().toString());
        }

        // Set the utility function to use
        String utilityClass = config.getValue(Constants.KEY_UTILITY_CLASS);
        UtilityFactory.setClass(utilityClass);

        // Extract the map and scenario names
        String map = config.getValue("gis.map.dir");
        map = map.substring(map.lastIndexOf("/")+1);
        config.setValue(Constants.KEY_MAP_NAME, map);
        String scenario = config.getValue("gis.map.scenario");
        scenario = scenario.substring(scenario.lastIndexOf("/")+1);
        config.setValue(Constants.KEY_MAP_SCENARIO, scenario);

        // The experiment can not start before the agent ignore time
        int ignore = config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY);
        int start  = config.getIntValue(Constants.KEY_START_EXPERIMENT_TIME);
        if (ignore > start) {
            Logger.error("The experiment can't start at time {} because agent commands are ignored until time {}", start, ignore);
            System.exit(0);
        }
    }

    private Solver buildSolver() {
        // Load main solver class
        solver = buildSolver(
                config.getValue(CONF_KEY_SOLVER + "." + CONF_KEY_CLASS),
                config.getIntValue(CONF_KEY_SOLVER + "." + CONF_KEY_TIME));
        Logger.info("Using main solver: {}", solver.getIdentifier());
        config.setValue(Constants.KEY_MAIN_SOLVER, solver.getIdentifier());

        // And any additional test solvers
        CompositeSolver comp = null;
        for(int nTestClass=1;;nTestClass++) {
            String key = CONF_KEY_SOLVER + "." + nTestClass + "." + CONF_KEY_CLASS;
            String className = config.getValue(key, null);
            if (className == null) {
                break;
            }

            if (comp == null) {
                comp = new CompositeSolver(solver);
                solver = comp;
            }

            Solver s = buildSolver(className, config.getIntValue(
                    CONF_KEY_SOLVER + "." + nTestClass + "." + CONF_KEY_TIME));
            Logger.info("Also testing solver: {}", s.getIdentifier());
            comp.addSolver(s);
        }

        return solver;
    }

    private Solver buildSolver(String clazz, int time) {
        try {
            Class<?> c = Class.forName(clazz);
            Object s = c.newInstance();
            if (s instanceof Solver) {
                Solver newSolver = (Solver)s;
                newSolver.setMaxTime(time);
                return newSolver;
            }

        } catch (ClassNotFoundException ex) {
            Logger.fatal("Solver class {} not found!", ex.getMessage());
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.fatal("Unable to instantiate solver {}", ex);
        }

        System.exit(1);
        return null;
    }

    @Override
    protected void think(int time, ChangeSet changed, Collection<Command> heard) {        
        final long startTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis();
        
        if (analytics == null) {
            // Generate Analytics instance
             analytics = new Analytics(new StandardWorldModel(model),
             fireAgents, policeAgents, ambulanceAgents);
        }
        
        //Update road to focus norm 185
        this.updaterRoadToFocus_N185();
        
        //Update road to focus norm 194
        this.updateRoadToFocus_N194();
        
        if (time == config.getIntValue(kernel.KernelConstants.IGNORE_AGENT_COMMANDS_KEY)) {
            // Subscribe to station channel
            Logger.info("CenterAgent [" + getID() + "] sending subscription to radio channel " + Constants.STATION_CHANNEL);
            sendSubscribe(time, Constants.STATION_CHANNEL);
        }
        
        //debug
        /*String content = "";
        if (!heard.isEmpty()) {
            for (Command cmd: heard) {
                if (cmd instanceof AKSpeak) {
                    AKSpeak s = (AKSpeak)cmd;
                    content += "emisor = " + s.getAgentID();
                    content += " ";
                    content += "content = " + new String(s.getContent());
                    content += "\n";
                }
            }
        }
        
        String firesWarnedContent = "";
        
        for (EntityID key : firesWarned.keySet()) {
            firesWarnedContent += String.valueOf(key.getValue()) + " " + firesWarned.get(key) + "  ";
        }
        Logger.info("[CenterAgent] " + "["+ getID() + "] heard -> " + content + " firesWarnedContent= " + firesWarnedContent);
             
        String blockadesWarnedContent = "";
        
        if (!blockadesWarned.isEmpty()) {
            for (EntityID key : blockadesWarned.keySet()) {
                blockadesWarnedContent += String.valueOf(key.getValue()) + " " + blockadesWarned.get(key) + "  ";
            }
            Logger.info("[CenterAgent] " + "["+ getID() + "]  blockadesWarnedContent= " + blockadesWarnedContent);
        }
        
        String injuriesWarnedContent = "";
        
        if (!injuriesWarned.isEmpty()) {
            for (EntityID key : injuriesWarned.keySet()) {
                injuriesWarnedContent += String.valueOf(key.getValue()) + " " + injuriesWarned.get(key) + "  ";
            }
            Logger.info("[CenterAgent] " + "["+ getID() + "]  injuriesWarnedContent= " + injuriesWarnedContent);
        }*/
        //debug
        
        
         //debug   
        /*String totalCivilianBuried = "";
        Collection<StandardEntity> civilians = this.model.getEntitiesOfType(StandardEntityURN.CIVILIAN);
        for (StandardEntity se :civilians) {
            Civilian c = (Civilian) se;
            if (c.isBuriednessDefined() && c.getBuriedness() > 0) {
                totalCivilianBuried += c.getFullDescription();
                totalCivilianBuried += "\n";
            }
        }

        Logger.info("total buried civilians -> " + totalCivilianBuried);*/
        //debug
        
        //Handle heard
        this.handleHeard(heard);
        
        // Restore working memory of Drools, if sync implementation works
        /*for (PlatoonFireAgent fireAgent: fireAgents) {
            ksession.delete(ksession.getFactHandle(fireAgent));
            ksession.insert(fireAgent);
        }*/
       
        // Cleanup non-existant blockades
        ArrayList<EntityID> blockadeIDs = new ArrayList<>();
        Iterator<Blockade> it = blockades.iterator();
        while (it.hasNext()) {
            Blockade blockade = it.next();
            StandardEntity roadEntity = model.getEntity(blockade.getPosition());
            if (roadEntity instanceof Road) {
                List<EntityID> roadBlockades = ((Road)roadEntity).getBlockades();
                if (!roadBlockades.contains(blockade.getID())) {
                    it.remove();
                    model.removeEntity(blockade.getID());
                } else {
                    blockadeIDs.add(blockade.getID());
                }
            }
        }
        long nextTime = System.currentTimeMillis();
        Logger.debug("Cleanup blockades took {} millis", nextTime - lastTime);
        lastTime = nextTime;

        // Report scenario status
        Collection<EntityID> burning = getBurningBuildings();
        Logger.info(Markers.WHITE, "TIME IS {} | {} burning buildings | {} blockades (OLD IMPLEMENTATION)",
                new Object[]{time, burning.size(), blockades.size()});
        
        Collection<EntityID> burningFromMap = getBurningBuildingsFromMap();
        Logger.info(Markers.WHITE, "TIME IS {} | {} burning buildings | {} blockades (NEW IMPLEMENTATION)",
                new Object[]{time, burningFromMap.size(), blockades.size()});
        
        Logger.info("burning content: " + burning + "   burningFromMap content: " + burningFromMap);
        
        // Skip steps until the experiment start time
        if (time < config.getIntValue(Constants.KEY_START_EXPERIMENT_TIME)) {
            Logger.debug("Waiting until experiment starts.");
            return;
        }

        // Simulation termination conditions
        if (burning.isEmpty()) {// && blockades.isEmpty()) {
            Logger.info("All fires extinguished. Good job!");
            //Time to generate analytics (Included)
            Logger.info("Analytics report: -> " + analytics.GenerateReport());
            System.exit(0);
        }

        ArrayList<EntityID> fires = new ArrayList<>(burningFromMap); //new implementations using what agentes share among themselves (burningFromMap)
        
        if (!fires.isEmpty()) { //If It were empty is not possible build ProblemDefinition and generate assignations 
             // Build the problem
            ProblemDefinition problem = new ProblemDefinition(config, fireAgentsIDs,
                    fires, policeAgentsIDs, blockadeIDs, lastAssignment, model);
            nextTime = System.currentTimeMillis();
            Logger.debug("Build problem took {} millis", nextTime - lastTime);
            lastTime = nextTime;

            // Export the problem if required
            if (exporter != null) {
                exporter.export(problem);
            }

            // Compute assignment
            lastAssignment = solver.solve(time, problem);
            nextTime = System.currentTimeMillis();
            Logger.debug("Solving took {} millis", nextTime - lastTime);
            lastTime = nextTime;
        }else {
            lastAssignment = null;
        }
        
        // Send assignment to agents
        sendAssignments(fireAgents);
        sendAssignments(policeAgents);
        sendAssignments(ambulanceAgents);
        
        if(lastAssignment == null) {
            //Regenerate init value
            lastAssignment = new Assignment();
        }

        Logger.info("Full step took {} millis.", lastTime-startTime);
    }
    
    @Override
    protected void handleHeard(Collection<Command> heard) {
        for (Command cmd :heard) {
            if (cmd instanceof AKSpeak) {
                AKSpeak speak = (AKSpeak)cmd;
                String msg = new String(speak.getContent());

                //Handle if discover message
                try {
                    DiscoveredFireMessage discoverMsg = new DiscoveredFireMessage(msg);
                    if ((boolean)this.firesWarned.get(discoverMsg.getLocation()) == false) {
                        this.firesWarned.put(discoverMsg.getLocation(), true);
                    }
                }catch(Exception ex) {
                    //System.out.println("[Rule-N82&N85] Error: " + ex.getMessage());
                }

                //Handle if extinguished message
                try {
                    ExtinguishedFireMessage extinguishedMsg = new ExtinguishedFireMessage(msg);
                    if ((boolean)this.firesWarned.get(extinguishedMsg.getLocation()) == true) {
                        this.firesWarned.put(extinguishedMsg.getLocation(), false);
                    }
                }catch (Exception ex) {
                    //System.out.println("[Rule-N82&N85] Error: " + ex.getMessage());
                }
                
                //Handle if discovered blockade message
                try {
                    DiscoveredBlockadeMessage discoveredBlockadeMessage = new DiscoveredBlockadeMessage(msg);
                    this.blockadesWarned.put(discoveredBlockadeMessage.getBlockadeID(), true);
                }catch (Exception ex) {
                    //Noop
                }
                
                //Handle if cleared blockade message
                try {
                    ClearedBlockadeMessage clearedBlockadeMessage = new ClearedBlockadeMessage(msg);
                    this.blockadesWarned.put(clearedBlockadeMessage.getBlockadeID(), false);
                }catch (Exception ex) {
                    //Noop
                }
                
                try {
                    DiscoveredInjuredMessage discoveredInjuredMessage = new DiscoveredInjuredMessage(msg);
                    this.injuriesWarned.put(discoveredInjuredMessage.getHumanID(), true);
                }catch (Exception ex) {
                    //Noop
                }
                
                try {
                    AttendedInjuredMessage attendedInjuredMessage = new AttendedInjuredMessage(msg);
                    this.injuriesWarned.put(attendedInjuredMessage.getHumanID(), false);
                }catch (Exception ex) {
                    //Noop
                }
            }
        }
    }

    private void sendAssignments(List<? extends PlatoonAbstractAgent> agents) {
        for (PlatoonAbstractAgent agent : agents) {
            if (lastAssignment != null) {
                EntityID assignment = lastAssignment.getAssignment(agent.getID());
                if (assignment == null) {
                    if (agent instanceof PlatoonAmbulanceAgent) {
                        agent.enqueueAssignment(Assignment.UNKNOWN_TARGET_ID);
                    }else {
                        Logger.error("Agent {} got a null assignment!", agent);
                    }
                }
                agent.enqueueAssignment(assignment);
            } else {
                Logger.debug("Agent {} got unkown assigment", agent);
                agent.enqueueAssignment(Assignment.UNKNOWN_TARGET_ID);
            }
        }
    }

    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum()
    {
        return EnumSet.of(StandardEntityURN.FIRE_STATION, StandardEntityURN.AMBULANCE_CENTRE, StandardEntityURN.POLICE_OFFICE);
    }
    
    @Override
    protected void processSense(KASense sense){
        if (analytics != null) {
            analytics.updateAnalytics(sense.getTime(), sense.getChangeSet());
        }
        super.processSense(sense);
    }

    /**
     * It returns the burning buildings
     * @return a collection of burning buildings.
     */
    private Collection<EntityID> getBurningBuildings()
    {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        List<EntityID> result = new ArrayList<>();
        for (StandardEntity next : e)
        {
            if (next instanceof Building)
            {
                Building b = (Building) next;
                if (b.getFieryness() > 0 && b.getFieryness() < 4)
                {
                    EntityID id = b.getID();
                    if (id == null) {
                        Logger.warn("Found a building with no id: {}. Dropped.", b);
                    }
                    result.add(id);
                }
            }
        }
        // Sort by distance
        return result;
    }
    
    private void updaterRoadToFocus_N185() {
        Collection<StandardEntity> humans = this.model.getEntitiesOfType(StandardEntityURN.AMBULANCE_TEAM,
                StandardEntityURN.FIRE_BRIGADE, StandardEntityURN.POLICE_FORCE, StandardEntityURN.CIVILIAN);
        Map<EntityID, Integer> buriedLocations = new HashMap<>();
        Pair<EntityID, Integer> roadToFocus = new Pair<>(null, 0);
        for (StandardEntity se :humans) {
            Human human = (Human)se;
            if (human.isBuriednessDefined() && human.getBuriedness() > 0) {
                Area position = (Area) this.model.getEntity(human.getPosition());
                List<EntityID> neighboursID = position.getNeighbours();
                for (EntityID neighbourID :neighboursID) {
                    Area neighbour = (Area) this.model.getEntity(neighbourID);
                    if (neighbour instanceof Road) {
                        if (buriedLocations.containsKey(neighbourID)) {
                            buriedLocations.put(neighbourID, buriedLocations.get(neighbourID) + 1);
                            
                            if (roadToFocus.second() < buriedLocations.get(neighbourID)) {
                                roadToFocus = new Pair<>(neighbourID, buriedLocations.get(neighbourID));
                            }
                            break;
                        }else {
                            buriedLocations.put(neighbourID, 1);
                            
                            if (roadToFocus.first() == null) {
                                roadToFocus = new Pair<>(neighbourID, 1);
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        this.roadToFocus_N185 = roadToFocus.first();
        
        //Update reference of police agents
        for (PlatoonPoliceAgent pAgent :this.policeAgents) {
            pAgent.roadToFocus_N185 = this.roadToFocus_N185;
        }
        
        //Update reference of ambulance agents
        for (PlatoonAmbulanceAgent aAgent :this.ambulanceAgents) {
            aAgent.roadMostTrapped = this.roadToFocus_N185;
        }
    }
    
    /*private void updateRoadToFocus_N194(){ OLD IMPLEMENTATION
        Collection<StandardEntity> roads = this.model.getEntitiesOfType(StandardEntityURN.ROAD);
        Pair<EntityID, Integer> roadToFocus = new Pair<>(null, 0);
        for (StandardEntity se :roads) {
            Road road = (Road)se;
            if (road.isBlockadesDefined()) {
                List<EntityID> blocks = road.getBlockades();
                if (blocks != null && blocks.isEmpty() == false) {
                    Logger.info("updateRoadToFocus_N194: " + " road->" + road.getFullDescription() + " has " + blocks.size() + " blockades and roadToFocus has " + roadToFocus.second());
                    if (blocks.size() > roadToFocus.second()) {
                        roadToFocus = new Pair<>(road.getID(), blocks.size());
                    }
                }
            }
        }
        
        this.roadToFocus_N194 = roadToFocus.first();
        
        //Update reference of police agents
        for (PlatoonPoliceAgent pAgent :this.policeAgents) {
            pAgent.roadToFocus_N194 = this.roadToFocus_N194;
        } 
    }*/
    
    private void updateRoadToFocus_N194(){
        Collection<StandardEntity> roads = this.model.getEntitiesOfType(StandardEntityURN.ROAD);
        Pair<EntityID, Integer> roadToFocus = new Pair<>(null, 0);
        for (StandardEntity se :roads) {
            Road road = (Road)se;
            if (road.isBlockadesDefined()) {
                List<EntityID> blocks = road.getBlockades();
                if (blocks != null && blocks.isEmpty() == false) {
                    Blockade blockade = (Blockade) this.model.getEntity(blocks.get(0));
                    if (blockade.getRepairCost() > roadToFocus.second()) {
                        roadToFocus = new Pair<>(road.getID(), blockade.getRepairCost());
                    }
                }
            }
        }
        
        this.roadToFocus_N194 = roadToFocus.first();
        
        //Update reference of police agents
        for (PlatoonPoliceAgent pAgent :this.policeAgents) {
            pAgent.roadToFocus_N194 = this.roadToFocus_N194;
        } 
    }
    
    
    /**
     * It returns the refuge buildings
     * @return a collection of refuge buildings.
     */
    private Collection<EntityID> getRefugeBuildings()
    {
        Collection<StandardEntity> e = model.getEntitiesOfType(StandardEntityURN.BUILDING);
        List<EntityID> result = new ArrayList<>();
        for (StandardEntity next : e)
        {
            if (next instanceof Refuge)
            {
              result.add(next.getID());
            }
        }
        return result;
    }
    
    private class SyncRulesApplication extends WorkerThread {
        
        private List<PlatoonFireAgent> fireAgents;
        private StatefulKnowledgeSession ksession;
        
        public SyncRulesApplication(List<PlatoonFireAgent> fireAgents, StatefulKnowledgeSession ksession) {
            this.fireAgents = fireAgents;
            this.ksession = ksession;
        }

        @Override
        protected boolean work() throws InterruptedException { 
            synchronized(ksession) {
                if (checkFireAgentsPath()) {
                    ksession.fireAllRules();
                    for (PlatoonFireAgent fireAgent: fireAgents) {
                        fireAgent.pathAssigned = false;
                    }

                    ksession.notifyAll();
                }
            }
            
            return true;
        }
        
        private boolean checkFireAgentsPath () {
            for (PlatoonFireAgent fireAgent: fireAgents) {
                if (fireAgent.pathAssigned == false) {
                    return false;
                }
            }
            
            return true;
        }
        
    }
}
