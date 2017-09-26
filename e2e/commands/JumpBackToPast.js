'use strict';

const jsc = require('jsverify');
const {readGrid} = require('../helpers');

function JumpBackToPast(pastId) {
    var self = this;

    self.check = function(model) {
        return model.states
                .map(items => Object.keys(items).length)
                .reduce((a,b) => a+b, 0) > 0;
    };
    
    self.run = async function(driver, model) {
        await driver.get("about:blank");
        var id = 0;
        var flatStates = model.states
                .map(items => {
                    var cid = id++;
                    return Object.keys(items).map(key => new Object({id: cid, url: items[key].url, key: key}))
                })
                .reduce((a,b) => a.concat(b), []);
        var scaledPastId = pastId % flatStates.length;
        if (scaledPastId < 0) {
            scaledPastId += flatStates.length;
        }
        var item = flatStates[scaledPastId];
        await driver.get(item.url);
        return model.jumpTo(item.id, item.key).store(await driver.getCurrentUrl(), await readGrid(driver));
    };
    self.name = "JumpBackToPast(" + pastId + ")";
    self.toString = function() { return self.name; };
}

module.exports = JumpBackToPast;
