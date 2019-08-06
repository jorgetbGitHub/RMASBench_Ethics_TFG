/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench;

import java.util.Objects;

/**
 *
 * @author jorgetb
 */
public class Message {
    
    protected String message;
    
    
    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        if (object == this) return true;
        if (!(object instanceof Message)) return false;
        if (object instanceof Message) {
            Message msg = (Message) object;
            return this.message.equals(msg.getMessage());
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.message);
        return hash;
    }
    
    public String getMessage() {
        return this.message;
    }
}
