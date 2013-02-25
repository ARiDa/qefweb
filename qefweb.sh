#!/bin/bash
APP_DIR=/linkeddata/apps/qefweb/
DIR="$PWD"
cd $APP_DIR

case "$1" in
  start)
	#play start --%prod
	play start --%prod
        ;;
  stop)
	play stop
        ;;

  status)
	play status
        ;;
  *)
        echo "Usage: qefweb {start|stop|status}"
	cd $DIR
        exit 1
esac

cd $DIR

exit 0

