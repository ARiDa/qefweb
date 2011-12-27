#!/bin/bash
APP_DIR=/home/regis/qefweb/
DIR="$PWD"
cd $APP_DIR

case "$1" in
  start)
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

