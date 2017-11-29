var page = require('webpage').create();
var system = require('system');
page.settings.resourceTimeout = 40000;
page.viewportsize={width:375,height:667}
// page.settings.userAgent = "Mozilla/5.0 (Linux; Android 4.4.4; vivo X5S L Build/KTU84P; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043305 Safari/537.36 MicroMessenger/6.5.8.1060 NetType/WIFI Language/zh_CN";

//
// var loadFinished = false;
// page.onConsoleMessage = function (msg, lineNum, sourceId) {
//     if (msg.toString() === "dom_finished") {
//         loadFinished = true;
//     }
// };
//
// page.onInitialized = function() {
//     page.evaluate(function() {
//         document.addEventListener('DOMContentLoaded', function() {
//             if (document.location.toString() !== "about:blank") {
//                 console.log("dom_finished");
//             }
//         }, false);
//     });
// };

// var adError = false;
// var forceSuccess = false;
//
// var navigateTimes = 0;
// page.onUrlChanged = function(targetUrl) {
//     if(targetUrl.toString() === "http://wap.ifeng.com/" ||
//         targetUrl.toString() === "http://wap.ifeng.com" ||
//         targetUrl.toString() === "http://i.ifeng.com/"||
//         targetUrl.toString() === "http://i.ifeng.com"||
//         targetUrl.toString() === "http://api.3g.ifeng.com/?"||
//         targetUrl.toString() === "http://api.3g.ifeng.com/"){
//         adError = true;
//     }
//     if (targetUrl.toString() !== "about:blank" ){
//         console.log('navigate:'+ ++navigateTimes+" taskId:" + " targetUrl: "+ targetUrl);
//         forceSuccess = true;
//     }
// };

page.onResourceTimeout = function(e) {
    console.log(e.errorCode);   // it'll probably be 408
    console.log(e.errorString); // it'll probably be 'Network timeout on resource'
    console.log(e.url);         // the url whose request timed out
};

var finished = false;
page.onConsoleMessage = function (msg, lineNum, sourceId) {
    // if (msg.toString() === "dom_finished"){
    //     finished = false;
    //     console.log(msg+"onConsoleMessage finished:"+finished)
    // }
    console.log("inner:" + msg);
};

page.open("http://www.hztech.net.cn/index.html", function (status) {

    if (status === "success") {
        if (page.injectJs("C:\\JavaCode\\Java\\git_source\\hippo\\src\\main\\scripts\\simulate.js")) {
            var arr = [
                [["DIV#myCarousel > NAV:nth-child(1) > DIV:nth-child(1) > DIV:nth-child(2) > UL:nth-child(1) > LI:nth-child(1) > A:nth-child(1)", [518, 590, 212, 460]]]
                // [["DIV#myCarousel > NAV:nth-child(1) > DIV:nth-child(1) > DIV:nth-child(1) > BUTTON:nth-child(1) > SPAN:nth-child(4)", [662, 163, 366, 30]]],
                // [["BODY:nth-child(2) > DIV:nth-child(2) > DIV:nth-child(1) > DIV:nth-child(1) > DIV:nth-child(3) > DIV:nth-child(1) > I:nth-child(1)", [515, 591, 197, 458]]],
                // [["BODY:nth-child(2) > DIV:nth-child(2) > DIV:nth-child(1) > DIV:nth-child(1) > DIV:nth-child(2) > DIV:nth-child(1) > I:nth-child(1)", [503, 426, 193, 293]]]
            ];
            // var seed = Math.random();
            // var rand = Math.round(seed * 1);
            // var data = arr[rand];
            page.evaluate(function (data) {
                cacheEvent(data)
            }, arr[0]);
        }
        // page.evaluate(function () {
        //     document.querySelector("DIV#myCarousel > NAV:nth-child(1) > DIV:nth-child(1) > DIV:nth-child(2) > UL:nth-child(1) > LI:nth-child(1) > A:nth-child(1)").click();
        // })
        page.render("C://test.png")
    }

    console.log("status:"+status);
})

window.setTimeout(function () {
    phantom.exit();
}, 50000)
