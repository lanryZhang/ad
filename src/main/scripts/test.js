var page = require('webpage').create();
var system = require('system');
page.settings.resourceTimeout = 10000;

page.viewportsize={width:375,height:667};

page.settings.userAgent = "Mozilla/5.0 (Linux; Android 4.4.4; vivo X5S L Build/KTU84P; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043305 Safari/537.36 MicroMessenger/6.5.8.1060 NetType/WIFI Language/zh_CN";
// page.settings.userAgent="Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36";
var navigateTimes = 0;
//

page.onUrlChanged = function(targetUrl) {
    // console.log('navigate:'+ ++navigateTimes+ " targetUrl: "+ targetUrl);
};
page.settings.referrer="http://fashion.ifeng.com"
page.onConsoleMessage = function(msg, lineNum, sourceId) {

};

var task = {"activeProportion":0,"api":"","beginTime":1518486644737,"behaviourData":"","deviceInfo":"DEFAULT","disableImg":0,"duration":0,"exclusiveProxy":0,"execDuration":0,"executeScript":"function%28%29%7Bstm_clicki%28%27send%27%2C%20%27pageview%27%2C%20%7B%27page%27%3A%20%27%2FP3%2F...%27%2C%20%27title%27%3A%20%27%E9%80%89%E9%A1%B9%E5%B1%8F%27%7D%29%3Bstm_clicki%28%27send%27%2C%20%27pageview%27%2C%20%7B%27page%27%3A%20%27%2FP4%2F...%27%2C%20%27title%27%3A%20%27%E8%BF%9B%E5%85%A5%E8%BD%A6%E5%8E%A2%27%7D%29%3B%7D","finishedFlag":"","finishedPv":0,"forceArrive":0,"forceWait":1,"fragmentId":0,"groupId":"59ed4a034d05bd1354d979f0","ipReusedTimes":1,"mainScriptPath":"/data/programs/hippo/scripts/process_simulate.js","parentId":0,"platform":"APP","provinces":[0],"proxyStr":"123.53.135.138:32358#HTTP#cnc##zhandaye#河南省许昌市###168000#1518486633469","pvToUvRatio":50,"referer":"","remainPv":0,"requestType":"WEBKIT","scriptPath":"C:\\JavaCode\\Java\\git_source\\hippo\\src\\main\\scripts\\simulate.js","shellProportion":100,"subFragments":[{"activeProportion":0,"api":"","beginTime":0,"behaviourData":"%5B%5B%22DIV%23page-inner%3EUL%3Anth-child%281%29%3ELI%3Anth-child%286%29%3EA%3Anth-child%281%29%3EDIV%3Anth-child%281%29%3EIMG%3Anth-child%281%29%22%2C%5B546%2C599%2C498%2C752%5D%5D%5D","deviceInfo":"DEFAULT","disableImg":0,"duration":0,"exclusiveProxy":0,"execDuration":0,"executeScript":"","finishedFlag":"","finishedPv":0,"forceArrive":0,"forceWait":0,"fragmentId":0,"groupId":"59ed4a034d05bd1354d979f0","ipReusedTimes":1,"mainScriptPath":"/data/programs/hippo/scripts/process_simulate.js","parentId":0,"platform":"APP","provinces":[0],"pvToUvRatio":50,"referer":"","remainPv":0,"requestType":"WEBKIT","scriptPath":"/data/programs/hippo/scripts/simulate.js","shellProportion":0,"subFragments":[{"activeProportion":0,"api":"","beginTime":0,"behaviourData":"%5B%5B%22DIV%23page-inner%3EUL%3Anth-child%281%29%3ELI%3Anth-child%281%29%3EP%3Anth-child%282%29%3ESPAN%3Anth-child%281%29%22%2C%5B337%2C439%2C141%2C479%5D%5D%5D","deviceInfo":"DEFAULT","disableImg":0,"duration":0,"exclusiveProxy":0,"execDuration":0,"executeScript":"","finishedFlag":"","finishedPv":0,"forceArrive":0,"forceWait":0,"fragmentId":0,"groupId":"59ed4a034d05bd1354d979f0","ipReusedTimes":1,"mainScriptPath":"/data/programs/hippo/scripts/process_simulate.js","parentId":0,"platform":"APP","provinces":[0],"pvToUvRatio":50,"referer":"","remainPv":0,"requestType":"WEBKIT","scriptPath":"/data/programs/hippo/scripts/simulate.js","shellProportion":0,"targetPv":8000,"taskId":7919,"taskName":"手凤新闻-首页-焦点?02","taskSource":"IFENGAD","taskType":"CLICK","timePairs":[{"beginTime":930,"endTime":2359}],"url":"http://biz.ifeng.com/auto/special/fordfrstrain/list.shtml","uuid":"a604dcbd-ff95-4305-b98f-55518f3ece00","waitTimeout":30}],"targetPv":8000,"taskId":7918,"taskName":"手凤新闻-首页-焦点?02","taskSource":"IFENGAD","taskType":"CLICK","timePairs":[{"beginTime":930,"endTime":2359}],"url":"http://biz.ifeng.com/auto/special/fordfrstrain/list.shtml","uuid":"067d8bd8-f686-4669-8d15-20179e5faa15","waitTimeout":15}],"targetPv":8000,"taskId":6502,"taskName":"手凤新闻-首页-焦点?02","taskPosition":"DEFAULT","taskSource":"IFENGAD","taskType":"CLICK","timePairs":[{"beginTime":930,"endTime":2359}],"url":"http://ifengad.3g.ifeng.com/ad/ad.php?adid=22303#amp#ps=5","uuid":"3cdc4583-b2a6-4f43-8550-a2b8837ba1da","waitTimeout":41};

page.open("http://ifengad.3g.ifeng.com/ad/ad.php?adid=22303&ps=5",
    function(status) {

    if (status === "success") {
        console.log("success")

        window.setTimeout(function () {
            var s1 = Math.random();
            var r1 = Math.round(s1 * 100);
            console.log("r1:"+r1);
            console.log("task.behaviourData:"+task.behaviourData);
            console.log("task.executeScript:"+task.executeScript);
            if ("" !== task.behaviourData && null !== task.behaviourData) {
                if (r1 < 50 || ("" === task.executeScript || null === task.executeScript)) {
                    task.behaviourData = decodeURIComponent(task.behaviourData);
                    var seed = Math.random();
                    var rand = Math.round(seed * 100);
                    if (rand < task.activeProportion) {
                        if (page.injectJs(task.scriptPath)) {
                            var dt = JSON.parse(task.behaviourData);

                            page.evaluate(function (data) {
                                cacheEvent(data)
                            }, dt);
                        }
                    }
                }else if ("" !== task.executeScript && null !== task.executeScript){
                    var seed = Math.random();
                    var rand = Math.round(seed * 100);
                    if (rand < task.shellProportion) {
                        var script = decodeURIComponent(task.executeScript);
                        page.evaluateJavaScript(script);
                    }
                }
            }else if ("" !== task.executeScript && null !== task.executeScript){
                var seed = Math.random();
                var rand = Math.round(seed * 100);
                if (rand < task.shellProportion) {
                    var script = decodeURIComponent(task.executeScript);
                    console.log("script:"+script)
                    page.evaluateJavaScript(script);
                }
            }
        }, 10 * 1000);

    }

    window.setTimeout(function () {
        phantom.exit();
    },60000)
});