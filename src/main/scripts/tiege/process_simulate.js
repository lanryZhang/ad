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

    if (en.taskType === "CLICK"){
        page.settings.resourceTimeout = 10 * 1000;
    }else{
        page.settings.resourceTimeout = 5 * 1000;
    }

    var ua = args[3];
    ua = ua.replace(new RegExp("@", "gm"), " ").replace(new RegExp("\x22", "gm"), "");
    page.settings.userAgent = ua;
    page.settings.referrer = en.referer;

    if (en.platform === "PC"){
        page.viewportsize={width:1400,height:900};
    }else{
        page.viewportsize={width:375,height:667};
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
        }        // the url whose request timed out
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
        var subFragments = task.subFragments;
        // console.log(JSON.stringify(task))
        page.settings.referrer = task.referer;

        taskCount = taskCount - 1;
        var timestamp=new Date().getTime();
        page.open(task.url, function (status) {
            var finishTime=new Date().getTime();
            console.log("for monitor: request time, taskId:" + task.taskId + " proxy:" + task.proxyStr + " time:" + (finishTime - timestamp));

            times = times + task.waitTimeout;

            var navigateError = false;
            if ((page.url.indexOf("http://ifengad.3g.ifeng.com") >= 0 || (task.taskType === "CLICK" && page.url.indexOf("http://dol.deliver.ifeng.com") >= 0))
                && page.url === task.url) {
                navigateError = true;
                console.log("for monitor: system error, response "+status+",url do not changed,taskId:"+task.taskId);
            }

            if ((loadFinished || status === "success" || forceSuccess) && !adError && !navigateError) {
                var cs = phantom.cookies;
                successCount = successCount + 1;
                var cookiesRes = [];
                for (var i in cs) {
                    // if (cs[i].domain.indexOf(".ifeng.com")>0){
                    cookiesRes.push(cs[i])
                    // }
                }

                console.log("cookie:" + JSON.stringify(cookiesRes));
                if (successCount === taskCountFinal) {
                    console.log("open success");
                    if (task.taskType === "CLICK") {

                        if ("" !== task.behaviourData && null !== task.behaviourData) {
                            task.behaviourData = decodeURIComponent(task.behaviourData);
                            var seed = Math.random();
                            var rand = Math.round(seed * 100);
                            if (rand < 100) {
                                if (page.injectJs(task.scriptPath)) {
                                    var dt = JSON.parse(task.behaviourData);
                                    window.setTimeout(function () {
                                        page.evaluate(function (data) {
                                            cacheEvent(data)
                                        }, dt);
                                    }, 3 * 1000);
                                }

                                window.setTimeout(function () {
                                    page.settings.referrer = "http://www.tiegetech.com/Tiegefh/Tiege_%E8%B4%B4%E8%86%9C%E5%B1%8F%20%20%E5%87%A4%E5%87%B0%E7%BD%91.html";
                                    page.open("http://www.tiegetech.com/",function () {

                                        var dt1 = [[["BODY:nth-child(2) > UL:nth-child(8) > LI:nth-child(1) > DIV:nth-child(1) > A:nth-child(1) > IMG:nth-child(1)", [390, 534, 105, 401]]], [["BODY:nth-child(2) > UL:nth-child(8) > LI:nth-child(2) > DIV:nth-child(1) > A:nth-child(1) > IMG:nth-child(1)", [574, 538, 289, 405]]], [["BODY:nth-child(2) > DIV:nth-child(5) > UL:nth-child(1) > A:nth-child(2) > LI:nth-child(1) > SPAN:nth-child(2)", [497, 428, 212, 303]]], [["BODY:nth-child(2) > UL:nth-child(8) > LI:nth-child(3) > DIV:nth-child(1) > A:nth-child(1) > IMG:nth-child(1)", [425, 626, 140, 493]]], [["BODY:nth-child(2) > UL:nth-child(8) > LI:nth-child(4) > DIV:nth-child(1) > A:nth-child(1) > IMG:nth-child(1)", [544, 626, 259, 493]]], [["DIV#demo-slider-0 > DIV:nth-child(1) > UL:nth-child(1) > LI:nth-child(4) > IMG:nth-child(1)", [489, 222, 204, 89]]], [["DIV#demo-slider-0 > DIV:nth-child(1) > UL:nth-child(1) > LI:nth-child(3) > IMG:nth-child(1)", [462, 273, 177, 140]]], [["DIV#demo-slider-0 > DIV:nth-child(1) > UL:nth-child(1) > LI:nth-child(2) > IMG:nth-child(1)", [480, 278, 195, 145]]]];

                                        seed = Math.random();
                                        rand = Math.round(seed * dt1.length);

                                        dt = dt1[rand];
                                        if (page.injectJs(task.scriptPath)) {
                                            page.evaluate(function (data) {
                                                cacheEvent(data)
                                            }, dt);
                                        }
                                    })

                                }, 6 * 1000);
                            }
                        }
                    }
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