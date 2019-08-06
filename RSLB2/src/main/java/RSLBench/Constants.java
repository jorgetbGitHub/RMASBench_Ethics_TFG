package RSLBench;

public class Constants {
    public final static int STATION_CHANNEL = 1;
    public final static int PLATOON_CHANNEL = 1;

    /** Path to the cache folder */
    public static final String KEY_CACHE_PATH = "cache.path";

    /** Default path to the cache folder */
    public static final String DEFAULT_CACHE_PATH = "cache/";

    /** The time at which the experiment starts */
    public static final String KEY_START_EXPERIMENT_TIME = "experiment.start_time";

    /** The time at which the experiment ends */
    public static final String KEY_END_EXPERIMENT_TIME = "experiment.end_time";

    /** Whether to export each step's problem (in terms of utilities) or not */
    public static final String KEY_EXPORT = "export";

    /** Path where exported problems should be saved to */
    public static final String KEY_EXPORT_PATH = "export.path";

    /** Fully qualified class of the utility function to employ */
    public static final String KEY_UTILITY_CLASS = "util.class";

    /** Amount of area covered by a single fire brigade. */
    public static final String KEY_AREA_COVERED_BY_FIRE_BRIGADE = "util.fire_brigade_area";

    /** Configuration key to enable/disable interteam coordination */
    public final static String KEY_INTERTEAM_COORDINATION = "agent.interteam";

    /**
     * This factor controls the influence of travel costs on the utility for
     * targets. As bigger as the factor as bigger the influence.
     */
    public static final String KEY_UTIL_TRADEOFF = "util.trade_off";

    /** K value to use in the workload model */
    public static final String KEY_UTIL_K = "util.k";

    /** Alpha value to use in the workload model */
    public static final String KEY_UTIL_ALPHA = "util.alpha";

    /** Key to the penalty applied when a target fire is blocked (M) */
    public static final String KEY_BLOCKED_FIRE_PENALTY = "util.blockade.fire_penalty";

    /** Key to the penalty applied when a target blockade is blocked (Q) */
    public static final String KEY_BLOCKED_POLICE_PENALTY = "util.blockade.police_penalty";

    /** Key to the eta proportionality factor */
    public static final String KEY_POLICE_ETA = "util.police.eta";

    /**
     * when true and there is no target assigned by the station, the agent
     * selects a target on his own (rather than doing nothing)
     */
    public static final String KEY_AGENT_ONLY_ASSIGNED = "agent.only_assigned";

    /** Hysteresis factor to prevent target switching due to pathing isssues. */
    public static final String KEY_UTIL_HYSTERESIS = "util.hysteresis";

    /** Map name */
    public static final String KEY_MAP_NAME = "map.name";

    /** Scenario name */
    public static final String KEY_MAP_SCENARIO = "map.scenario";

    /** Whether to prune the problem or not */
    public static final String KEY_PROBLEM_PRUNE = "problem.prune";

    /** The maximum number of neighbours of an agent or fire in the pruned problem */
    public static final String KEY_PROBLEM_MAXNEIGHBORS = "problem.max_neighbors";

    /** Config key to the results path */
    public static final String KEY_RESULTS_PATH = "results.path";

    /** Config key to the results filename */
    public static final String KEY_RESULTS_PREFIX = "results.prefix";

    /** Key to identify the ID of the experiment run */
    public static final String KEY_RUN_ID = "run";

    /** Key to identify the main solver being run */
    public static final String KEY_MAIN_SOLVER = "main_solver";

}