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
public class ClearedBlockadeMessage extends Message {
    
    private static final String POLICE_FORCE_PREFIX = "[PF]";
    
    private EntityID blockadeID;
    
    public ClearedBlockadeMessage(StandardAgent sa, EntityID blockadeID) throws Exception {
        BuildMessage(sa, blockadeID);
    }
    
    public ClearedBlockadeMessage(String clearedMessage) throws Exception {
        BuildMessage(clearedMessage);
    }
    
    public EntityID getBlockadeID() {
        return blockadeID;
    }
    
    private void BuildMessage(StandardAgent sa, EntityID blockadeID) throws Exception {
        String message = "";     
        String prefix;
        
        if (sa instanceof PlatoonPoliceAgent) {
            prefix = POLICE_FORCE_PREFIX;
        }
        else {
            throw new Exception("[ClearedBlockadeMessage] agent type unrecognised. Agent ID: " + sa.getID());
        }
        
        String content = " Cleared blockade ";
        
        message = prefix + content + "(" + blockadeID + ")";
        
        this.message = message;
        this.blockadeID = blockadeID;
    }
    
    private void BuildMessage(String clearedMessage) throws Exception {
        if (clearedMessage.contains(POLICE_FORCE_PREFIX)) {
            if (clearedMessage.contains("Cleared blockade")) {
                String id = clearedMessage.substring(clearedMessage.lastIndexOf("(") + 1, clearedMessage.lastIndexOf(")"));
                this.blockadeID = new EntityID(Integer.valueOf(id));
                this.message = clearedMessage;
            }else{
                throw new Exception("[ClearedBlockadeMessage] could not be created from " + clearedMessage + ". Bad format.");
            }
        }else {
            throw new Exception("[ClearedBlockadeMessage] could not be created from " + clearedMessage + ". Bad format.");
        }
    }
}
