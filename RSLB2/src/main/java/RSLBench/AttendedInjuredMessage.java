/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench;

import rescuecore2.standard.components.StandardAgent;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author jorgetb
 */
public class AttendedInjuredMessage extends Message {
    
    private static final String AMBULANCE_TEAM_PREFIX = "[AT]";
    private EntityID humanID;
    
    public AttendedInjuredMessage(StandardAgent sa, EntityID humanID) throws Exception {
        BuildMessage(sa, humanID);
    }
    
    public AttendedInjuredMessage(String attendedMessage) throws Exception {
        BuildMessage(attendedMessage);
    }
    
    public EntityID getHumanID() {
        return humanID;
    }
    
    private void BuildMessage(StandardAgent sa, EntityID humanID) throws Exception {
        String message = "";     
        String prefix;
        
        if (sa instanceof PlatoonAmbulanceAgent) {
            prefix = AMBULANCE_TEAM_PREFIX;
        }
        else {
            throw new Exception("[AttendedInjuredMessage] agent type unrecognised. Agent ID: " + sa.getID());
        }
        
        String content = " Attended injured ";
        
        message = prefix + content + "(" + humanID + ")";
        
        this.message = message;
        this.humanID = humanID;
    }
    
    private void BuildMessage(String attendedMessage) throws Exception {
        if (attendedMessage.contains(AMBULANCE_TEAM_PREFIX)) {
            if (attendedMessage.contains("Attended injured")) {
                String id = attendedMessage.substring(attendedMessage.lastIndexOf("(") + 1, attendedMessage.lastIndexOf(")"));
                this.humanID = new EntityID(Integer.valueOf(id));
                this.message = attendedMessage;
            }else{
                throw new Exception("[AttendedInjuredMessage] could not be created from " + attendedMessage + ". Bad format.");
            }
        }else {
            throw new Exception("[AttendedInjuredMessage] could not be created from " + attendedMessage + ". Bad format.");
        }
    }
}
