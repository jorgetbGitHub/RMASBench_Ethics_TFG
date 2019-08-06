/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Search;

import RSLBench.Helpers.Logging.Markers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.config.Config;
import rescuecore2.config.NoSuchConfigOptionException;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class SearchFactory {

    private static final Logger Logger = LogManager.getLogger(SearchFactory.class);
    public static final String KEY_SEARCH_CLASS = "agent.search.class";

    public static SearchAlgorithm buildSearchAlgorithm(Config config) {
        // construct default search algorithm
        SearchAlgorithm instance = new BreadthFirstSearch();

        // retrieve data from config
        String searchClassName = config.getValue(KEY_SEARCH_CLASS);
        try {
            // instantiate search algorithm
            Class<?> concreteSearchClass = Class.forName(searchClassName);
            Object object = concreteSearchClass.newInstance();
            if (object instanceof SearchAlgorithm) {
                instance = (SearchAlgorithm) object;
                Logger.debug(Markers.LIGHT_BLUE, "Using search class: " + searchClassName);
            } else {
                Logger.error(Markers.RED, searchClassName + " is not a SearchAlgorithm.");
            }
        } catch (NoSuchConfigOptionException e) {
            Logger.warn(Markers.RED, "SearchAlgorithm config not found. Using BreadthFirstSearch.");
        } catch (ClassNotFoundException e) {
            Logger.error("SearchAlgorithm could not be found: " + searchClassName);
        } catch (InstantiationException e) {
            Logger.error("SearchAlgorithm " + searchClassName + " could not be instantiated. (abstract?!)");
        } catch (IllegalAccessException e) {
            Logger.error("SearchAlgorithm " + searchClassName + " must have an empty constructor.");
        }

        return instance;
    }
}
