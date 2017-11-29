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
    if (en.referer !== "" && en.referer !== null) {
        page.settings.referrer = en.referer;
    }
    page.settings.resourceTimeout = 5 * 1000;
    page.settings.loadImages = false;
    // page.settings.resourceTimeout = en.waitTimeout * 1000;
    execute(en);
    var navigateTimes = 0;
    page.onUrlChanged = function(targetUrl) {
        if (targetUrl.toString() !== "about:blank" &&
            targetUrl.toString() !== en.url){
            console.log('navigate:'+ ++navigateTimes + " taskId:" + en.taskId + pvOrUvStr + en.url +" targetUrl: "+ targetUrl + " uuid:" + en.uuid);
        }
    };

    function execute(task) {
        var timestamp = new Date().getTime();
        if (task.referer !== "" && task.referer !== null) {
            page.settings.referrer = task.referer;
        }
        page.open(task.url, function (status) {
            if (status === "success") {
                // var listUrl = page.evaluate(function () {
                //
                //         var el = document.querySelector("body>DIV[class='i_topNews iBox']>DIV[class='i_topNews_slide i_section_main_tj']>DIV[class='i_topNews_slide_main']>UL>LI>A[class='i_con']");
                //         if (el !== null && el.href !== ""){
                //             return el.href;
                //         }
                //     return "";
                // });

                var r1 = Math.random();
                var max = Math.round(r1 * 20);
                var min = 12;
                var len = max > min ? max : min;

                var imgUrl = "http://inews.ifeng.com/53444315/news.shtml?ch=qd_lykj_dl2#imgnum=";
                for (var i = 0; i < len; i++) {
                    window.setTimeout(function () {
                        page.open(imgUrl+i.toString(),function (s1) {
                            // console.log("Status:"+s1);
                            // console.log("pageURL:"+ listUrl[i])
                        });
                    },i * 1 * 1000)
                }
                if (status === "success") {
                    // page.render("/data/images/page_" + task.taskId + "_" + timestamp + ".png")
                    console.log("open success");
                } else {
                    console.log("open error");
                }
            } else {
                console.log("open error");
            }
        });
    }

    window.setTimeout(function () {
        phantom.exit();
    }, en.waitTimeout  * 1000);

}