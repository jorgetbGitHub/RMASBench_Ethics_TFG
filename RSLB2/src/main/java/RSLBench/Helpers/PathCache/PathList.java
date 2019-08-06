/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Helpers.PathCache;

import java.io.Serializable;
import java.util.List;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class PathList implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int[] elements;

    public PathList(List<EntityID> path) {
        elements = new int[path.size()];
        for (int i=0; i<elements.length; i++) {
            elements[i] = path.get(i).getValue();
        }
    }

    public int[] getElements() {
        return elements;
    }

}
