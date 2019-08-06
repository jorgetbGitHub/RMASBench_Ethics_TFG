/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Assignment.DCOP;

import RSLBench.Comm.CommunicationLayer;
import RSLBench.Comm.Message;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Default implementation of a DCOP agent, who doesn't send or receive any
 * messages.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public abstract class DefaultDCOPAgent extends AbstractDCOPAgent {

    @Override
    public long getConstraintChecks() {
        return 0;
    }

    @Override
    public void receiveMessages(Collection<Message> messages) {
    }

    @Override
    public Collection<? extends Message> sendMessages(CommunicationLayer com) {
        return new ArrayList<>(0);
    }

}
