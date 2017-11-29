var el = document.querySelector("body>DIV[class='pageWidth']>DIV[class='col01 clearfix']>DIV[class='colR']>DIV[class='adv003']>DIV>A>IMG");

// console.log(el);
el.parentElement.removeAttribute("target");
el.click();