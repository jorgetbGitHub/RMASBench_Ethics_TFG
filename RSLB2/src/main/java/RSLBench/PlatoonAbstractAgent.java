package RSLBench;

import RSLBench.Helpers.Logging.Markers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import RSLBench.Search.DistanceInterface;
import RSLBench.Search.Graph;
import RSLBench.Search.SearchAlgorithm;
import RSLBench.Search.SearchFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import rescuecore2.Constants;
import rescuecore2.messages.Command;
import rescuecore2.misc.Pair;
import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.Refuge;
import rescuecore2.standard.entities.Road;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityConstants;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardPropertyURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.standard.kernel.comms.ChannelCommunicationModel;
import rescuecore2.standard.messages.AKSpeak;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;
import rescuecore2.worldmodel.WorldModelListener;
import rescuecore2.worldmodel.properties.IntProperty;

/**
   Abstract base class for agents.
   @param <E> The subclass of StandardEntity this agent wants to control.
 */
public abstract class PlatoonAbstractAgent<E extends StandardEntity> extends StandardAgent<E> {
    private static final Logger Logger = LogManager.getLogger(PlatoonAbstractAgent.class);

    public static final String THINK_TIME_KEY = "kernel.agents.think-time";

    private static final int RANDOM_WALK_LENGTH = 50;

    //private static final String SAY_COMMUNICATION_MODEL = StandardCommunicationModel.class.getName();
    private static final String SPEAK_COMMUNICATION_MODEL = ChannelCommunicationModel.class.getName();

    /** Queue to receive assignments from the central */
    private BlockingQueue<EntityID> assignmentQueue = new ArrayBlockingQueue<>(1);
    
    protected List<Message> messagesToSend = new ArrayList<>();

    /**
       Whether to use AKSpeak messages or not.
    */
    protected boolean useSpeak;

    /**
       Cache of building IDs.
    */
    protected List<EntityID> buildingIDs;

    /**
       Cache of road IDs.
    */
    protected List<EntityID> roadIDs;

    /**
     * Cache of refuge IDs.
     */
    protected List<EntityID> refugeIDs;

    /**
     * the connectivity graph of all places in the world
     */
    protected Graph connectivityGraph;

    /**
     * a matrix containing the pre-computed distances between each two areas in the world
     */
    protected DistanceInterface distanceMatrix;

    /**
     * The search algorithm.
     */
    protected SearchAlgorithm search;

    protected EntityID randomExplorationGoal = null;
    
    /**
     * an instance of StatefulKnowledgeSession drools rules
     */
    protected StatefulKnowledgeSession ksession;
    
    /**
     * to keep in memory if communication Rules defined into Fire Brigade
     * documentation (N82, N85) that targets multiple type of agents were fired or not
     * this time.
     * 
     * Integer represents TIME
     */
    private int communicationRules_FB = 0;
    
    /**
     * to keep in memory if communication Rules defined into Police Force
     * documentation (N261, N262) that targets multiple type of agents were fired or not
     * this time.
     * 
     * Integer represents TIME
     */
    private int communicationRules_PF = 0;
    
    /**
     * to keep in memory if communication Rules defined into Ambulance Team
     * documentation (N174, N175) that targets multiple type of agents were fired or not
     * this time.
     */
    private int communicationRules_AT = 0;
    
    /**
     * Pair structure to keep in memory if communication specific Rules were fired.
     * For Fire Brigades specific rules are: N90, N91
     * For Police Forced specific communication rules are: N267, 268
     */
    private int communicationSpecific = 0;
    
    
    public void sendDiscoveredFireMessage(DiscoveredFireMessage msg, int time, int channel) {
        if (communicationRules_FB < time) {
            sendSpeak(time, channel, msg.getMessage().getBytes());
            communicationRules_FB = time;
        }
    }
    
    public void sendDiscoveredBlockadeMessage(DiscoveredBlockadeMessage msg, int time, int channel) {
        if (communicationRules_PF < time) {
            sendSpeak(time, channel, msg.getMessage().getBytes());
            communicationRules_PF = time;
        }
    }
    
    public void sendDiscoveredInjuredMessage(DiscoveredInjuredMessage msg, int time, int channel) {
        if (communicationRules_AT < time) {
            sendSpeak(time, channel, msg.getMessage().getBytes());
            communicationRules_AT = time;
        }
    }
    
    public void sendExtinguishedFireMessage(ExtinguishedFireMessage msg, int time, int channel) {
        if (communicationSpecific < time) {
            sendSpeak(time, channel, msg.getMessage().getBytes());
            communicationSpecific = time;
        }
    }
    
    public void sendClearedBlockadeMessage(ClearedBlockadeMessage msg, int time, int channel) {
        if (communicationSpecific < time) {
            sendSpeak(time, channel, msg.getMessage().getBytes());
            communicationSpecific = time;
        }
    }
    
    public void sendAttendedInjuredMessage(AttendedInjuredMessage msg, int time, int channel) {
        if (communicationSpecific < time) {
            sendSpeak(time, channel, msg.getMessage().getBytes());
            communicationSpecific = time;
        }
    }
    
    
    public List<EntityID> path; // Included
    
    public void FireAllRules() {
        synchronized(ksession) {
                
                ksession.insert(this);
                ksession.setGlobal("filterID", this.getID());
                
                if (path != null) {
                    System.out.println("[" + this.getID() + "]" + " This is my path -> " + this.path);
                }
                
                ksession.fireAllRules();
                ksession.delete(ksession.getFactHandle(this));
                
                /*try {
                    
                    Logger.info("I am " + Thread.currentThread().getName() + " and I'm gonna wait right now");
                    this.ksession.wait();
                    Logger.info("I am " + Thread.currentThread().getName() + " and I'm gonna wake up right now");
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(PlatoonFireAgent.class.getName()).log(Level.SEVERE, null, ex);
                }*/
            }
    }

    /**
     * Construct an AbstractSampleAgent.
     */

    @Override
    protected void postConnect() {
        super.postConnect();
        buildingIDs = new ArrayList<>();
        roadIDs = new ArrayList<>();
        refugeIDs = new ArrayList<>();
        for (StandardEntity next : model) {
            if (next instanceof Building) {
                buildingIDs.add(next.getID());
            }
            if (next instanceof Road) {
                roadIDs.add(next.getID());
            }
            if (next instanceof Refuge) {
                refugeIDs.add(next.getID());
            }
        }

        // load correct search algorithm
        search = SearchFactory.buildSearchAlgorithm(config);
        connectivityGraph = Graph.getInstance(model);
        distanceMatrix = new DistanceInterface(model);

        useSpeak = config.getValue(Constants.COMMUNICATION_MODEL_KEY).equals(SPEAK_COMMUNICATION_MODEL);
        Logger.debug("Communcation model: " + config.getValue(Constants.COMMUNICATION_MODEL_KEY));
        Logger.debug(useSpeak ? "Using speak model" : "Using say model");
         model.indexClass(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE, StandardEntityURN.CIVILIAN);
         
        firesWarned = new HashMap<>();
        blockadesWarned = new HashMap<>();
        injuriesWarned = new HashMap<>();
        
        //Initialize firesWarned (Included n82 & n85)
        Collection<StandardEntity> buildings = model.getEntitiesOfType(StandardEntityURN.BUILDING, StandardEntityURN.REFUGE);
        for (StandardEntity se :buildings) {
            firesWarned.put(se.getID(), false);
        }
        
        final PlatoonAbstractAgent self = this;
        model.addWorldModelListener(new WorldModelListener<StandardEntity>() {
            @Override
            public void entityAdded(WorldModel<? extends StandardEntity> model,
                    StandardEntity e) {
                if (e instanceof Blockade) {
                    try {
                        enqueueMessageToSend(new DiscoveredBlockadeMessage(self, (Blockade) e));
                        //messagesToSend.add(new DiscoveredBlockadeMessage(self, (Blockade) e));
                    } catch (Exception ex) {
                    }
                }
            }

            @Override
            public void entityRemoved(WorldModel<? extends StandardEntity> model, StandardEntity e) {
                //Noop
            }
        });
    }
    
    
    /**
     * Allow to check if among collection of messages exists evidences or not about overuse channel
     * @param heard
     * @param channelID
     * @return true if some speak message lost its real content cause overuse of channel, else false
     */
    public boolean anyOverusedMessage(Collection<Command> heard, int channelID) {
        for (Command cmd :heard) {
            if (cmd instanceof AKSpeak) {
                AKSpeak speak = (AKSpeak)cmd;
                String content = new String(speak.getContent());
                if (content.equals("*") && speak.getChannel() == channelID) {
                    //Overused chracter located instead of the real content
                    return true;
                }
            }
        }
        return false;
    }

    public boolean enqueueAssignment(EntityID target) {
        return assignmentQueue.offer(target);
    }

    /**
     * Fetch the latest assignment as computed by the DCOP algorithm.
     *
     * @return EntityID of the target assigned to this agent.
     */
    protected EntityID fetchAssignment() {
        EntityID assignment = null;

        Logger.debug("Agent {} waiting for command.", getID());
        try {
            assignment = assignmentQueue.poll(
                    config.getIntValue(THINK_TIME_KEY) - 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Logger.error("Agent {} unable to fetch its assingment.",
                    ex, getID());
            return null;
        }
        Logger.debug("Agent {} approaching {}!", getID(), assignment);
        return assignment;
    }


    /**
       Construct a random walk starting from this agent's current location to a random building.
       @return A random walk.
    */
    protected List<EntityID> randomWalk() {
        List<EntityID> result = new ArrayList<>(RANDOM_WALK_LENGTH);
        Set<EntityID> seen = new HashSet<>();
        EntityID current = ((Human)me()).getPosition();
        for (int i = 0; i < RANDOM_WALK_LENGTH; ++i) {
            result.add(current);
            seen.add(current);
            List<Area> possible = new ArrayList<>(connectivityGraph.getNeighbors(current));
            Collections.shuffle(possible, random);
            boolean found = false;
            for (Area next : possible) {
                if (seen.contains(next.getID())) {
                    continue;
                }
                current = next.getID();
                found = true;
                break;
            }
            if (!found) {
                // We reached a dead-end.
                break;
            }
        }
        return result;
    }

    protected List<EntityID> randomExplore()
    {
        // check if goal reached
        EntityID position = ((Human)me()).getPosition();
        if (randomExplorationGoal != null)
        {
            int distance = model.getDistance(position, randomExplorationGoal);
            Logger.debug(Markers.BLUE, "RANDOM_EXPLORATION: distance to goal: " + distance);
            if (distance <= 20000)
            {
                randomExplorationGoal = null;
                Logger.debug(Markers.BLUE, "RANDOM_EXPLORATION: goal reached");
            }
        }

        // select new exploration goal
        if (randomExplorationGoal == null)
        {
            //Logger.debugColor("RANDOM_EXPLORATION: selecting new goal", Logger.BG_BLUE);
            Collection<StandardEntity> roads = model.getEntitiesOfType(StandardEntityURN.ROAD);
            Entity[] roadArray = roads.toArray(new Entity[0]);
            int index = random.nextInt(100000);
            int step = getID().getValue();
            while (randomExplorationGoal == null)
            {
                index += step;
                index %= roadArray.length;
                if (index<0 || index > roadArray.length-1) {
                    continue;
                }

                Entity entity = roadArray[index];
                if (model.getDistance(position, entity.getID()) > 20000)
                {
                    randomExplorationGoal = entity.getID();
                    Logger.debug(Markers.BLUE, "RANDOM_EXPLORATION: new goal selected");
                }
            }
        }

        // plan path to goal
        return search.search(position, randomExplorationGoal, connectivityGraph, distanceMatrix).getPathIds();
    }

    public void removeBlockade(Blockade blockade) {
        if (model == null) {
            Logger.error("Null model in agent {}", this);
        }
        if (blockade == null) {
            Logger.error("Null blockade in agent {}", this);
        }
        model.removeEntity(blockade.getID());
    }
    
    //Included
    @Override
    protected void handleHeard(Collection<Command> heard) {
        Map<EntityID, Boolean> firesWarnedChanges = new HashMap<>();
        Map<EntityID, Boolean> blockadesWarnedChanges = new HashMap<>();
        Map<EntityID, Boolean> injuriesWarnedChanges = new HashMap<>();
        
        for (Command cmd :heard) {
            if (cmd instanceof AKSpeak) {
                AKSpeak speak = (AKSpeak)cmd;
                String msg = new String(speak.getContent());

                //Handle if discover message
                try {
                    DiscoveredFireMessage discoverMsg = new DiscoveredFireMessage(msg);
                    this.handleDiscoveredFireMessageReceived(discoverMsg);
                    firesWarnedChanges.put(discoverMsg.getLocation(), true);
                }catch(Exception ex) {
                    //Noop
                }

                //Handle if extinguished message
                try {
                    ExtinguishedFireMessage extinguishedMsg = new ExtinguishedFireMessage(msg);
                    this.handleExtinguishedFireMessageReceived(extinguishedMsg);  
                    firesWarnedChanges.put(extinguishedMsg.getLocation(), false);
                }catch (Exception ex) {
                    //Noop
                }
                
                //Handle if discovered blockade message
                try {
                    DiscoveredBlockadeMessage discoveredBlockadeMessage = new DiscoveredBlockadeMessage(msg);
                    this.handleDiscoveredBlockadeMessageReceived(discoveredBlockadeMessage);
                    blockadesWarnedChanges.put(discoveredBlockadeMessage.getBlockadeID(), true);
                }catch (Exception ex) {
                    //Noop
                }
                
                //Handle if cleared blockade message
                try {
                    ClearedBlockadeMessage clearedBlockadeMessage = new ClearedBlockadeMessage(msg);
                    this.handleClearedBlockadeMessageReceived(clearedBlockadeMessage);
                    blockadesWarnedChanges.put(clearedBlockadeMessage.getBlockadeID(), false);
                }catch (Exception ex) {
                    //Noop
                }
                
                try {
                    DiscoveredInjuredMessage discoveredInjuredMessage = new DiscoveredInjuredMessage(msg);
                    this.handleDiscoveredInjuredMessageReceived(discoveredInjuredMessage);
                    injuriesWarnedChanges.put(discoveredInjuredMessage.getHumanID(), true);
                }catch (Exception ex) {
                    //Noop
                }
                
                try {
                    AttendedInjuredMessage attendedInjuredMessage = new AttendedInjuredMessage(msg);
                    this.handleAttendedInjuredMessageReceived(attendedInjuredMessage);
                    injuriesWarnedChanges.put(attendedInjuredMessage.getHumanID(), false);
                }catch (Exception ex) {
                    //Noop
                }
            }
        }
        
        this.updateMessagesToSend(firesWarnedChanges, blockadesWarnedChanges, injuriesWarnedChanges);
        //this.updateMessageToSend();
    }
    
    @Override
    protected void handlePerception(ChangeSet change) {
        Set<EntityID> entitiesChanged = change.getChangedEntities();
        Iterator<EntityID> itr = entitiesChanged.iterator();
        while (itr.hasNext()) {
            EntityID next = itr.next();
            StandardEntity se = this.model.getEntity(next);
            if (se instanceof Human) 
            {
                //Check here if there are new injured people and generate messages
                Human human = (Human) se;
                IntProperty damage = (IntProperty) change.getChangedProperty(next, StandardPropertyURN.DAMAGE.toString());
                if (damage != null) {
                    //Check if damage has incremented
                    if (human.isDamageDefined() && human.getDamage() < damage.getValue()) {
                        //Then in this timestep this human has suffered some injury
                        if (!injuriesWarned.containsKey(human.getID())
                                || (injuriesWarned.containsKey(human.getID()) && !injuriesWarned.get(human.getID()))) {
                            //Generate Discover Injuried Message
                            try {
                                DiscoveredInjuredMessage injuriedMessage = new DiscoveredInjuredMessage(this, human);
                                enqueueMessageToSend(injuriedMessage);
                            } catch (Exception ex) {
                                //java.util.logging.Logger.getLogger(PlatoonAbstractAgent.class.getName()).log(Level.SEVERE, null, ex);
                            } 
                        }
                        //Update injuriesWarned structure    
                        injuriesWarned.put(human.getID(), true); 
                    }
                }
            }
            
            if (se instanceof Building || se instanceof Refuge) {
                //Check here if there are new buildings burning and generate messages
                Building building = (Building) se;
                IntProperty fieryness = (IntProperty) change.getChangedProperty(next, StandardPropertyURN.FIERYNESS.toString());
                if (fieryness != null) {
                    //Check if currently this building is not burning or extinguished
                    if (building.isFierynessDefined() &&
                            (building.getFieryness() == StandardEntityConstants.Fieryness.UNBURNT.getValue()
                            || building.getFieryness() == StandardEntityConstants.Fieryness.MINOR_DAMAGE.getValue()
                            || building.getFieryness() == StandardEntityConstants.Fieryness.MODERATE_DAMAGE.getValue()
                            || building.getFieryness() == StandardEntityConstants.Fieryness.SEVERE_DAMAGE.getValue())) {
                        //Check if with changes now is burning
                        if (fieryness.getValue() == StandardEntityConstants.Fieryness.HEATING.getValue()
                                || fieryness.getValue() == StandardEntityConstants.Fieryness.BURNING.getValue()
                                || fieryness.getValue() == StandardEntityConstants.Fieryness.INFERNO.getValue()) {
                            //Check if already was comunicated
                            if (!firesWarned.containsKey(building.getID())
                                    || (firesWarned.containsKey(building.getID()) && !firesWarned.get(building.getID()))) {
                                //Generate Discover Fire Message
                                try {
                                    DiscoveredFireMessage fireMessage = new DiscoveredFireMessage(this, building);
                                    enqueueMessageToSend(fireMessage);
                                } catch (Exception ex) {
                                    //java.util.logging.Logger.getLogger(PlatoonAbstractAgent.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            //Update firesWarned structure    
                            firesWarned.put(building.getID(), true);
                        }
                    }
                }
            }
        //(Blockades are managed using listener for the moment)
        
        //Generate Specific Message
        generateSpecificMessage(next, change);
        }
    }
    
    protected abstract void generateSpecificMessage(EntityID entityChanged, ChangeSet change);
    
    protected void handleDiscoveredFireMessageReceived(DiscoveredFireMessage discoverMsg) {
        if ((boolean)this.firesWarned.get(discoverMsg.getLocation()) == false) {
            this.firesWarned.put(discoverMsg.getLocation(), true);
            //System.out.println("[PlatoonAbstractAgent] [" + getID() + "] Building " + discoverMsg.getLocation() + " setted true");
        }  
    }
    
    protected void handleExtinguishedFireMessageReceived(ExtinguishedFireMessage extinguishedMsg) {
        if ((boolean)this.firesWarned.get(extinguishedMsg.getLocation()) == true) {
            this.firesWarned.put(extinguishedMsg.getLocation(), false);
            //System.out.println("[PlatoonAbstractAgent] [" + getID() + "] Building " + extinguishedMsg.getLocation() + " setted false");
        }
    }
    
    protected void handleDiscoveredBlockadeMessageReceived(DiscoveredBlockadeMessage discoveredBlockadeMsg) {
        this.blockadesWarned.put(discoveredBlockadeMsg.getBlockadeID(), true);
        //System.out.println("[PlatoonAbstractAgent] [" + getID() + "] Blockade " + discoveredBlockadeMsg.getBlockadeID() + " setted true");
    }
    
    protected void handleClearedBlockadeMessageReceived(ClearedBlockadeMessage clearedBlockadeMsg) {
        this.blockadesWarned.put(clearedBlockadeMsg.getBlockadeID(), false);
        //System.out.println("[PlatoonAbstractAgent] [" + getID() + "] Blockade " + clearedBlockadeMsg.getBlockadeID() + " setted false");
    }
    
    protected void handleDiscoveredInjuredMessageReceived(DiscoveredInjuredMessage discoveredInjuredMsg) {
        this.injuriesWarned.put(discoveredInjuredMsg.getHumanID(), true);
        //System.out.println("[PlatoonAbstractAgent] [" + getID() + "] Injured human " + discoveredInjuredMsg.getHumanID() + " setted true");
    }
    
    protected void handleAttendedInjuredMessageReceived(AttendedInjuredMessage attendedInjuredMsg) {
        this.injuriesWarned.put(attendedInjuredMsg.getHumanID(), false);
        //System.out.println("[PlatoonAbstractAgent] [" + getID() + "] Injured human " + attendedInjuredMsg.getHumanID() + " setted false");

    }
    
    
    //Included
    public StandardWorldModel getWorld() {
        return this.connectivityGraph.getWorld();
    }
    
    //Included
    public List<EntityID> getRefugesIDs() {
        return this.refugeIDs;
    }
    
    //Included
    public SearchAlgorithm getSearch() {
        return this.search;
    }
    
    //Included
    public Graph getGraph() {
        return this.connectivityGraph;
    }
    
    //Included
    public DistanceInterface getDistanceMatrix() {
        return this.distanceMatrix;
    }
    
     //Included n82 & n85
    @Override
     public void sendSpeak(int time, int channel, byte[] data) {
        send(new AKSpeak(getID(), time, channel, data));
    }
     
     
    public void enqueueMessageToSend(Message message) {
        this.messagesToSend.add(message);
    }
    
    public Collection<DiscoveredFireMessage> getAllDiscoveredFireMessage() {
        Collection<DiscoveredFireMessage> discoveredFireMessages = new ArrayList<>();
        for (Message message :this.messagesToSend) {
            if (message instanceof DiscoveredFireMessage) {
                discoveredFireMessages.add((DiscoveredFireMessage)message);
            }
        }
        
        return discoveredFireMessages;
    }
    
    public Collection<ExtinguishedFireMessage> getAllExtinguishedFireMessage() {
        Collection<ExtinguishedFireMessage> extinguishedFireMessages = new ArrayList<>();
        for (Message message :this.messagesToSend) {
            if (message instanceof ExtinguishedFireMessage) {
                extinguishedFireMessages.add((ExtinguishedFireMessage)message);
            }
        }
        
        return extinguishedFireMessages;
    }
    
    public Collection<DiscoveredBlockadeMessage> getAllDiscoveredBlockadeMessage() {
        Collection<DiscoveredBlockadeMessage> discoveredBlockadeMessages = new ArrayList<>();
        for (Message message :this.messagesToSend) {
            if (message instanceof DiscoveredBlockadeMessage) {
                discoveredBlockadeMessages.add((DiscoveredBlockadeMessage)message);
            }
        }
        
        return discoveredBlockadeMessages;
    }
    
    public Collection<ClearedBlockadeMessage> getAllClearedBlockadeMessage() {
        Collection<ClearedBlockadeMessage> clearedBlockadeMessages = new ArrayList<>();
        for (Message message :this.messagesToSend) {
            if (message instanceof ClearedBlockadeMessage) {
                clearedBlockadeMessages.add((ClearedBlockadeMessage)message);
            }
        }
        
        return clearedBlockadeMessages;
    }
    
    public Collection<DiscoveredInjuredMessage> getAllDiscoveredInjuredMessage() {
        Collection<DiscoveredInjuredMessage> discoveredInjuredMessages = new ArrayList<>();
        for (Message message :this.messagesToSend) {
            if (message instanceof DiscoveredInjuredMessage) {
                discoveredInjuredMessages.add((DiscoveredInjuredMessage)message);
            }
        }
        
        return discoveredInjuredMessages;
    }
    
    public Collection<AttendedInjuredMessage> getAllAttendedInjuredMessage() {
        Collection<AttendedInjuredMessage> attendedInjuredMessages = new ArrayList<>();
        for (Message message :this.messagesToSend) {
            if (message instanceof AttendedInjuredMessage) {
                attendedInjuredMessages.add((AttendedInjuredMessage)message);
            }
        }
        
        return attendedInjuredMessages;
    }
    
    public void sendEnquedDiscoveredFireMessage(int time, int channel) {
        Random rand = new Random();
        List<DiscoveredFireMessage> discoveredFireMessages = (List<DiscoveredFireMessage>) this.getAllDiscoveredFireMessage();
        if (!discoveredFireMessages.isEmpty()) {
            int randIdx = rand.nextInt(discoveredFireMessages.size());
            DiscoveredFireMessage randMessage = discoveredFireMessages.get(randIdx);
            this.sendDiscoveredFireMessage(randMessage, time, channel);
        }
    }
    
    public void sendEnquedExtinguishedFireMessage(int time, int channel) {
        Random rand = new Random();
        List<ExtinguishedFireMessage> extinguishedFireMessages = (List<ExtinguishedFireMessage>) this.getAllExtinguishedFireMessage();
        if (!extinguishedFireMessages.isEmpty()) {
            int randIdx = rand.nextInt(extinguishedFireMessages.size());
            ExtinguishedFireMessage randMessage = extinguishedFireMessages.get(randIdx);
            this.sendExtinguishedFireMessage(randMessage, time, channel);
        }
    }
    
    public void sendEnquedDiscoveredBlockadeMessage(int time, int channel) {
        Random rand = new Random();
        List<DiscoveredBlockadeMessage> discoveredBlockadeMessages = (List<DiscoveredBlockadeMessage>) this.getAllDiscoveredBlockadeMessage();
        if (!discoveredBlockadeMessages.isEmpty()) {
            int randIdx = rand.nextInt(discoveredBlockadeMessages.size());
            DiscoveredBlockadeMessage randMessage = discoveredBlockadeMessages.get(randIdx);
            this.sendDiscoveredBlockadeMessage(randMessage, time, channel);
        }
    }
    
    public void sendEnquedClearedBlockadeMessage(int time, int channel) {
        Random rand = new Random();
        List<ClearedBlockadeMessage> clearedBlockadeMessages = (List<ClearedBlockadeMessage>) this.getAllClearedBlockadeMessage();
        if (!clearedBlockadeMessages.isEmpty()) {
            int randIdx = rand.nextInt(clearedBlockadeMessages.size());
            ClearedBlockadeMessage randMessage = clearedBlockadeMessages.get(randIdx);
            this.sendClearedBlockadeMessage(randMessage, time, channel);
        }
    }
    
    public void sendEnquedDiscoveredInjuredMessage(int time, int channel) {
        Random rand = new Random();
        List<DiscoveredInjuredMessage> discoveredInjuredMessages = (List<DiscoveredInjuredMessage>) this.getAllDiscoveredInjuredMessage();
        if (!discoveredInjuredMessages.isEmpty()) {
            int randIdx = rand.nextInt(discoveredInjuredMessages.size());
            DiscoveredInjuredMessage randMessage = discoveredInjuredMessages.get(randIdx);
            this.sendDiscoveredInjuredMessage(randMessage, time, channel);
        }
    }
    
    public void sendEnquedAttendedInjuredMessage(int time, int channel) {
        Random rand = new Random();
        List<AttendedInjuredMessage> attendedInjuredMessages = (List<AttendedInjuredMessage>) this.getAllAttendedInjuredMessage();
        if (!attendedInjuredMessages.isEmpty()) {
            int randIdx = rand.nextInt(attendedInjuredMessages.size());
            AttendedInjuredMessage randMessage = attendedInjuredMessages.get(randIdx);
            this.sendAttendedInjuredMessage(randMessage, time, channel);
        }
    }
     
    /**
     * Check if in a determined building is empty of civilians or not (Included n37)
     * @param building
     * @return false if there's almost one civilian else false
     */
    public boolean isBuildingEmpty(Building building) {
        Collection<StandardEntity> civiliansID = this.connectivityGraph.getWorld().getEntitiesOfType(StandardEntityURN.CIVILIAN);
        for (StandardEntity se :civiliansID) {
            if (((Civilian)se).getPosition().equals(building.getID())) {
                return false;
            }
        }
        return true;
    }
    
    private void updateMessagesToSend(Map<EntityID, Boolean> firesWarnedChanges,
            Map<EntityID, Boolean> blockadesWarnedChanges,
            Map<EntityID, Boolean> injuriesWarnedChanges) {
        
        Iterator<Message> itr = this.messagesToSend.iterator();
        while (itr.hasNext()) {
            Message msg = itr.next();
            if (msg instanceof DiscoveredFireMessage) {
                if (firesWarnedChanges.containsKey(((DiscoveredFireMessage) msg).getLocation()) && 
                        firesWarnedChanges.get(((DiscoveredFireMessage) msg).getLocation())) {
                    itr.remove();
                }
            }else if (msg instanceof ExtinguishedFireMessage) {
                if (firesWarnedChanges.containsKey(((ExtinguishedFireMessage) msg).getLocation()) &&
                        !firesWarnedChanges.get(((ExtinguishedFireMessage) msg).getLocation())) {
                    itr.remove();
                }
            }else if (msg instanceof DiscoveredBlockadeMessage) {
                if (blockadesWarnedChanges.containsKey(((DiscoveredBlockadeMessage) msg).getBlockadeID()) &&
                        blockadesWarnedChanges.get(((DiscoveredBlockadeMessage)msg).getBlockadeID())) {
                    itr.remove();
                }
            }else if (msg instanceof ClearedBlockadeMessage) {
                if (blockadesWarnedChanges.containsKey(((ClearedBlockadeMessage) msg).getBlockadeID()) &&
                        !blockadesWarnedChanges.get(((ClearedBlockadeMessage)msg).getBlockadeID())) {
                    itr.remove();
                }
            }else if (msg instanceof DiscoveredInjuredMessage) {
                if (injuriesWarnedChanges.containsKey(((DiscoveredInjuredMessage) msg).getHumanID()) &&
                        injuriesWarnedChanges.get(((DiscoveredInjuredMessage)msg).getHumanID())) {
                    itr.remove();
                }
            }else if (msg instanceof AttendedInjuredMessage) {
                if (injuriesWarnedChanges.containsKey(((AttendedInjuredMessage) msg).getHumanID()) &&
                        !injuriesWarnedChanges.get(((AttendedInjuredMessage)msg).getHumanID())) {
                    itr.remove();
                 }
            }else{
                //Unkown type
            } 
        }
    }
    
    public boolean isInjured(EntityID entityID) {
        if (injuriesWarned.containsKey(entityID)) {
            return injuriesWarned.get(entityID);
        }else {
            return false;
        }
    }
    
    public boolean isOnFire(EntityID entityID) {
        if (firesWarned.containsKey(entityID)) {
            return firesWarned.get(entityID);
        }else {
            return false;
        }
    }
    
     public enum Norm {
        N94,
        N97,
        N110,
        N115,
        N116,
        N121,
        N142,
        N151,
        N162,
        N169,
        N185,
        N194,
        N206,
        N218,
        N221,
        N233,
        N241,
        N242,
        N244,
        N245
    }
}
