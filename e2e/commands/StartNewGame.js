'use strict';

const {clickInMenu, readGrid} = require('../helpers');

function StartNewGame() {
    var self = this;

    self.check = async function(driver, model) {
        return true;
    };
    self.run = async function(driver, model) {
        await clickInMenu(driver, "new-game");
        return model.newGame().store(await driver.getCurrentUrl(), await readGrid(driver));
    };
    self.name = "StartNewGame";
    self.toString = function() { return self.name; };
}

module.exports = StartNewGame;
