import groovy.grape.Grape

@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7')
@Grab(group = 'org.jsoup', module = 'jsoup', version = '1.10.3')
import org.jsoup.Jsoup
import groovyx.net.http.HTTPBuilder


import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovy.transform.Field

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import java.net.URLConnection
//import com.alibaba.fastjson.JSONObject;

//可以指定maven仓库
//@GrabResolver(name = 'aliyun', root = 'http://maven.aliyun.com/nexus/content/groups/public/')
//加载数据库连接驱动包
//@Grab('mysql:mysql-connector-java:5.1.25')
//@GrabConfig(systemClassLoader=true)



@NonCPS
def sendWechatAlarm(String webhookURL, String message) {
    String s="""
    {
    "msgtype": "text",
    "text": {
            "content" : "${message}"
            }
    }
   """
    println(webhookURL)
    HTTPBuilder http = new HTTPBuilder(webhookURL)
    def jsonSlurper = new groovy.json.JsonSlurper()
    def object1 = jsonSlurper.parseText(s)
    print object1

    http.request( POST, JSON ) { req ->


//        String picurl = "http://a1.qpic.cn/psc?/V50K8Aj22Pi7jG1cQHUv13mYFX1nzj4i/ruAMsa53pVQWN7FLK88i5tHP1AzYWSQYCpP9GLLjmX2kVPpmgDpWYZyT7qEjJ9gca9K5NwjMAhXGJm7RwGD61afF2eRZuzBuLTCavKor4Pw!/m&ek=1&kp=1&pt=0&bo=2wEJAQAAAAABF.I!&tl=3&vuin=1677684467&tm=1597251600&sce=60-3-3&rf=0-0"
//        Map<String, String> bodyParam = new HashMap<>()
//
//        bodyParam['picurl'] = picurl
//        bodyParam['title'] = "测试title"
//        bodyParam['description'] = "测试description"
//
//        Map<String, Map<String, String>> articles = new HashMap<>()
//        articles['articles'] = bodyParam
//        articles1 = [articles]
//        body = [
//                msgtype : 'text',
//                text : 'lengfeng'
//        ]
        body = object1


        response.success = { resp, json ->
            // TODO process json data
            println resp.status
        }
    }


//    http.post(path:'',body:object1,requestContentType:URLENC){resp->
//        assert resp.statusLine.statusCode == 200
//    }

}




def call(String webhookURL, String message) {
    sendWechatAlarm(webhookURL, message)

}
