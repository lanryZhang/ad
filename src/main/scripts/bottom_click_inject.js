var el = document.querySelector("body>DIV[class='main']>DIV[class='right']>DIV[class='pic300 iphone_none ipad_none']>DIV>DIV>A>img");
// console.log(el);
el.parentElement.removeAttribute("target");
el.click();