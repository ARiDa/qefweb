#!/bin/bash

# Make sure that the script "container" is in the path
# "container" is located in GLOBUS_LOCATION
for machine in localhost giga09.lncc.br
do
 echo attempting to start container in machine $machine
 ssh -l username $machine container
 echo machine $machine started
done