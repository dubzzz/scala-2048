'use strict';

const {clickInMenu, readGrid} = require('../helpers');

function RedoMove() {
    var self = this;

    self.check = model => true;

    self.run = async function(driver, model) {
        var initialUrl = await driver.getCurrentUrl();
        await clickInMenu(driver, "redo-move");
        return model.redo().store(await driver.getCurrentUrl(), await readGrid(driver));
    };
    self.name = "RedoMove";
    self.toString = function() { return self.name; };
}

module.exports = RedoMove;
