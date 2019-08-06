/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package RSLBench.Helpers;

import RSLBench.Constants;
import RSLBench.Helpers.Utility.ProblemDefinition;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rescuecore2.config.Config;
import rescuecore2.standard.entities.StandardWorldModel;
import rescuecore2.worldmodel.EntityID;

/**
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class Exporter {
    private static final Logger Logger = LogManager.getLogger(Exporter.class);

    private Config config;
    private int counter;

    public void initialize(StandardWorldModel world, Config config) {
        this.config = config;
        counter = 0;
    }

    public void export(ProblemDefinition utility) {
        counter++;
        File folder = new File(
                config.getValue(Constants.KEY_EXPORT_PATH) + '/' +
                config.getValue(Constants.KEY_RUN_ID));
        if (!folder.exists() && !folder.mkdirs()) {
            Logger.error("Unable to create exports directory \"" + folder.getPath() + "\"");
            System.exit(0);
        }
        export(utility, folder.getPath() + "/" + counter + ".def");
    }

    private void export(ProblemDefinition utility, String file) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file, false))) {
            out.write(Integer.toString(utility.getNumFireAgents()));
            out.write(" ");
            out.write(Integer.toString(utility.getNumFires()));
            out.write(" ");
            out.write(Double.toString(config.getFloatValue(Constants.KEY_UTIL_K)));
            out.write(" ");
            out.write(Double.toString(config.getFloatValue(Constants.KEY_UTIL_ALPHA)));
            out.newLine();

            String separator = "";
            for (EntityID target : utility.getFires()) {
                out.write(separator);
                int count = utility.getRequiredAgentCount(target);
                out.write(Integer.toString(count));
                separator = " ";
            }
            out.newLine();

            for (EntityID agent : utility.getFireAgents()) {
                separator = "";
                for (EntityID target : utility.getFires()) {
                    out.write(separator);
                    out.write(Double.toString(utility.getFireUtility(agent, target)));
                    separator = " ";
                }
                out.newLine();
            }

            out.close();
        } catch (IOException e) {
            Logger.error(e.getLocalizedMessage(), e);
        }
    }

}
