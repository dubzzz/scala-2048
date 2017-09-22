'use strict';

const jsc = require('jsverify');
const {readGrid} = require('../helpers');

function JumpBackToPast() {
    var self = this;

    self.check = async function(driver, model) {
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
        var item = flatStates[jsc.random(0, flatStates.length -1)];//TODO: use a reproducible random number generator
        await driver.get(item.url);
        return model.jumpTo(item.id, item.key).store(await driver.getCurrentUrl(), await readGrid(driver));
    };
    self.name = "JumpBackToPast";
    self.toString = function() { return self.name; };
}

module.exports = JumpBackToPast;
