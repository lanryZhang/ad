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
    page.settings.referrer = en.referer;

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
        }        // the url whose request timed out
    };

    var taskCount = 2;
    var taskCountFinal = 2;
    var successCount = 0;


    var times = 0;

    function execute(task) {
        page.settings.referrer = task.referer;

        taskCount = taskCount - 1;
        page.open(task.url, function (status) {
            if (status === "success") {
                var cs = phantom.cookies;
                successCount = successCount+1;

                if (successCount === taskCountFinal){
                    var cookiesRes = [];
                    for (var i in cs) {
                        // if (cs[i].domain.indexOf(".ifeng.com")>0){
                        cookiesRes.push(cs[i]);
                        // }
                    }
                    console.log("open success" );
                    console.log("cookie:" + JSON.stringify(cookiesRes));
                }

            } else {
                // page.render("/data/images/page_error_"+task.taskId+"_"+timestamp+".png");
                console.log("open error");
                phantom.exit();
            }

            if (taskCount === 0) {
                window.setTimeout(function () {
                    phantom.exit();
                },  task.waitTimeout * 1000);
            }
        });
    }

    execute(en);

    window.setTimeout(function () {
        execute(en);
    }, 2 * 1000);
}