var page = require('webpage').create();
var system = require('system');

// page.viewportsize={width:8000,height:10000};


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
    if (en.referer !== "" && en.referer !== null) {
        page.settings.referrer = en.referer;
    }

    var navigateTimes = 0;
    var adError = false;

    page.onUrlChanged = function(targetUrl) {
        if(targetUrl.toString() === "http://wap.ifeng.com/" ||
            targetUrl.toString() === "http://i.ifeng.com/"||
            targetUrl.toString() === "http://api.3g.ifeng.com/?"||
            targetUrl.toString() === "http://api.3g.ifeng.com/"){
            adError = true;
        }
        if (targetUrl.toString() !== "about:blank" && targetUrl.toString() !== en.url){
            console.log('navigate:'+ ++navigateTimes+" taskId:" + en.taskId + pvOrUvStr + en.url +" targetUrl: "+ targetUrl+" uuid:"+en.uuid);
        }
    };

    page.settings.resourceTimeout = 10 * 1000;
    // page.settings.resourceTimeout = en.waitTimeout * 1000;
    execute(en);

    page.onResourceReceived = function(response) {
        if (en.finishedFlag === response.url){
            console.log("open success");
        }
    };
    function execute(task) {
        var timestamp=new Date().getTime();
        if (task.referer !== "" && task.referer !== null) {
            page.settings.referrer = task.referer;
        }
        page.open(task.url, function (status) {
            if (status === "success" && !adError) {
                var cs = phantom.cookies;

                var cookiesRes = [];
                for (var i in cs){
                    if (cs[i].domain.indexOf(".ifeng.com")>0){
                        cookiesRes.push(cs[i])
                    }
                }
                // console.log("open success:" + en.taskId  + pvOrUvStr + en.url +" content:"+page.getElementById("search_form").getAttribute("action"));
                // page.render("/data/images/page_"+task.taskId+"_"+timestamp+".png");
                console.log("cookie:" + JSON.stringify(cs));
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