package species

import java.util.logging.Logger;

import species.Resource;


class ClearUserResourcesJob {

    def resourcesService

    static triggers = {
        cron name:'cronTriggerForClearRes', startDelay:600l, cronExpression: '0 0 2 ? * *'
    }

    def execute() {
        println "========JOB SCHEDULER OF RESOURCE CLEARING======"
        resourcesService.deleteUsersResources();
    } 

}
