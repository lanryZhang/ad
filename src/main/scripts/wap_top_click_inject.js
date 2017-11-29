var Rand = Math.random();
var index = Math.round(Rand * 10);

var el = document.querySelector("body>DIV[class='i_nav clearfix']>a:nth-child("+index+")");

el.click();