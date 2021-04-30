import groovy.grape.Grape

//可以指定maven仓库
//@GrabResolver(name = 'aliyun', root = 'http://maven.aliyun.com/nexus/content/groups/public/')
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7')
@Grab(group = 'org.jsoup', module = 'jsoup', version = '1.10.3')

@Grab('mysql:mysql-connector-java:5.1.38')
//@GrabConfig(systemClassLoader = true)

import org.jsoup.Jsoup
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.transform.Field
import groovy.sql.Sql
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import com.mysql.jdbc.*
import com.mysql.jdbc.Driver
import groovy.sql.*
import java.sql.DriverManager

//可以指定maven仓库
//@GrabResolver(name = 'aliyun', root = 'http://maven.aliyun.com/nexus/content/groups/public/')
//加载数据库连接驱动包
//@Grab('mysql:mysql-connector-java:5.1.25')
//@GrabConfig(systemClassLoader=true)

//global variable
@Field jenkinsURL = "http://auto.4paradigm.com"
@Field int passed
@Field int failed
@Field int skipped
@Field int broken
@Field int unknown
@Field int total
@Field int build_result 
@Field int confirm 

@Field Map<String, Map<String, Integer>> map = new HashMap<>()

@NonCPS
def getResultFromAllure() {

    def reportURL = "/view/API/job/${JOB_NAME}/${BUILD_NUMBER}/allure/"
   
    HTTPBuilder http = new HTTPBuilder(jenkinsURL)
    //根据responsedata中的Content-Type header，调用json解析器处理responsedata
    http.get(path: "${reportURL}widgets/summary.json") { resp, json ->
        println resp.status
        passed = Integer.parseInt((String) json.statistic.passed)
        failed = Integer.parseInt((String) json.statistic.failed)
        skipped = Integer.parseInt((String) json.statistic.skipped)
        broken = Integer.parseInt((String) json.statistic.broken)
        unknown = Integer.parseInt((String) json.statistic.unknown)
        total = Integer.parseInt((String) json.statistic.total)

        if(total==passed && passed != 0) { 
           build_result=1 //测试用例执行成功
        } else{ 
           build_result=2 //测试用例执行失败
           confirm=1   //需要确认结果
        }
        
    }

}


def call() {
    
    getResultFromAllure()
    
    MysqlDataSource ds = new MysqlDataSource()
    ds.user = 'root'
    ds.password = 'root'
    ds.url = 'jdbc:mysql://172.27.234.42:3306/holmes'
    
    def allure_url= "http://auto.4paradigm.com/view/API/job/${JOB_NAME}/${BUILD_NUMBER}/allure/"
    
    Sql sql=Sql.newInstance(ds)
    def sqlString = "INSERT INTO holmes.func_test_summary (name, build_id, version, total, passed, unknown, skipped, failed, broken, create_time,build_result,allure_url,confirm) VALUES ('${JOB_NAME}', '${BUILD_ID}', '${VERSION}', " +
              "${total}, ${passed}, ${unknown}, ${skipped}, ${failed}, ${broken},NOW(),${build_result},${allure_url},${confirm})"

    echo sqlString
    sql.execute(sqlString)
    sql.close()
   

}


