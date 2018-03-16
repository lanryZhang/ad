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
    var pvOrUvStr = " PV ";

    var ua = args[3];
    ua = ua.replace(new RegExp("@", "gm"), " ").replace(new RegExp("\x22", "gm"), "");
    page.settings.userAgent = ua;
    page.settings.referrer = en.referer;

    page.settings.resourceTimeout = 5 * 1000;
    page.settings.loadImages = false;
    // page.settings.resourceTimeout = en.waitTimeout * 1000;

    var navigateTimes = 0;
    page.onUrlChanged = function(targetUrl) {
        if (targetUrl.toString() !== "about:blank" &&
            targetUrl.toString() !== en.url){
            console.log('navigate:'+ ++navigateTimes + " taskId:" + en.taskId + pvOrUvStr + en.url +" targetUrl: "+ targetUrl + " uuid:" + en.uuid);
        }
    };

    var arrAs = [];
    page.onConsoleMessage = function(msg, lineNum, sourceId) {
        if (msg.toString().indexOf("as:") >= 0) {
            arrAs = JSON.parse(msg.toString().replace("as:",""));
        }
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
        page.open(task.url, function (status) {
            if (status === "success") {

                page.evaluate(function () {
                    var as =document.querySelectorAll("div[class='i_topNews_slide_main']>ul>li>a[class='i_con']");
                    var arr=[];
                    for (var i = 0 ; i < as.length;i++){
                        arr.push(as[i].href);
                    }
                    var bs = document.querySelectorAll("div[id='zxlb']>a[class='i_con']");
                     for (var i = 0 ; i < bs.length;i++){
                         arr.push(bs[i].href);
                     }
                    console.log("as:"+JSON.stringify(arr))
                });

                window.setInterval(function () {
                    var r2 = Math.random();
                    var index = Math.round(r2 * arrAs.length);
                    page.settings.referrer = task.referer;

                    page.open(arrAs[index]+"?ch="+task.finishedFlag, function (s1) {
                        // console.log("Status:"+s1);
                        // console.log("pageURL:"+ listUrl[i])
                    });
                }, 3000);

                if (status === "success") {
                    // page.render("/data/images/page_" + task.taskId + "_" + timestamp + ".png")
                    console.log("open success");
                } else {
                    console.log("open error");
                }
            } else {
                console.log("open error");
                phantom.exit();
            }
        });
    }

    execute(en);

    window.setTimeout(function () {
        phantom.exit();
    }, en.waitTimeout  * 1000);
}