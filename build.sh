#! /bin/bash
currentDir=`pwd`
devPluginsDir=/rv/data/server/octagon.dev.robertvokac.lan/octagon-data-dev/data/pluginData/plugins

mvn clean install&&rm $devPluginsDir/octagon-plugin-devops-*.*.*-SNAPSHOT.jar&&mv $currentDir/target/octagon-plugin-devops-*.*.*-SNAPSHOT.jar $devPluginsDir &&cd $currentDir
