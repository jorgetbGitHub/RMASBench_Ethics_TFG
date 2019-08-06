/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.standard.entities.Building;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author jorgetb
 */
public class ExtinguishedFireMessage extends Message {
    
    private static final String FIRE_BRIGADE_PREFIX = "[FB]";
    
    private EntityID location;
    
    public ExtinguishedFireMessage(StandardAgent sa, Building location) throws Exception {
        BuildMessage(sa, location);
    }
    
    public ExtinguishedFireMessage(String extinguishedMessage) throws Exception {
        BuildMessage(extinguishedMessage);
    }
    
    public EntityID getLocation() {
        return location;
    }
    
    private void BuildMessage(StandardAgent sa, Building location) throws Exception {
        String message = "";  
        String prefix;
        
        if (sa instanceof PlatoonFireAgent) {
            prefix = FIRE_BRIGADE_PREFIX;
        }
        else {
            throw new Exception("[ExtinguishingFireMessage] agent type unrecognised. Agent ID: " + sa.getID());
        }
        
        String content = " Fire extinguished ";
        
        message = prefix + content + location;
        
        this.message = message;
        this.location = location.getID();
    }
    
    private void BuildMessage(String extinguishedMessage) throws Exception {
        if (extinguishedMessage.contains(FIRE_BRIGADE_PREFIX)) {
            if (extinguishedMessage.contains("Fire extinguished")) {
                String id = extinguishedMessage.substring(extinguishedMessage.lastIndexOf("(") + 1, extinguishedMessage.lastIndexOf(")"));
                this.location = new EntityID(Integer.valueOf(id));
                this.message = extinguishedMessage;
            }else{
                throw new Exception("[ExtinguishedFireMessage] could not be created from " + extinguishedMessage + ". Bad format.");
            }
        }else {
            throw new Exception("[ExtinguishedFireMessage] could not be created from " + extinguishedMessage + ". Bad format.");
        }
    }
}
