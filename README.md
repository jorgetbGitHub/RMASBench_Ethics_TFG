RMASBench: Multi-Agent Coordination Benchmark
=============================================

This is the main repository of the RMASBench benchmarking tool. There's actually no code in this repository, but it contains git submodules of all necessary software to run the platform and all available algorithms.

Requirements
------------

All software in the RSLBench platform is written in *java 1.7*. Hence, to compile and run it you need to have *java 1.7* installed and set as your default java platform. You can check your java version by running `java -version` in a terminal. You also need both `maven` and `ant` installed in your system to compile the different libraries.

The software is known to work in both *Mac OS X 10.7* with the *Oracle JDK*, and *Ubuntu GNU/linux* with *OpenJDK*. Unfortunately, no version of windows is supported at this time.

Installation
------------

Check out this repository and all its submodules to your computer:

    git clone --recursive https://github.com/RMASBench/RMASBench.git

You will get an RMASBench folder containing 4 sub-folders (projects):

- **BinaryMaxSum.** 
    Library that implements a binary version of MaxSum, including special factors whose messages can be computed more efficiently.

- **Maxsum.**
    Library that implements the standard version of MaxSum.

- **RSLB2.**
    Main tool of the RMASBench platform. This is where most of your work and test will take place, as it allows for an easy interfacing with the robocup rescue simulation platform.

- **BlockadeLoader.**
    Addon simulator that loads blockade definitions from the map file and creates them at the start of the simulation.

- **roborescue**
    Robocup Rescue agent simulation platform.

All this software must be compiled before being able to run the *RSLB2* tool. This can be easily achieved by using ant with the proper target for each subfolder:

    cd RMASBench
    cd BinaryMaxSum; mvn -DskipTests package; cd ..
    cd MaxSum; ant jar; cd ..
    cd roborescue; ant oldsims jars; cd ..
    cd BlockadeLoader; ant jar; cd ..
    cd RSLB2; ant jar; cd ..

If everything compiles well (you can ignore warnings, but not errors!), you are now ready to
start testing. 


Usage
-----

Normally, you will run experiments from within the *RSLB2/boot* folder. Get into that folder and check out the launcher's options:

    cd RSLB2/boot
    ./start.sh -h

You can now launch an example scenario using any of the included algorithms. When testing, include the `-v` flag to enable the simulation viewer. For example, you can run the example scenario with the included example experiment configuration by running:

    ./start.sh -v -c example -m paris -s example-nopolice

This will start a simulation of the example paris scenario without blockades nor police forces. Alternatively, you can run with the police forces and blockades enabled by running the following (notice the `-b` switch which enables blockades, as well as the change of the scenario file to use):

    ./start.sh -v -b -c example -m paris -s example-police

**Warning:** The first time you run a scenario of a map (default is "paris"), the simulator will pre-compute a number of things about the map. This process takes a lot of time (up to one hour depending on your machine), during which you will see the progress on the terminal where you launched the simulation from.

When the simulation finishes, you get a results file for each of the algorithms in your experiment. The paths to these results files are displayed in the terminal (right after all agents finish connecting).


Results file format
-------------------

Each of the results file generated by a simulation run follows the same format. First, there are a number of entries at the beginning that specify the configuration settings used to produce the results. There is one setting per line, and the line format is:

    # setting = value

Next, there is a single line that contains the column names for the actual results. The columns are:

- time: simulation time
- nOnceBurned: number of buildings that have been on fire at any point during the simulation
- nBurning: number of buildings currently burning
- iterations: number of iterations done by the solver (if you setup multiple solvers, there's a separate output file for each of them. As you can see, the file itself contains the solver name to which these results belong)
- NCCCs: number of operations done by the solver
- MessageNum: total number of messages sent by the solver in this simulation step (time)
- MessageBytes: total number of bytes sent by the solver in this simulation step
- OtherNum: number of additional messages sent by the solver (this is weird, used only by max-sum)
- OtherBytes: number of additional bytes sent by the solver (same as above)
- final: utility value achieved by the solver in the last of its iteration
- best: best utility value achieved by the solver in any of its iterations
- final_greedy: utility value obtained by greedy-improving the final solution above
- best_greedy: utility value obtained by greedy-improving the best solution above
- utilities: list of the utilities obtained by the solver at each of its iterations
- score: simulation score at this point in time
- utility: utility obtained by the simulator on the actual used configuration (one of the above, the settings will tell you which of them is being used)
- violations: number of "hard constraint violations". Useless information because there are no hard constraints anymore.
- solvable: also useless
- cpu_time: total time (in ms) employed by this solver at this simulation step

Finally there are the actual results, shown as one line (row) per simulation step.


Troubleshooting
---------------

RMASBench can generate very detailed logs about what is happening within the simulation. Nonetheless, most of that information is hidden by the default logging configuration. Hence, when something goes wrong and you need to figure out what is happening, you can increase the logging level to help diagnose the problem. To do that, modify the "RSLB2/supplement/log4j2.xml" file. This is a standard `log4j2` configuration file which you can customize at your will. For instance, if you want to get all the debug output for the entire RSLBench project (NOT the kernel or other simulators), then comment out this lines:

```xml
    <logger name="RSLBench.Algorithms.BMS" level="INFO" />
    <logger name="RSLBench.Algorithms.DSA" level="INFO" />
```

and change INFO to DEBUG here:

```xml
    <logger name="RSLBench" level="INFO" />
```

Developing your coordination algorithm in RMASBench
---------------------------------------------

You can develop your coordination algorithm in RMASBench by using the coordination API.
This [pdf](https://github.com/RMASBench/RSLB2/raw/master/docs/rmas_benchmark.pdf) document provides some (outdated) info on how to do this. Hence, it is probably better if you look at how current algorightms are implemented (greedy and random are especially easy to grasp).

Resources
--------

* [AAMAS 2013 Video](https://www.youtube.com/watch?v=39y6tkhv5O4)
* [AAMAS 2013 Short Paper](http://www.ifaamas.org/Proceedings/aamas2013/docs/p1195.pdf)
* [OptMAS 2014 Paper](http://www.cs.nmsu.edu/~wyeoh/optmas-dcr2014/docs/optmasdcr2014_submission_9.pdf)