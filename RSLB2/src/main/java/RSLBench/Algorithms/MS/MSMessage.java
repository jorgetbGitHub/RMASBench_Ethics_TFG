/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.MS;

import RSLBench.Comm.Message;
import es.csic.iiia.ms.functions.CostFunction;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class MSMessage implements Message {

    public final CostFunction message;
    public final Identity sender;
    public final Identity recipient;

    public MSMessage(CostFunction message, Identity sender, Identity recipient) {
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
    }

    @Override
    public int getBytes() {
        return Message.BYTES_ENTITY_ID + (int)message.getSize() * Message.BYTES_UTILITY_VALUE;
    }

}
