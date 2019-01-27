#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

find -maxdepth 1 -not -name "*_small.jpg" -exec convert {} -resize 540x1100 {}-smaller.jpg \;
find -maxdepth 1 -name "*-smaller.jpg" -exec convert {} -crop 540x1080+0+35 {}-crop.jpg \;
rm *-smaller.jpg
find -maxdepth 1 -name "*-crop.jpg" -exec convert {} -resize 201x400 {}-small.jpg \;
rm *-crop.jpg
rename -f 's/\.jpg-smaller\.jpg-crop\.jpg-small\.jpg/_small\.jpg/g' *-small.jpg
