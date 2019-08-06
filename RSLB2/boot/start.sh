#!/bin/bash

# Cleanup processes when exiting
trap 'kill $PIDS 2>/dev/null' SIGINT SIGTERM EXIT

RSL_SIM_PATH=$(cd ../../roborescue; pwd)
BLOCKADE_SIM_PATH=$(cd ../../BlockadeLoader; pwd)
. functions.sh

processArgs $*

if [ -z "$ONLY_RSLB2" ]; then
	if [ -z "$KERNEL_VIEWER" ]; then
		startKernel --nomenu --autorun --nogui
	else
		startKernel --nomenu --autorun
	fi
	startSims --nogui
fi

if [ -z "$NO_RSLB2" ]; then
    startRslb2
fi

waitUntilFinished $PIDS

if [ -n "$PLOT" ]; then
    for f in results/$UUID-*.dat; do results/plot.sh $f; done
fi