import groovy.grape.Grape


/**
 * Created by sungaofei on 19/3/1.
 */

@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7')
@Grab(group = 'org.jsoup', module = 'jsoup', version = '1.10.3')
import org.jsoup.Jsoup
import groovyx.net.http.HTTPBuilder


import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.transform.Field

//可以指定maven仓库
//@GrabResolver(name = 'aliyun', root = 'http://maven.aliyun.com/nexus/content/groups/public/')
//加载数据库连接驱动包
//@Grab('mysql:mysql-connector-java:5.1.25')
//@GrabConfig(systemClassLoader=true)

//global variable
@Field jenkinsURL = "http://k8s.testing-studio.com:5003"

@Field int passed
@Field int failed
@Field int skipped
@Field int broken
@Field int unknown
@Field int total
@Field Map<String, Map<String, Integer>> map = new HashMap<>()

@NonCPS
def getResultFromAllure() {
    def reportURL = "/job/DailyBuild/${BUILD_NUMBER}/allure/"

//    if (env.BRANCH_NAME != "" && env.BRANCH_NAME != null) {
//        reportURL = "http://k8s.testing-studio.com:5003/job/Daily%20Build/allure/"
//    } else {
//        reportURL = "/view/API/job/${JOB_NAME}/${BUILD_NUMBER}/allure/"
//    }

//    reportURL = "/view/API/job/sage-sdk-test/185/allure/"
    println(jenkinsURL+"${reportURL}widgets/summary.json")

    HTTPBuilder http = new HTTPBuilder("http://k8s.testing-studio.com:5003")
    //根据responsedata中的Content-Type header，调用json解析器处理responsedata
    http.get(path: "${reportURL}widgets/summary.json") { resp, json ->
        println resp.status
        passed = Integer.parseInt((String) json.statistic.passed)
        failed = Integer.parseInt((String) json.statistic.failed)
        skipped = Integer.parseInt((String) json.statistic.skipped)
        broken = Integer.parseInt((String) json.statistic.broken)
        unknown = Integer.parseInt((String) json.statistic.unknown)
        total = Integer.parseInt((String) json.statistic.total)
    }


}


def call() {
    getResultFromAllure()

    getDatabaseConnection(type: 'GLOBAL') {
//        map.each { feature, valueMap ->
//            def sqlString = "INSERT INTO func_test (name, build_id, feature, version, total, passed, unknown, skipped, failed, broken, create_time) VALUES ('${JOB_NAME}', '${BUILD_ID}', '${feature}', '${version}', " +
//                    "${valueMap['total']}, ${valueMap['passed']}, ${valueMap['unknown']}, ${valueMap['skipped']}, ${valueMap['failed']}, ${valueMap['broken']}, NOW())"
//            println(sqlString)
//
//            sql sql: sqlString
//        }

//        def lineCov = 0
//        def branchCov = 0
//        if (coverage != null && coverage != ""){
//            lineCov = getLineCov()
//            branchCov = getBranchCov() * 100
//
//        }

        def sqlString = "INSERT INTO func_test_summary (name, build_id, total, passed, unknown, skipped, failed, broken, create_time) VALUES ('${JOB_NAME}', '${BUILD_ID}', " +
                "${total}, ${passed}, ${unknown}, ${skipped}, ${failed}, ${broken}, NOW())"

        sql sql: sqlString

    }
}


