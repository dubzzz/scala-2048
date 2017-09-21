'use strict';
/** CheckTiles
 *  Check whether or not the animation ends in the expected state
 */

const {readGrid, readGridFromD3, waitD3} = require('../helpers');

function CheckTiles() {
    var self = this;

    self.check = async function(driver, model) {
        return true;
    };
    self.run = async function(driver, model) {
        await waitD3(driver);
        return await readGrid(driver) == await readGridFromD3(driver);
    };
    self.name = "CheckTiles";
    self.toString = function() { return self.name; };
}

module.exports = CheckTiles;
