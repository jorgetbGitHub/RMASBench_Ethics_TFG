package RSLBench.Search;

import rescuecore2.standard.entities.Area;

public class SearchNode implements Comparable<SearchNode>
{
    private int pathLength;
    private int heuristicValue;

    private Area nodeID;
    private SearchNode parent;

    /**
     * Creates a new search node, which implements the java.lang.Comparable
     * interface. Therefore it can be sorted using a PriorityQueue as open list.
     *
     * @param nodeID
     *            The EntityID of the current node.
     * @param parent
     *            The parent SearchNode from which we got to this node. Used to
     *            construct the path, once the search finishes successful.
     * @param distanceToParent
     *            The distance to the parent node.
     * @param heuristicValue
     *            The heuristic value for this node.
     */
    public SearchNode(Area nodeID, SearchNode parent, int distanceToParent, int heuristicValue)
    {
        this.nodeID = nodeID;
        this.parent = parent;
        if (parent != null)
        {
            this.pathLength = parent.pathLength;
        }
        else
        {
            this.pathLength = 0;
        }
        this.pathLength += distanceToParent;
        this.heuristicValue = heuristicValue;
    }

    public int getPathLength()
    {
        return pathLength;
    }

    public int getHeuristicValue()
    {
        return heuristicValue;
    }

    public Area getNodeID()
    {
        return nodeID;
    }

    public SearchNode getParent()
    {
        return parent;
    }

    @Override
    public int compareTo(SearchNode o)
    {
        return (pathLength + heuristicValue) - (o.pathLength + o.heuristicValue);
    }
}
