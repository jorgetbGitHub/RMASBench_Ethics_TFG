/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Helpers.Logging;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Log markers to color console output.
 * 
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Markers {
    public static final Marker GREEN = MarkerManager.getMarker("GREEN");
    public static final Marker YELLOW = MarkerManager.getMarker("YELLOW");
    public static final Marker WHITE = MarkerManager.getMarker("WHITE");
    public static final Marker MAGENTA = MarkerManager.getMarker("MAGENTA");
    public static final Marker RED = MarkerManager.getMarker("RED");
    public static final Marker BLUE = MarkerManager.getMarker("BLUE");
    public static final Marker LIGHT_BLUE = MarkerManager.getMarker("LIGHT_BLUE");
}
