if "%GLOBUS_LOCATION%" == "" goto nogl
goto gl

:nogl

    echo Error: GLOBUS_LOCATION not set
    pause

:gl
cd ..
cd ..
echo %GLOBUS_LOCATION%
java -classpath $GLOBUS_LOCATION/lib/derby-10.4.1.3.jar:%GLOBUS_LOCATION%/lib/CoDIMS.jar:/srv/QEF/CoDIMS/build/classes ch.epfl.CodimsEnv
@pause