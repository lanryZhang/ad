var page = require('webpage').create();
var system = require('system');

var args = system.args;
if (args.length <2){
    phantom.exit();
}else{
    console.log(args[1]);
    var es = args[1].replace(new RegExp("#amp#","gm")," ","&")
    console.log(es);
    var en = JSON.parse(es);
    var cookieStr = args[2];
    if (cookieStr !== "\"null\""){
        console.log("cookieStr="+cookieStr);
        var cookies = JSON.parse(cookieStr);

        for(var i in cookies) {
            console.log(cookies[i].name);
            console.log(cookies[i].value);
            console.log(cookies[i].domain);
            console.log(cookies[i].path);
            console.log(cookies[i].expires);

            phantom.addCookie({
                'name'     : cookies[i].name,
                'value'    : cookies[i].value,
                'domain'   : cookies[i].domain,
                'path'     : cookies[i].path,
                'expires'  : cookies[i].expires,
                'httponly' : false,
                'secure'   : false
            });
        }
    }
}

var ua = args[3];
ua = ua.replace(new RegExp("@","gm")," ").replace(new RegExp("\x22","gm"),"");
page.settings.userAgent = ua;


page.onConsoleMessage = function(msg) {
    console.log(msg);
}

page.open(en.url, function(status) {
    var cs = page.cookies;
    if(status === "success") {
        console.log("true");

        console.log("cookie:"+JSON.stringify(cs));

        // page.evaluateJavaScript("function(){setTimeout(function () {console.log(\"timeout\");},5000);}");
        // if (en.scriptPath !== "" && page.injectJs(en.scriptPath)){
        //   console.log("InjectJs sucess");
        // }else{
        //   console.log("InjectJs error");
        // }
    }else{
        console.log("open false");
    }
    phantom.exit();
});