package RSLBench.Helpers;

import RSLBench.Assignment.Solver;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import rescuecore2.config.Config;
import rescuecore2.log.Logger;

/**
 * This class collects and writes the stats in the fileName file (default logs/basePackage_groupName_className.dat).
 */
public class Stats
{
    private Config config;
    private Solver solver;
    private Map<String, Object> stats = new LinkedHashMap<>();
    private String fileName;
    private boolean headerWritten;

    public Stats() {}

    public void initialize(Config config, Solver solver, String fileName) {
        this.config = config;
        this.fileName = fileName;
        this.solver = solver;
        this.headerWritten = false;
    }

    /**
     * Stores a reported value for this step.
     *
     * @param statKey Name of the statistic being reported
     * @param statValue Value of that statistic for the current step
     */
    public void report(String statKey, Object statValue) {
        stats.put(statKey, statValue);
    }

    /**
     * Writes the current step's statistics to the report file.
     */
    public void reportStep() {
        if (!headerWritten) {
            writeHeader();
        }

        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true))) {
            String prefix = "";
            for (String statKey : stats.keySet()) {
                out.write(prefix);
                prefix = "\t";
                out.write(stats.get(statKey).toString());
            }
            out.newLine();
            out.close();
        } catch (IOException e) {
            Logger.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Writes the header of the file and the names of the metrics
     * @param fileName:name of the file
     */
    public void writeHeader() {
        headerWritten = true;
        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName, false))) {
            writeLine(out, "# solver: " + solver.getIdentifier());
            writeLine(out, "# max_time: " + solver.getMaxTime());
            for (String key : solver.getUsedConfigurationKeys()) {
                writeLine(out, "# " + key + ": " + config.getValue(key));
            }

            String prefix = "";
            for (String statKey : stats.keySet()) {
                out.write(prefix);
                prefix = "\t";
                out.write(statKey);
            }
            out.newLine();
            out.close();
        } catch (IOException e) {
            Logger.error(e.getLocalizedMessage(), e);
        }
    }

    private void writeLine(BufferedWriter out, String line) throws IOException {
        out.write(line);
        out.newLine();
    }

}
