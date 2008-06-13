#!/bin/bash
for i in $(ls g*.png); do
   convert $i rgba:${i:0:3}.r
done

