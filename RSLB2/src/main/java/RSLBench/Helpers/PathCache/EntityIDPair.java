/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Helpers.PathCache;

import java.io.Serializable;
import java.util.Objects;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class EntityIDPair implements Comparable<EntityIDPair>, Serializable {
    private static final long serialVersionUID = 1L;

    private final int first;
    private final int second;

    public EntityIDPair(EntityID first, EntityID second) {
        this.first = first.getValue();
        this.second = second.getValue();
    }

    public EntityID first() {
        return new EntityID(first);
    }

    public EntityID second() {
        return new EntityID(second);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.first;
        hash = 97 * hash + this.second;
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
        final EntityIDPair other = (EntityIDPair) obj;
        if (this.first != other.first) {
            return false;
        }
        if (this.second != other.second) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(EntityIDPair o) {
        int result = Integer.compare(first, o.first);
        return result != 0 ? result : Integer.compare(second, o.second);
    }



}
