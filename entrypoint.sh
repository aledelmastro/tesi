#!/bin/sh
set -e
# $*  – bash Parameter Lists all the command line parameters in a single string format.
exec java $JAVA_OPTS -jar otp.jar $*
