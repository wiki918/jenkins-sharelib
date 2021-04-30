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
@Field jenkinsURL = "http://auto.4paradigm.com"

@Field int passed
@Field int failed
@Field int skipped
@Field int broken
@Field int unknown
@Field int total
@Field Map<String, Map<String, Integer>> map = new HashMap<>()

@NonCPS
def getResultFromAllure() {
    def reportURL = ""
    if (env.BRANCH_NAME != "" && env.BRANCH_NAME != null) {
        reportURL = "/view/API/job/${jobName}/job/${env.BRANCH_NAME}/${BUILD_NUMBER}/allure/"
    } else {
        reportURL = "/view/API/job/${JOB_NAME}/${BUILD_NUMBER}/allure/"
    }

//    reportURL = "/view/API/job/sage-sdk-test/185/allure/"

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
    }

    http.get(path: "${reportURL}data/behaviors.json") { resp, json ->
        List featureJson = json.children

        for (int i = 0; i < featureJson.size(); i++) {
            String featureName = featureJson.get(i).name
            Map<String, Integer> results = new HashMap<>()
            results['passed'] = 0
            results['failed'] = 0
            results['skipped'] = 0
            results['broken'] = 0
            results['unknown'] = 0


            List storyJson = featureJson.get(i).children
            for (int j = 0; j < storyJson.size(); j++) {

                List caseJson = storyJson.get(j).children
                for (int k = 0; k < caseJson.size(); k++) {
                    def caseInfo = caseJson.get(k)
                    String status = caseInfo.status
                    int num = results.get(status) + 1
                    results[status] = num

                }
            }
            int total = 0
            results.each { key, value ->
                total = total + value
            }
            results['total'] = total
            map.put(featureName, results)
        }


    }
}

def int getLineCov(){
    def htmlurl = "${jenkinsURL}/view/API/job/${JOB_NAME}/${BUILD_NUMBER}/_e4bba3_e7a081_e8a686_e79b96_e78e87_e68aa5_e5918a/index.html"
    String doc = Jsoup.connect(htmlurl).get().getElementsByClass("pc_cov").text();
    int cov = Integer.parseInt(doc.replace("%", ""))
    println("当前行覆盖率为 ${cov}")
    return  cov
}

def int getBranchCov(){
    def htmlurl = "${jenkinsURL}/view/API/job/${JOB_NAME}/${BUILD_NUMBER}/_e4bba3_e7a081_e8a686_e79b96_e78e87_e68aa5_e5918a/index.html"
    String branchAll = Jsoup.connect(htmlurl).get().select(".total > :nth-child(5)").text();
    String branchPartial = Jsoup.connect(htmlurl).get().select(".total > :nth-child(6)").text();

    println("all branch number: ${branchAll}")
    println("cover branch number: ${branchPartial}")

    def cov = Integer.parseInt(branchPartial)/Integer.parseInt(branchAll)
    println("the branch cov is ${cov}")



    return cov

}

def call() {
    def version = "release/3.8.2"
    getResultFromAllure()

    getDatabaseConnection(type: 'GLOBAL') {
        map.each { feature, valueMap ->
            def sqlString = "INSERT INTO func_test (name, build_id, feature, version, total, passed, unknown, skipped, failed, broken, create_time) VALUES ('${JOB_NAME}', '${BUILD_ID}', '${feature}', '${version}', " +
                    "${valueMap['total']}, ${valueMap['passed']}, ${valueMap['unknown']}, ${valueMap['skipped']}, ${valueMap['failed']}, ${valueMap['broken']}, NOW())"
            println(sqlString)

            sql sql: sqlString
        }

        def lineCov = getLineCov()
        def branchCov = getBranchCov() * 100
        def sqlString = "INSERT INTO func_test_summary (name, build_id, version, total, passed, unknown, skipped, failed, broken, line_cov, branch_cov, create_time) VALUES ('${JOB_NAME}', '${BUILD_ID}', '${version}', " +
                "${total}, ${passed}, ${unknown}, ${skipped}, ${failed}, ${broken}, ${lineCov}, ${branchCov}, NOW())"

        sql sql: sqlString
    }
}


