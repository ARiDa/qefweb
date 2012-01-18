#!/bin/bash
ssh douglas@$1 'source /etc/profile; killall java; export CODIMS_HOME="/srv/QEF/CoDIMS-tcp/build/classes/codims-home"; export GLOBUS_LOCATION="/srv/Globus"; export GLOBUS_TCP_PORT_RANGE=40000,40100; export PATH=${GLOBUS_LOCATION}/bin:$PATH; globus-start-container -nosec -p' $2 
