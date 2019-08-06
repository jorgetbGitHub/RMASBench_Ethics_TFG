/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Search;

import java.util.ArrayList;
import java.util.List;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Blockade;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 * Class that holds the results of the path search of an agent.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class SearchResults {

    private List<EntityID> pathIds = new ArrayList<>();
    private List<Blockade> pathBlocks = new ArrayList<>();

    public List<EntityID> getPathIds() {
        return pathIds;
    }

    public void setPathIds(List<EntityID> pathIds) {
        this.pathIds = pathIds;
    }

    public List<Blockade> getPathBlocks() {
        return pathBlocks;
    }

    public void setPathBlocks(List<Blockade> pathBlocks) {
        this.pathBlocks = pathBlocks;
    }

    private void computeBlocks(StandardWorldModel model) {
        for (EntityID areaID : pathIds) {
            // Skip non-area parts
            StandardEntity e = model.getEntity(areaID);
            if (!(e instanceof Area)) {
                continue;
            }

            // Find blockades and add them if their cost is > 0 (removed blockades may remain here
            // with a cost of 0)
            Area area = (Area)e;
            if (area.isBlockadesDefined()) {
                for (EntityID blockadeID : area.getBlockades()) {
                    Blockade blockade = (Blockade)model.getEntity(blockadeID);
                    if (blockade.getRepairCost() > 0) {
                        pathBlocks.add(blockade);
                    }
                }
            }
        }
    }

    public static SearchResults build(int[] path, StandardWorldModel model, boolean reverse) {
        SearchResults result = new SearchResults();
        ArrayList<EntityID> pathIds = new ArrayList<EntityID>();
        if (reverse) {
            for (int i=path.length-1; i>=0; i--) {
                pathIds.add(new EntityID(path[i]));
            }
        } else {
            for (int i=0; i<path.length; i++) {
                pathIds.add(new EntityID(path[i]));
            }
        }
        result.setPathIds(pathIds);
        result.computeBlocks(model);
        return result;
    }

}
