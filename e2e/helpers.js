const {By, until} = require('selenium-webdriver');

function __injectedEndAll(transition, callback) {
    // Source: https://gist.github.com/miguelmota/3faa2a2954f5249f61d9
    // Usage : d3.selectAll('g').transition().call(endAll, allDone);
    var n = 0;
    transition
        .each(function() { ++n; })
        .each('end', function() { if (!--n) callback.apply(this, arguments); });
}

var helpers = {
    // block execution until the end of the animation
    waitD3: async function(driver) {
        await driver.executeAsyncScript("             \
            var done = arguments[arguments.length-1]; \
            " + __injectedEndAll + "                  \
            d3.selectAll('g').transition().call(__injectedEndAll, done);");
    },
    // read data coming from the underlying grid
    // this data is updated instantly after the action
    readGrid: async function(driver) {
        // driver.findElements throws StaleElementReferenceException
        return await driver.executeScript("return (" + function() {
            var ids = [0,1,2,3]
                .map(y => [0,1,2,3].map(x => "grid-tile-" + y + "-" + x))
                .reduce((a,b) => a.concat(b), []);
            return ids.map(id => document.getElementById(id).getAttribute('data-tile-value')).join(',');
        } + ")();");
    },
    // read data from an human point of view
    readGridFromD3: async function(driver) {
        // driver.findElements throws StaleElementReferenceException
        return await driver.executeScript("return (" + function() {
            var values = Object.values(document.getElementsByTagName("rect"))
                .filter(e => e.parentNode.tagName == "g" && e.parentNode.id.indexOf("grid-tile-") === -1);
            var ids = [0,1,2,3]
                .map(y => [0,1,2,3].map(x => "grid-tile-" + y + "-" + x))
                .reduce((a,b) => a.concat(b), []);
            return ids.map(id => {
                var elt = document.getElementById(id).getElementsByTagName("rect")[0];
                var pos = {
                    x: elt.getAttribute('x'),
                    y: elt.getAttribute('y')
                };
                var withValue = values.filter(e => e.getAttribute("x") == pos.x && e.getAttribute("y") == pos.y);
                if (withValue.length !== 1) {
                    return "0";
                }
                return withValue[0].parentNode.getElementsByTagName("text")[0].innerHTML;
            }).join(',');
        } + ")();");
    },
};

module.exports = helpers;
