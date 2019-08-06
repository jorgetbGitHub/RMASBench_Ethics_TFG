/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Comm.Platform;

import static rescuecore2.standard.Constants.MESSAGE_URN_PREFIX;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public enum RSLB2MessageURN {
    /** Assign agents to fires. */
    CA_ASSIGN(MESSAGE_URN_PREFIX + "rest");

    private String urn;

    private RSLB2MessageURN(String urn) {
        this.urn = urn;
    }

    @Override
    public String toString() {
        return urn;
    }

    /**
     * Convert a String to an RSLB2MessageURN.
     * @param s The String to convert.
     * @return An RSLB2MessageURN
     */
    public static RSLB2MessageURN fromString(String s) {
        for (RSLB2MessageURN next : RSLB2MessageURN.values()) {
            if (next.urn.equals(s)) {
                return next;
            }
        }
        throw new IllegalArgumentException(s);
    }
}
