def call(){
   
   sh """
    
   cd UIAutomation/UIAutomation
   config=src/main/resources/config.properties
   echo " " >> src/main/resources/config.properties
   
   if [ -z $ENV_PORT ];then
     echo "base.url=$HTTP_TYPE://$HOST" >> src/main/resources/config.properties
   else
     echo "base.url=$HTTP_TYPE://$HOST:$ENV_PORT" >> src/main/resources/config.properties
   fi

   echo "browser.version=68.0.3440.84" >> src/main/resources/config.properties
   echo "remote=http://m7-prod-ssd001:30000/wd/hub" >> src/main/resources/config.properties
   echo "jdbc.url=jdbc:mysql://$HOST:$DB_PORT" >> src/main/resources/config.properties

   if [ -n "$ENV_WORKSPACE" ];then
     echo "workspace=$ENV_WORKSPACE" >> src/main/resources/config.properties
   fi

   echo "jenkins_workspace=$WORKSPACE" >> src/main/resources/config.properties
   echo "
   browser=$browser
   timeout=20000
   hdfs.nameNodeUrl=$hdfs_url
   hive_Kerberos_open=$hive_Kerberos_open
   isOpenLdb=$isOpenLdb
   " >> src/main/resources/config.properties

   cat src/main/resources/config.properties
   
   if [ $isOpenLdb = true ];then 	
	sed -i '/dango.cases.sdp.sqlPreprocess/d'  testsuite/sdp/sdp+modelCenter.xml
   fi

   if [ $hive_Kerberos_open = CDHOPEN ];then
	sed -i '/dango.cases.sdp.hivecdhker/d'  testsuite/sdp/sdp+modelCenter.xml
   elif [ $hive_Kerberos_open = CDHCLOSE ];then
	sed -i '/dango.cases.sdp.hivecdh\"/d'  testsuite/sdp/sdp+modelCenter.xml
   elif [ $hive_Kerberos_open = C60OPEN ];then
	sed -i '/dango.cases.sdp.hivec60/d'  testsuite/sdp/sdp+modelCenter.xml
   elif [ $hive_Kerberos_open = C70OPEN ];then
	sed -i '/dango.cases.sdp.hivec70/d'  testsuite/sdp/sdp+modelCenter.xml
   elif [ $hive_Kerberos_open = HDP ];then
	sed -i '/dango.cases.sdp.hivehdp/d'  testsuite/sdp/sdp+modelCenter.xml
   elif [ $hive_Kerberos_open = LEAP ];then
	sed -i '/dango.cases.sdp.hiveleap/d' testsuite/sdp/sdp+modelCenter.xml
  fi
    sed -i 's/sdptest+modelCenter/sdp/g' testsuite/sdp/sdp+modelCenter.xml
    sed -i '/dango.cases.modelCenter/d' testsuite/sdp/sdp+modelCenter.xml

  cat testsuite/sdp/sdp+modelCenter.xml

  if [ "$deletedata" = "true" -a "$importdata" = "true" ]; then 
	mvn clean test -Dmaven.test.failure.ignore=true -DsuiteXmlFile=dataCleanDeleteData.xml
    mvn clean test -DsuiteXmlFile=befortest.xml
    
  elif [ "$deletedata" = "false" -a "$importdata" = "true" ]; then 
   mvn clean test -DsuiteXmlFile=befortest.xml  
 
  elif [ "$deletedata" = "true" -a "$importdata" = "false" ]; then 
   mvn clean test -Dmaven.test.failure.ignore=true -DsuiteXmlFile=dataCleanDeleteData.xml
  else
    echo "您没有删除数据也没有引入数据哦"
  fi

    """
}
