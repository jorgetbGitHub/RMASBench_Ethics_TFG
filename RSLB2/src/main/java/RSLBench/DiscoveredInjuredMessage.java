/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.Human;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author jorgetb
 */
public class DiscoveredInjuredMessage extends Message {
    private static final String POLICE_FORCE_PREFIX = "[PF]";
    private static final String FIRE_BRIGADE_PREFIX = "[FB]";
    private static final String AMBULANCE_TEAM_PREFIX = "[AT]";
    private static final String CIVILIAN_PREFIX = "[CV]";
    
    private EntityID humanID;
    
    public DiscoveredInjuredMessage(StandardAgent sa, Human injured) throws Exception {
        BuildMessage(sa, injured);
    }
    
    public DiscoveredInjuredMessage(String discoveredMessage) throws Exception {
        BuildMessage(discoveredMessage);
    }
    
    public EntityID getHumanID() {
        return humanID;
    }
    
    private void BuildMessage(StandardAgent sa, Human injured) throws Exception {
        String message = "";  
        String prefix;
        if (sa instanceof PlatoonPoliceAgent) {
            prefix = POLICE_FORCE_PREFIX;
        }else if (sa instanceof PlatoonFireAgent) {
            prefix = FIRE_BRIGADE_PREFIX;
        }else if (sa instanceof PlatoonAmbulanceAgent) {
            prefix = AMBULANCE_TEAM_PREFIX;
        }
        else {
            throw new Exception("[DiscoveredInjuriedMessage] agent type unrecognised. Agent ID: " + sa.getID());
        }
        
        String content = " New injured person ";

        message = prefix + content + injured;
        
        this.message = message;
        this.humanID = injured.getID();
    }
    
    private void BuildMessage(String discoveredMessage) throws Exception {
        if (discoveredMessage.contains(POLICE_FORCE_PREFIX)
                || discoveredMessage.contains(FIRE_BRIGADE_PREFIX)
                || discoveredMessage.contains(AMBULANCE_TEAM_PREFIX)
                || discoveredMessage.contains(CIVILIAN_PREFIX)) {
            if (discoveredMessage.contains("New injured person")) {
                String id = discoveredMessage.substring(discoveredMessage.lastIndexOf("(") + 1, discoveredMessage.lastIndexOf(")"));
                this.humanID = new EntityID(Integer.valueOf(id));
                this.message = discoveredMessage;
            }else{
                throw new Exception("[DiscoveredInjuredMessage] could not be created from " + discoveredMessage + ". Bad format.");
            }
            
        }else{
            throw new Exception("[DiscoveredInjuredMessage] could not be created from " + discoveredMessage + ". Bad format.");
        }
    }
}
