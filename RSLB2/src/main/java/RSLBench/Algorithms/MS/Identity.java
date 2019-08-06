/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Algorithms.MS;

import java.util.Objects;
import rescuecore2.worldmodel.EntityID;

/**
 * Identifies Max-Sum variables and nodes.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Identity implements es.csic.iiia.ms.Identity {

    private final EntityID id;

    public Identity(EntityID agent) {
        id = agent;
    }

    public EntityID getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identity other = (Identity) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(es.csic.iiia.ms.Identity o) {
        if (o instanceof Identity) {
            Identity i = (Identity)o;
            return Integer.compare(id.getValue(), i.id.getValue());
        }
        return Integer.compare(hashCode(), o.hashCode());
    }

    @Override
    public String toString() {
        return id.toString();
    }

}
