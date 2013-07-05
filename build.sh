#!/bin/bash

mkdir -p build

rm -r forge/mcp/src/minecraft/tpw_rules

cp -r src/tpw_rules forge/mcp/src/minecraft/tpw_rules

cd forge/mcp/bin
find ../src/minecraft/tpw_rules -name "*.scala" | xargs scalac -classpath client_161.jar:../jars/libraries/com/google/guava/guava/14.0/guava-14.0.jar -sourcepath ../src -d minecraft/

cd ..
./reobfuscate.sh

cd ../..

mkdir -p build/zip
rm -r build/zip/*
mkdir build/zip/tpw_rules

cp -r forge/mcp/reobf/minecraft/tpw_rules build/zip/

cp -r assets build/zip/assets

cp pack.mcmeta mcmod.info build/zip

cd build/zip
rm ../crappymod.jar
zip -r ../crappymod.jar *
cd ../..

cp build/crappymod.jar ~/Library/Application\ Support/minecraft/mods/