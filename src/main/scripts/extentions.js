var path = "";
function findElementPath(el){
    if (el.tagName == null || el.tagName.toLocaleUpperCase() == "BODY")
        return;
    // if (el.tagName == null || el.tagName.toLowerCase() == "a"){
    //     return;
    // }
    findElementPath(el.parentElement);
    if (el.className != ""){
        path = path+">"+el.tagName+"[class='"+el.className+"']";
    }else{
        path = path+">"+el.tagName;
    }
}
document.onmouseup = function (e) {
    findElementPath(e.target);
    console.log("body"+path);
}

