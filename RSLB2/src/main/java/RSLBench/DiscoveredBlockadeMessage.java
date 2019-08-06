/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.Building;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author jorgetb
 */
public class DiscoveredBlockadeMessage extends Message {
    
    private static final String POLICE_FORCE_PREFIX = "[PF]";
    private static final String FIRE_BRIGADE_PREFIX = "[FB]";
    private static final String AMBULANCE_TEAM_PREFIX = "[AT]";
    private static final String CIVILIAN_PREFIX = "[CV]";
    
    private EntityID blockadeID;
    
    public DiscoveredBlockadeMessage(StandardAgent sa, Blockade blockade) throws Exception {
        BuildMessage(sa, blockade);
    }
    
    public DiscoveredBlockadeMessage(String discoveredBlockadeMessage) throws Exception {
        BuildMessage(discoveredBlockadeMessage);
    }
    
    @Override
    public String getMessage() {
        return this.message;
    }
    
    public EntityID getBlockadeID() {
        return this.blockadeID;
    }
    
    private void BuildMessage(StandardAgent sa, Blockade blockade) throws Exception {
        String msg = "";
        String prefix;
        
        if (sa instanceof PlatoonPoliceAgent) {
            prefix = POLICE_FORCE_PREFIX;
        }else if (sa instanceof PlatoonFireAgent) {
            prefix = FIRE_BRIGADE_PREFIX;
        }else if (sa instanceof PlatoonAmbulanceAgent) {
            prefix = AMBULANCE_TEAM_PREFIX;
        }
        else {
            throw new Exception("[DiscoveredBlockadeMessage] agent type unrecognised. Agent ID: " + sa.getID());
        }
        
        String content = " New blockade discovered ";

        msg = prefix + content + blockade;
        
        this.message = msg;
        this.blockadeID = blockade.getID();
    }
    
    private void BuildMessage(String discoveredBlockadeMessage) throws Exception {
        if (discoveredBlockadeMessage.contains(POLICE_FORCE_PREFIX)
                || discoveredBlockadeMessage.contains(FIRE_BRIGADE_PREFIX)
                || discoveredBlockadeMessage.contains(AMBULANCE_TEAM_PREFIX)
                || discoveredBlockadeMessage.contains(CIVILIAN_PREFIX)) {
            if (discoveredBlockadeMessage.contains("New blockade discovered")) {
                String id = discoveredBlockadeMessage.substring(discoveredBlockadeMessage.lastIndexOf("(") + 1, discoveredBlockadeMessage.lastIndexOf(")"));
                this.blockadeID = new EntityID(Integer.valueOf(id));
                this.message = discoveredBlockadeMessage;
            }else{
                throw new Exception("[DiscoveredBlockadeMessage] could not be created from " + discoveredBlockadeMessage + ". Bad format.");
            }
            
        }else{
            throw new Exception("[DiscoveredBlockadeMessage] could not be created from " + discoveredBlockadeMessage + ". Bad format.");
        }
    }
    
}
