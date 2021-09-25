#!/bin/bash

set -ex

source ~/.profile
sbt publishLocal
cs bootstrap -f -o product-service.bin --standalone com.example:product-service_3:0.1.0
chmod +x product-service.bin
