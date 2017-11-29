var page = require('webpage').create();
var system = require('system');

var timestamp=new Date().getTime();
page.customHeaders = {
    "DNT": "ifeng_"+timestamp
};

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
    if (en.referer !== "" && en.referer !== null) {
        page.settings.referrer = en.referer;
    }

    page.settings.resourceTimeout = 10 * 1000;
    // page.settings.resourceTimeout = en.waitTimeout * 1000;


    var re = /href="http:\/\/.*\.ifeng\.com\/a\/\d+\/\d+_0\.shtml"/;


    var navigateTimes = 0;
    page.onUrlChanged = function(targetUrl) {
        if (targetUrl.toString() !== "about:blank" && targetUrl.toString() !== en.url){
            console.log('navigate:'+ ++navigateTimes+" taskId:" + en.taskId + pvOrUvStr + en.url +" targetUrl: "+ targetUrl+" uuid:"+en.uuid);
        }
    };

    execute(en);

    function execute(task) {

        // console.log(JSON.stringify(task))
        if (task.referer !== "" && task.referer !== null) {
            page.settings.referrer = task.referer;
        }
        page.open(task.url, function (status) {

            var href = re.exec(page.content);
            href = href.toString().replace("href=\"", "").replace("\"", "");
            console.log("r-----" + href);
            if (status === "success") {
                page.open(href, function (s) {
                    if (s === "success") {
                        window.setTimeout(function () {
                            if (page.injectJs(task.scriptPath)) {
                                if (status === "success") {
                                    // page.render("/data/images/page_"+task.taskId+"_"+timestamp+".png");
                                    console.log("open success:" + task.taskId + " " + pvOrUvStr + task.url);
                                } else {
                                    console.log("open error:" + task.taskId + " " + pvOrUvStr + task.url);
                                }
                            }
                        },35000);

                    } else {
                        console.log("open error:" + task.taskId + " " + pvOrUvStr + task.url);
                    }
                });
            }else{
                console.log("open error:" + task.taskId + " " + pvOrUvStr + task.url);
            }
        });
    }

    window.setTimeout(function () {
        phantom.exit();
    }, en.waitTimeout * 1000);
}