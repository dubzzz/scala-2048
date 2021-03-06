'use strict';

const {clickInMenu, readGrid} = require('../helpers');

function UndoMove() {
    var self = this;

    self.check = model => true;

    self.run = async function(model, driver) {
        var initialUrl = await driver.getCurrentUrl();
        await clickInMenu(driver, "undo-move");
        return model.undo().store(await driver.getCurrentUrl(), await readGrid(driver));
    };
    self.name = "UndoMove";
    self.toString = function() { return self.name; };
}

module.exports = UndoMove;
