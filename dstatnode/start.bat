
@echo off
title DstatNode
:a
java -Djava.library.path=libs/ -jar dstatnode.jar %*
goto a
