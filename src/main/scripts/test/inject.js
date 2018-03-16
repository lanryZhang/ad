function adRender(adJson, type) {
    adJson="%7b%09%22hrefURL%22%3a+%22http%3a%2f%2f111.202.89.168%3a8888%2fname%3fp%3dhrefURL%22%2c%09%22imgURL%22%3a+%5b%22http%3a%2f%2f111.202.89.168%3a8888%2fname%3fp%3dimgURL%22%5d%2c%09%22impURL%22%3a+%5b%22http%3a%2f%2f111.202.89.168%3a8888%2fname%3fp%3dimpURL%22%5d%2c%09%22text%22%3a+%222018%e6%ac%be%e5%88%ab%e5%85%8b%e6%96%b0%e6%98%82%e7%a7%91%e5%a8%81%e5%85%a8%e6%96%b0%e4%b8%8a%e5%b8%82%22%7d";
    if (type == 'click') {
        setTimeout(function () {
            track_click();
        }, 2000);
    }

    if (adJson) {
        try {
            var ad = JSON.parse(decodeURIComponent(adJson));
            var hrefURL = ad.hrefURL || '';
            // var clkURL = ad.clkURL || [];
            // clkURL.push(url)
            // window.clkURL = clkURL;
            window.hrefURL = hrefURL;
            var imgURL = ad.imgURL[0] || '';
            var impURL = ad.impURL || [];
            var str = '<a id="fff" href="' + hrefURL + '" target="_self" >';
            str += '<img src="' + imgURL + '">';
            for (var i = 0; i < impURL.length; i++) {
                str += '<img src="' + impURL[i] + '" width=0 height=0>';
            }
            str += '<img src="' + imgURL + '">';
            str += '</a>'

            document.write(str);
        } catch (e) {
            console.log(e.message);
        }
    }
}
