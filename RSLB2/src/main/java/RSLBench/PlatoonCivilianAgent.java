/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench;

import java.util.Collection;
import java.util.EnumSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.messages.Command;
import rescuecore2.standard.entities.Civilian;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author jorgetb
 */
public class PlatoonCivilianAgent extends PlatoonAbstractAgent<Civilian>{
    
    private static final Logger Logger = LogManager.getLogger(PlatoonCivilianAgent.class);
    
    @Override
    public String toString() {
        return "Civilian agent";
    }


    @Override
    protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
        return EnumSet.of(StandardEntityURN.CIVILIAN);
    }

    @Override
    protected void think(int time, ChangeSet changes, Collection<Command> heard) {
        Civilian me = this.me();
        //Logger.info("[" + getID() + "] I'm a civlian and my position is " + this.model.getEntity(me.getPosition()).getFullDescription());
        
        //debug
        /*String debug = "";
        Collection<StandardEntity> civilians = this.model.getEntitiesOfType(StandardEntityURN.CIVILIAN);
        for (StandardEntity se :civilians) {
            debug += se.getFullDescription() + "\n";
        }
        Logger.info("[" + getID() + "] debug CV -> " + debug);*/
        //debug
        //Noop
    }

    @Override
    protected void generateSpecificMessage(EntityID entityChanged, ChangeSet change) {
        //Noop
    }
    
}
