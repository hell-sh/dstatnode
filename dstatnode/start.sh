#!/bin/bash

while true
do
	java -Djava.library.path=libs/ -jar dstatnode.jar $1 $2
done
