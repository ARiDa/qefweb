#!/bin/bash

# Make sure that the script "container" is in the path
# "container" is located in GLOBUS_LOCATION
for machine in mymachine1.mydomain.com mymachine2.mydomain.com mymachine3.mydomain.com
do
 ssh -l username $machine pkill java
 echo machine $machine stopped
done

