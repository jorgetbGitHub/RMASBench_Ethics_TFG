/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Helpers.PathCache;

import RSLBench.Constants;
import RSLBench.Search.DistanceInterface;
import RSLBench.Search.Graph;
import RSLBench.Search.SearchAlgorithm;
import RSLBench.Search.SearchFactory;
import RSLBench.Search.SearchResults;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import rescuecore2.config.Config;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class PathDB {
    private static final Logger Logger = LogManager.getLogger(PathDB.class);

    private static File dbFile;
    private static PathDB instance;
    private static StandardWorldModel model;

    private final BTreeMap<EntityIDPair, PathList> paths;

    private PathDB(File f) {
        DB db = DBMaker.newFileDB(f).readOnly().transactionDisable()
                .compressionEnable().closeOnJvmShutdown().make();
        paths = db.getTreeMap("paths");
    }

    public SearchResults search(EntityID from, EntityID to) {
        boolean reverse = false;

        EntityIDPair key = new EntityIDPair(from, to);
        if (!paths.containsKey(key)) {
            key = new EntityIDPair(to, from);
            reverse = true;
        }
        if (!paths.containsKey(key)) {
            Logger.error("Unable to find path from {} ({}) to {} ({})!",
                    from, model.getEntity(from), to, model.getEntity(to));
            throw new RuntimeException("Unable to find path from " + from + " to " + to);
        }
        PathList path = paths.get(key);
        return SearchResults.build(path.getElements(), model, reverse);
    }

    public static PathDB getInstance() {
        if (instance == null) {
            instance = new PathDB(dbFile);
        }
        return instance;
    }

    public static void initialize(Config config, StandardWorldModel model) {
        PathDB.model = model;
        String cachePath = config.getValue(Constants.KEY_CACHE_PATH, Constants.DEFAULT_CACHE_PATH);
        String searchClass = config.getValue(SearchFactory.KEY_SEARCH_CLASS);
        searchClass = searchClass.substring(searchClass.lastIndexOf('.')+1);
        String map = config.getValue(Constants.KEY_MAP_NAME);

        dbFile = new File(cachePath + map + "-" + searchClass + ".paths");
        if (dbFile.exists() && dbFile.isFile()) {
            Logger.info("Using precomputed paths database: {}", dbFile);
            return;
        }

        Logger.info("Building precomputed paths database: {}", dbFile);

        DB db = DBMaker.newFileDB(dbFile).asyncWriteEnable().transactionDisable().compressionEnable().make();
        final BTreeMap<EntityIDPair, PathList> paths = db.getTreeMap("paths");

        // Compute number of road-to-road pairs
        final SearchAlgorithm search = SearchFactory.buildSearchAlgorithm(config);
        final Graph g = Graph.getInstance(model);
        final DistanceInterface distanceMatrix = new DistanceInterface(model);

        final List<StandardEntity> areas = new ArrayList<>(model.getEntitiesOfType(
                StandardEntityURN.ROAD, StandardEntityURN.BUILDING));

        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService service = Executors.newFixedThreadPool(threads);

        final int nAreas = areas.size();
        for (int nArea = 0; nArea < nAreas; nArea++) {
            final int i = nArea;
            service.execute(new Runnable() {
                @Override
                public void run() {
                    final EntityID r1 = areas.get(i).getID();
                    for (int j=i; j < nAreas; j++) {
                        EntityID r2 = areas.get(j).getID();
                        SearchResults result = search.search(r1, r2, g, distanceMatrix);
                        paths.put(new EntityIDPair(r1,r2), new PathList(result.getPathIds()));
                    }
                    Logger.info("Done with area {} of {}", i, areas.size());
                }
            });
        }

        service.shutdown();
        try {
            service.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.error(ex);
            throw new RuntimeException(ex);
        }
        db.close();
    }

}
