var page = require('webpage').create();
var system = require('system');

var args = system.args;
if (args.length < 2) {
    phantom.exit();
} else {
    var temp = args[1].replace(new RegExp("#amp#", "gm"), "&");
//console.log("temp===="+temp);
    var t = JSON.parse(temp);
    var en = JSON.parse(t);
    var cookieStr = args[2];
    var pvOrUvStr = " PV ";

    if (cookieStr !== "\"null\"") {
        pvOrUvStr = " UV ";
        cookieStr = cookieStr.replace(new RegExp("@", "gm"), " ");
        //  console.log("cookieStr="+cookieStr);

        var c = JSON.parse(cookieStr);
        var cookies = JSON.parse(c);

        for (var i in cookies) {
            //console.log("name=="+cookies[i].name);
            //    console.log("value=="+cookies[i].value);
            ///  console.log("domain=="+cookies[i].domain);
            // console.log("path=="+cookies[i].path);
            //console.log("expires=="+cookies[i].expires);

            phantom.addCookie({
                'name': cookies[i].name,
                'value': cookies[i].value,
                'domain': cookies[i].domain,
                'path': cookies[i].path,
                'expires': cookies[i].expires,
                'httponly': false,
                'secure': false
            });
        }
    }

    var ua = args[3];
    ua = ua.replace(new RegExp("@", "gm"), " ").replace(new RegExp("\x22", "gm"), "");
    page.settings.userAgent = ua;
    page.settings.referrer = en.referer;


    var navigateTimes = 0;
    var adError = false;

    page.onUrlChanged = function(targetUrl) {
        // if(targetUrl.toString() === "http://wap.ifeng.com/" ||
        //     targetUrl.toString() === "http://i.ifeng.com/"||
        //     targetUrl.toString() === "http://api.3g.ifeng.com/?"||
        //     targetUrl.toString() === "http://api.3g.ifeng.com/"){
        //     adError = true;
        // }
        // if (targetUrl.toString() !== "about:blank" && targetUrl.toString() !== en.url){
        //     console.log('navigate:'+ ++navigateTimes+" taskId:" + en.taskId + pvOrUvStr + en.url +" targetUrl: "+ targetUrl+" uuid:"+en.uuid);
        // }
    };

    if (en.platform === "PC"){
        page.viewportsize={width:1400,height:900};
    }else{
        page.viewportsize={width:375,height:667};
    }

    page.settings.resourceTimeout = 5 * 1000;
    // page.settings.resourceTimeout = en.waitTimeout * 1000;
    execute(en);

    page.onConsoleMessage = function(msg, lineNum, sourceId) {
        console.log(msg);
    };

    page.onResourceTimeout = function(e) {
        if (e.url.toString() !== "about:blank" && e.url.toString() !== en.url){
            forceSuccess = true;
            // console.log("for monitor: url:"+e.url +" taskId:"+en.taskId+" uuid:"+en.uuid)
        }        // the url whose request timed out
    };

    page.onResourceError = function(e) {
        if (en.disableImg === 1) {
            console.log("for monitor:resource error url:" + e.url + " taskId:" + en.taskId + " uuid:" + en.uuid);
        }
    };

    page.onResourceReceived = function(response) {
        if (en.disableImg === 1) {
            console.log('for monitor: Response (#' + response.id + ', stage "' + response.stage + '"): ' + JSON.stringify(response) + " taskId:" + en.taskId + " uuid:" + en.uuid);
        }
    };

    page.onResourceRequested = function(requestData, networkRequest) {
        // console.log('for monitor: Request (#' + requestData.id + '): ' + JSON.stringify(requestData) +" taskId:"+en.taskId+" uuid:"+en.uuid);
        if (en.disableImg === 1 && (requestData.url.match(/.*.jpg$/g) || requestData.url.match(/.*.png/g))){
            console.log("for monitor: request url canceled "+requestData.url+" taskId:"+en.taskId+" uuid:"+en.uuid);
            networkRequest.cancel();
        }
    };

    function execute(task) {
        page.settings.referrer = task.referer;

        var timestamp=new Date().getTime();
        page.open(task.url, function (status) {
            var finishTime=new Date().getTime();
            console.log("for monitor: request time, taskId:" + task.taskId + " proxy:" + task.proxyStr + " time:" + (finishTime - timestamp) + " status:"+status);
            console.log("for monitor: open url "+task.url+" taskId:"+task.taskId+" uuid:"+task.uuid +" status:"+status);
            if (status === "success") {
                var script = "";
                if (task.taskType === "CLICK") {

                    page.evaluate(function (data) {
                        adRender(data, "click");
                    }, task.data);

                    try{
                        window.setTimeout(function () {
                            var s1 = Math.random();
                            var r1 = Math.round(s1 * 100);

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
                                    page.evaluateJavaScript(script);
                                }
                            }
                        }, 15 * 1000);
                    }
                    catch(e) {

                    }

                }else {
                    page.evaluate(function (data) {
                        adRender(data)
                    },task.data);
                }
                // console.log("script:"+script);
                console.log("open success");
            } else {
                // console.log("open error:" + task.taskId + " " + pvOrUvStr + task.url);
                console.log("open error");
            }

            window.setTimeout(function () {
                phantom.exit();
            }, task.waitTimeout * 1000);
        });
    }
}