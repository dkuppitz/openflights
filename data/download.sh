#!/bin/bash

DIR=`dirname $0`

pushd ${DIR} > /dev/null

wget https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat \
     https://raw.githubusercontent.com/jpatokal/openflights/master/data/airlines.dat \
     https://raw.githubusercontent.com/jpatokal/openflights/master/data/routes.dat

popd > /dev/null
