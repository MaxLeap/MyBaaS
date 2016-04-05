#!/bin/bash
p=$0
p=${p%/*}
p=${p:1}
p="$PWD"$p
cd $p

mvn clean install -Dmaven.test.skip=true

if [ ! -d build ];then
  mkdir build
fi

if [ ! -d build/bin ];then
  mkdir build/bin
fi

cp -rf webroot build/bin
cp -rf maxleap-baas-apiserver/target/maxleap-baas-apiserver-1.0.0-fat.jar build/bin
cp -rf bootstrap build/bin
cp -rf bootstrap-cloudcode build/bin

echo build successful.