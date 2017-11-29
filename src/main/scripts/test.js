var page = require('webpage').create();
var system = require('system');
page.settings.resourceTimeout = 10000;

// page.settings.userAgent = "Mozilla/5.0 (Linux; Android 4.4.4; vivo X5S L Build/KTU84P; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043305 Safari/537.36 MicroMessenger/6.5.8.1060 NetType/WIFI Language/zh_CN";
page.settings.userAgent="Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36";
// var navigateTimes = 0;
//
// page.onUrlChanged = function(targetUrl) {
//     if(targetUrl.toString() === "http://wap.ifeng.com/" ||
//         targetUrl.toString() === "http://wap.ifeng.com" ||
//         targetUrl.toString() === "http://i.ifeng.com/"||
//         targetUrl.toString() === "http://i.ifeng.com"||
//         targetUrl.toString() === "http://api.3g.ifeng.com/?"||
//         targetUrl.toString() === "http://api.3g.ifeng.com/"){
//         adError = true;
//     }
//     console.log('navigate:'+ ++navigateTimes+ " targetUrl: "+ targetUrl);
// };
// var forceSuccess = false;
page.onResourceReceived = function(response) {
    // console.log('Response (#' + response.id + ', stage "' + response.stage + '"): ' + JSON.stringify(response));
    //     console.log(response.url)
};

var forceSuccess = false;

page.onConsoleMessage = function(msg, lineNum, sourceId) {
    if (msg.indexOf("creativeData:") >= 0){
        msg = msg.replace("creativeData:","");
        var scriptInject = "a("+msg+")";

        page.switchToParentFrame();

        console.log("scriptInject:"+scriptInject)
        if (page.injectJs("C:\\JavaCode\\Java\\git_source\\hippo\\src\\main\\scripts\\boshi\\inject.js")) {

                console.log("inject 1 success")
                page.evaluate(function (dt) {
                    a(JSON.parse(dt));
                    opendsp_click();
                }, msg)
        }
    }
};

page.onResourceTimeout = function(e) {
    console.log(e.errorCode);   // it'll probably be 408
    console.log(e.errorString); // it'll probably be 'Network timeout on resource'
    console.log(e.url);         // the url whose request timed out

    // if (e.url.toString() !== "about:blank"
    //     && e.url.toString() !== en.url){
    //     forceSuccess = true;
    // }
};

page.open("http://www.ifeng.com/a_if/taobao/171128/ssnyhzh01.html", function(status) {
    console.log("status:"+status)
    window.setTimeout(function () {
        page.switchToFrame("tanxssp-outer-iframemm_12229823_1573806_148756418");
        // page.settings.userAgent="Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36";

        // console.log(page.getElementsByTagName("a"))


        page.evaluate(function () {
           console.log("creativeData:"+JSON.stringify(creativeData));
            // data.settings.userAgent="Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.130 Safari/537.36";
            // document.querySelector("body > a").click()
            // console.log("clickURL:"+document.getElementsByTagName('body')[0].getElementsByTagName('iframe')[0].contentDocument.getElementsByTagName('a')[0].href);
        });

    },5000)

    // console.log(page.childFramesName())
    // page.switchToFrame("tanxssp-outer-iframemm_12229823_1573806_148756418");
    //
    // console.log("A--href:"+page.getElementsByTagName("a").href)
    // page.getElementsByTagName("a").click()
});

window.setTimeout(function () {
    phantom.exit();
},10000)