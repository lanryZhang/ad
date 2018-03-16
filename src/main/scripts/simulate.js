function dispatchClick(dom, position, timing) {
    // console.log('dom')
    // console.log(dom)
    // console.log(document.getElementById('demo-slider-0') && document.getElementById('demo-slider-0').innerHTML)
    // console.log(document.querySelector('body > ul > li:nth-child(1) > div > a > img'))
    // console.log(document.querySelector('DIV#demo-slider-0'))
    // console.log(document.querySelector(dom))
    var ele = prettieNode(document.querySelector(dom));
    var _position = prettiePosition(position);
    var clickEvent = createClickEvent(_position);

    if(timing) {
        setTimeout(function() {
            ele.dispatchEvent(clickEvent);
        }, timing * 1000)
    } else {
        ele.dispatchEvent(clickEvent);
    }
}

function prettieNode(node) {
    console.log('node')
    console.log(node)
    switch (node.tagName.toLocaleLowerCase()) {
        case 'a':
            node.target = '_self';
            break;
        // case value2:
        //     break;
        default:
            break;
    }

    return node;
}

function prettiePosition(position) {
    var screenX = position[0];
    var screenY = position[1];
    var clientX = position[2];
    var clientY = position[3];

    var _r = randomNum(-20, 20);

    return [screenX + _r, screenY + _r, clientX + _r, clientY + _r,]

}

function createClickEvent(position) {
    console.log(position[0], position[1], position[2], position[3])
    return createEvent('click')(position[0], position[1], position[2], position[3]);
    //click事件绑定事件处理程序
}

function createEvent(type) {
    return function(screenX, screenY, clientX, clientY) {
        var event = document.createEvent("MouseEvents");
        //初始化event
        event.initMouseEvent(type, true, true, document.defaultView, 0, screenX, screenY, clientX, clientY, false, false, false, false, 0, null);

        return event;
    }
}

function randomNum(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + 0);
}

function initPage() {
    var divMaskEle = document.createElement('div');
    divMaskEle.id = "divMaskEle_1";
    divMaskEle.style.width = '100%';
    divMaskEle.style.height = '10000px';
    divMaskEle.style.position = 'absolute';
    divMaskEle.style.zIndex = 1000000000000;
    divMaskEle.style.top = 0;
    divMaskEle.style.left = 0;
    document.body.style.position = 'relative';
    document.body.appendChild(divMaskEle);
    window.scrollTo(0, 10000);
    setTimeout(function(){
        document.getElementById('divMaskEle_1').parentNode.removeChild(document.getElementById('divMaskEle_1'));
        if(flag) {
            initEvent();
        } else {
            flag = true;
        }
    }, 1000)
}
var _eventsCache = [];
var flag = false;

function cacheEvent(events) {
    console.log('_eventsCache')
    console.log(events)
    console.log(_eventsCache)
    events.forEach(function(_event){
        _eventsCache.push(_event);
    })

    if(flag) {
        initEvent();
    } else {
        flag = true;
    }
}

function initEvent() {
    _eventsCache.forEach(function(_event){
        dispatchClick(_event[0], _event[1],3);
    })
}

initPage();

// [['div', [1,2,3,4]], ['div', [1,2,3,4]]]