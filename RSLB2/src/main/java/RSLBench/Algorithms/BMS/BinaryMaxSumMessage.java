/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package RSLBench.Algorithms.BMS;

import RSLBench.Comm.Message;

/**
 * Message sent from a binary max-sum agent to another.
 * <p/>
 * The message includes the originating factor id, the intented recipient id,
 * and the actual single-valued message.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class BinaryMaxSumMessage implements Message {

    private final NodeID senderFactor;
    private final NodeID recipientFactor;
    public final double message;

    /**
     * Build a new binary max-sum message.
     *
     * @param message value of this message
     * @param sender identifier of the sending factor
     * @param recipient identifier of the recipient factor
     */
    public BinaryMaxSumMessage(double message, NodeID senderFactor,
            NodeID recipientFactor) {
        this.senderFactor = senderFactor;
        this.recipientFactor = recipientFactor;
        this.message = message;
    }

    /**
     * Get the identifier of the sender factor.
     * @return identifier of the sender factor.
     */
    public NodeID getSenderFactor() {
        return senderFactor;
    }

    /**
     * Get the identifier of the recipient factor.
     * @return identifier of the recipient factor.
     */
    public NodeID getRecipientFactor() {
        return recipientFactor;
    }

    @Override
    public int getBytes() {
        // ID's are not necessary because it's a fully connected graph, so we
        // can send out a structured large message instead of the individual
        // ones.
        return Message.BYTES_UTILITY_VALUE;
    }
}
