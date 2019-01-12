#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd screenshots
find -maxdepth 1 -name "*.jpg" -exec convert {} -resize 540x1100 {}-small.jpg \;
find -maxdepth 1 -name "*-small.jpg" -exec convert {} -crop 540x1080+0+35 {}-playstore.jpg \;
rm *-small.jpg
rename 's/\.jpg-small\.jpg//g' *.jpg

rm playstore/*
mv *-playstore.jpg playstore/
