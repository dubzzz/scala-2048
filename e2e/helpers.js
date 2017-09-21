const {until} = require('selenium-webdriver');

function __injectedEndAll(transition, callback) {
    // Source: https://gist.github.com/miguelmota/3faa2a2954f5249f61d9
    // Usage : d3.selectAll('g').transition().call(endAll, allDone);
    var n = 0;
    transition
        .each(function() { ++n; })
        .each('end', function() { if (!--n) callback.apply(this, arguments); });
}

var helpers = {
    waitD3: async function(driver) {
        await driver.executeAsyncScript("             \
            var done = arguments[arguments.length-1]; \
            " + __injectedEndAll + "                  \
            d3.selectAll('g').transition().call(__injectedEndAll, done);");
    }
};

module.exports = helpers;
