var page = require('webpage').create();
var system = require('system');

// page.viewportsize={width:8000,height:10000};

page.onConsoleMessage = function(msg, lineNum, sourceId) {
    console.log(msg );
};

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
    if (en.referer !== "" && en.referer !== null) {
        var refs = en.referer.split(",");
        var rk = Math.random();
        var ix = Math.round(rk * refs.length);
        var ref = refs[ix];
        page.settings.referrer = ref;
    }
    if (en.taskType === "CLICK"){
        page.settings.resourceTimeout = 10 * 1000;
    }else{
        page.settings.resourceTimeout = 5 * 1000;
    }
    // page.settings.loadImages = false;

    var loadFinished = false;

    page.onInitialized = function() {
        page.evaluate(function() {
            document.addEventListener('DOMContentLoaded', function() {
                if (document.location.toString() !== "about:blank") {
                    console.log("dom_finished");
                }
            }, false);
        });
    };


    page.onConsoleMessage = function(msg, lineNum, sourceId) {
        if (msg.toString() === "dom_finished"){
            loadFinished = true;
        }
        console.log(msg);
    };

    var adError = false;

    var forceSuccess = false;
    var navigateTimes = 0;
    page.onUrlChanged = function(targetUrl) {
        if(targetUrl.toString() === "http://wap.ifeng.com/" ||
            targetUrl.toString() === "http://wap.ifeng.com" ||
            targetUrl.toString() === "http://i.ifeng.com/"||
            targetUrl.toString() === "http://i.ifeng.com"||
            targetUrl.toString() === "http://api.3g.ifeng.com/?"||
            targetUrl.toString() === "http://api.3g.ifeng.com/"){
            adError = true;
        }
        if (targetUrl.toString() !== "about:blank" && targetUrl.toString() !== en.url){
            console.log('navigate:'+ ++navigateTimes+" taskId:" + en.taskId + pvOrUvStr + en.url +" targetUrl: "+ targetUrl);
            forceSuccess = true;
        }
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

    // page.settings.resourceTimeout = en.waitTimeout * 1000;
    var taskCount = 0;
    var taskCountFinal = 0;
    var successCount = 0;

    function caculateTaskCount(task) {
        taskCount = taskCount + 1;
        taskCountFinal = taskCountFinal + 1;

        var subFragments = task.subFragments;
        if (subFragments !== null) {
            for (var i in subFragments) {
                caculateTaskCount(subFragments[i]);
            }
        }
    }

    var times = 0;

    function execute(task) {
        //var timestamp=new Date().getTime();
        var subFragments = task.subFragments;
        // console.log(JSON.stringify(task))
        if (task.referer !== "" && task.referer !== null) {
            page.settings.referrer = task.referer;
        }
        taskCount = taskCount - 1;
        // console.log("for monitor: open url "+task.url+" taskId:"+task.taskId+" uuid:"+task.uuid);
        var timestamp=new Date().getTime();
        page.open(task.url, function (status) {
            var finishTime=new Date().getTime();
            console.log("for monitor: request time, taskId:" + task.taskId + " proxy:" + task.proxyStr + " time:" + (finishTime - timestamp) + " status:"+status);
            console.log("for monitor: open url "+task.url+" taskId:"+task.taskId+" uuid:"+task.uuid +" status:"+status);

            times = times + task.waitTimeout;

            var navigateError = false;
            if (page.url.indexOf("http://ifengad.3g.ifeng.com") >= 0 && page.url === task.url) {
                navigateError = true;

                console.log("for monitor: system error, response "+status+",url do not changed,taskId:"+task.taskId);
                // var timestamp = new Date().getTime();
                // window.setTimeout(function () {
                //     page.render("/data/images/page_error_"+task.taskId+"_"+timestamp+".png");
                //
                // }, 3 * 1000);
            }

            // console.log("for monitor: task navigateError:"+navigateError.toString()+" loadFinished:"+loadFinished+" forceSuccess:"+
            //     forceSuccess+" adError:"+adError+ " status:"+status+" taskId:"+task.taskId +" task.url:"+page.url+ " taskUUID:"+task.uuid);


            if ((loadFinished || status === "success" || forceSuccess)
                && !adError && !navigateError) {
                var cs = phantom.cookies;
                successCount = successCount+1;

                var cookiesRes = [];
                for (var i in cs) {
                    // if (cs[i].domain.indexOf(".ifeng.com")>0){
                    cookiesRes.push(cs[i]);
                    // }
                }
                console.log("cookie:" + JSON.stringify(cookiesRes));

                if (successCount === taskCountFinal){
                    console.log("open success" );
                }

            } else {
                // page.render("/data/images/page_error_"+task.taskId+"_"+timestamp+".png");
                console.log("open error");
                phantom.exit();
            }
            for (var i in subFragments) {
                window.setTimeout(function () {
                    execute(subFragments[i]);
                }, times * 1000);
            }

            if (taskCount === 0) {
                window.setTimeout(function () {
                    phantom.exit();
                }, times * 1000);
            }
        });
    }

    caculateTaskCount(en);

    execute(en);
}
