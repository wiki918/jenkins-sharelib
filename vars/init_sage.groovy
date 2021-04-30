
/**
 * Created by liyuqing on 20/8/16.
 */


def call(targetHost,sageVersion="4.x", hadoopUser="work", hadoopType="cdh"){
    def run_status = sh (
            script: """ssh root@${targetHost} "mkdir -p tmp && cd tmp && rm -f init_pipeline_environment.sh && wget http://pkg-plus.4paradigm.com/qa-tools/qa-scripts/init_pipeline_environment.sh && sh -x init_pipeline_environment.sh ${targetHost} ${sageVersion} ${hadoopUser} ${hadoopType}"
                    """,
            returnStatus:true
    )
    print run_status
    // 失败退出
    if (run_status != 0){
        print '环境初始化执行失败，强制退出'
        sh 'exit 1'
    }
}
