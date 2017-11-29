var page = require('webpage').create();
var system = require('system');

page.settings.resourceTimeout = 5000;

var args = system.args;
if (args.length < 2) {
    phantom.exit();
} else {
    var temp = args[1].replace(new RegExp("#amp#", "gm"), "&");
//console.log("temp===="+temp);
    //var t = JSON.parse(temp);
    var en = JSON.parse(temp);
    var cookieStr = args[2];
    var pvOrUvStr = " PV ";

    if (cookieStr !== "null") {

        pvOrUvStr = " UV ";
        cookieStr = cookieStr.replace(new RegExp("@", "gm"), " ");
        console.log("cookieStr="+cookieStr);

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
}

var ua = args[3];
ua = ua.replace(new RegExp("@", "gm"), " ").replace(new RegExp("\x22", "gm"), "");
page.settings.userAgent = ua;
var taskCount = 0;
caculateTaskCount(en);
console.log("taskCount=====" + taskCount);

page.onConsoleMessage = function (msg) {
    // console.log("console message:"+msg);
}

execute(en);

function caculateTaskCount(task) {
    taskCount = taskCount + 1;
    var subFragments = task.subFragments;
    if (subFragments !== null) {
        for (var i in subFragments) {
            caculateTaskCount(subFragments[i]);
        }
    }
}

function execute(task) {
    var subFragments = task.subFragments;
    console.log(JSON.stringify(task))
    page.open(task.url, function (status) {
        var cs = phantom.cookies;
        taskCount = taskCount - 1;
        if (status === "success") {
            console.log("true");
            console.log("open success:" + task.taskId + " " + pvOrUvStr+ task.url);
            console.log("cookie:" + JSON.stringify(cs));
            // if (taskCount === 0) {
            //   console.log("exit")
            //      phantom.exit();
            //  }
        } else {
            console.log("open false"+pvOrUvStr);
        }
        if (taskCount === 0) {
            phantom.exit();
        }
        for (var i in subFragments) {
            window.setTimeout(function () {
                execute(subFragments[i]);
            },1000);
        }
    });
}