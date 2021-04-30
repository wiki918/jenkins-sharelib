/**
 * Created by sungaofei on 20/2/8.
 */
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7')

import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.*


import static groovyx.net.http.Method.*
import groovy.transform.Field


//global variable
@Field jenkinsURL = "http://auto.4paradigm.com"
@Field failed = "FAILED"
@Field success = "SUCCESS"
@Field inProgress = "IN_PROGRESS"
@Field abort = "ABORTED"

@NonCPS
def String checkJobStatus() {

    def url = ""

    if (env.BRANCH_NAME!= "" && env.BRANCH_NAME != null){
        String jobName = "${JOB_NAME}".split("/")[0]
        url = "/view/API/job/${jobName}/job/${env.BRANCH_NAME}/${BUILD_NUMBER}/wfapi/describe"
    }else {
        url = "/view/API/job/${JOB_NAME}/${BUILD_NUMBER}/wfapi/describe"
    }
    HTTPBuilder http = new HTTPBuilder(jenkinsURL)
    String status = success

    println("1111111111")
    println("${JOB_NAME}")
    println(url)
    http.get(path: url) { resp, json ->
        if (resp.status != 200) {
            throw new RuntimeException("请求 ${url} 返回 ${resp.status} ")
        }
        List stages = json.stages

        for (int i = 0; i < stages.size(); i++) {
            def stageStatus = json.stages[i].status
            if (stageStatus == failed) {
                status = failed
                break
            }
            if (stageStatus == abort) {
                status = abort
                break
            }
        }
    }

    return status;

}


@NonCPS
def call(String to) {
    println("邮件列表：${to}")

    def reportURL = ""
    String jobName = "${JOB_NAME}"
    String blueOCeanURL = ""

    if (env.BRANCH_NAME!= "" && env.BRANCH_NAME != null){
        jobName = "${JOB_NAME}".split("/")[0]
        reportURL = "/view/API/job/${jobName}/job/${env.BRANCH_NAME}/${BUILD_NUMBER}/allure/"
//            http://auto.4paradigm.com/blue/organizations/jenkins/gitlabtest/detail/master/217/pipeline
        blueOCeanURL = "${jenkinsURL}/blue/organizations/jenkins/${jobName}/detail/${env.BRANCH_NAME}/${BUILD_NUMBER}/pipeline"
    }else{
        reportURL = "/view/API/job/${JOB_NAME}/${BUILD_NUMBER}/allure/"
        blueOCeanURL = "${jenkinsURL}/blue/organizations/jenkins/${JOB_NAME}/detail/${JOB_NAME}/${BUILD_NUMBER}/pipeline"
    }

    def sendSuccess = {
//        blueOCeanURL = "${jenkinsURL}/blue/organizations/jenkins/${JOB_NAME}/detail/${JOB_NAME}/${BUILD_NUMBER}/pipeline"

        def fileContents = ""
        def passed = ""
        def failed = ""
        def skipped = ""
        def broken = ""
        def unknown = ""
        def total = ""
        HTTPBuilder http = new HTTPBuilder('http://auto.4paradigm.com')
        //根据responsedata中的Content-Type header，调用json解析器处理responsedata
        http.get(path: "${reportURL}widgets/summary.json") { resp, json ->
            println resp.status
            passed = json.statistic.passed
            failed = json.statistic.failed
            skipped = json.statistic.skipped
            broken = json.statistic.broken
            unknown = json.statistic.unknown
            total = json.statistic.total

        }

        println(passed)

        emailext body: """
<html>
  <style type="text/css">
  <!--
  ${fileContents}
  -->
  </style>
  <body>
  <div id="content">
  <h1>Summary</h1>
  <div id="sum2">
      <h2>Jenkins Build</h2>
      <ul>
      <li>Job 地址 : <a href='${BUILD_URL}'>${BUILD_URL}</a></li>
       <li>测试报告地址 : <a href='${jenkinsURL}${reportURL}'>${jenkinsURL}${reportURL}</a></li>
       <li>Pipeline 流程地址 : <a href='${blueOCeanURL}'>${blueOCeanURL}</a></li>
      </ul>

      <h2>测试结果汇总</h2>
      <ul>
      <li>用例总数 : ${total}</li>
      <li>pass数量 : ${passed}</li>
       <li>failed数量 :${failed} </li>
       <li>skip数量 : ${skipped}</li>
       <li>broken数量 : ${broken}</li>
      </ul>
  </div>
  </div></body></html>
    """, mimeType: 'text/html', subject: "${JOB_NAME} 测试结束", to: to

    }

    def send = { String subject ->
        emailext body: """
<html>
  <style type="text/css">
  <!--
  -->
  </style>
  <body>
  <div id="sum2">
      <h2>Jenkins Build</h2>
      <ul>
      <li>Job 地址 : <a href='${BUILD_URL}'>${BUILD_URL}</a></li>
        <li>测试报告地址 : <a href='${jenkinsURL}${reportURL}'>${jenkinsURL}${reportURL}</a></li>
       <li>Pipeline 流程地址 : <a href='${blueOCeanURL}'>${blueOCeanURL}</a></li>
      </ul>
  </div>
  </div></body></html>
    """, mimeType: 'text/html', subject: subject, to: to
    }

    String status = checkJobStatus()
//    String status = $BUILD_STATUS
    println("当前job 的运行状态为： ${status}")
    switch (status) {
        case ["SUCCESS", "UNSTABLE"]:
            sendSuccess()
            break
        case "FAILED":
            send("Job运行失败")
            break
        case "ABORTED":
            send("Job在运行中被取消")
            break
        default:
            send("Job运行结束")
    }

}



