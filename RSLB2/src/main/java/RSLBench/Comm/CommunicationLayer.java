package RSLBench.Comm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rescuecore2.worldmodel.EntityID;

/**
 * Internal communication layer use by DCOP agents.
 *
 * This communication layer ignores the limitations of the regular roborescue
 * communication layer. This way agents can exchange as many messages as they
 * want, as expected by DCOP algorithms. 
 * 
 * When a message is sent to an agent, it is added to the messageInbox of the
 * recipient (represented by an EntityID). Then the recipient can obtain all its
 * incoming messages when the time comes.
 */
public class CommunicationLayer {

    Map<EntityID, List<Message>> messageInboxes;

    /**
     * Build a new communication layer.
     */
    public CommunicationLayer() {
        messageInboxes = new HashMap<>();
    }

    /**
     * This method memorizes a message in the messageInbox of the recipient.
     *
     * @param agentID: the id of the recipient
     * @param message: the message
     */
    public void send(EntityID agentID, Message message) {
        // Fetch the inbox, creating it if it doesn't exist yet
        List<Message> inbox = messageInboxes.get(agentID);
        if (inbox == null) {
            inbox = new ArrayList<>();
            messageInboxes.put(agentID, inbox);
        }

        inbox.add(message);
    }

    /**
     * This method memorizes a series of messages in the messageInbox of the
     * recipient
     *
     * @param agentID: the id of the recipient
     */
    public void send(EntityID agentID, Collection<Message> messages) {
        for (Message message : messages) {
            send(agentID, message);
        }
    }

    /**
     * This method retrieves the messages from the inbox of an agent
     *
     * @param agentID: the id of the recipient
     * @return a list of alla the messages received
     */
    public List<Message> retrieveMessages(EntityID agentID) {
        List<Message> mesageInbox = messageInboxes.remove(agentID);
        if (mesageInbox == null) {
            mesageInbox = new ArrayList<>();
        }
        return mesageInbox;
    }

}
