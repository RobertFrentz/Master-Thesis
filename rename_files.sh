#!/bin/sh
find /data/csvmetrics -type f -name '*:*' | rename 's/:/-/g'
