'use strict';

const {By, Key} = require('selenium-webdriver');
const {readGrid} = require('../helpers');

function PlayMove(direction) {
    var self = this;

    var key = function() {
        switch (direction) {
            case "L": return Key.ARROW_LEFT;
            case "R": return Key.ARROW_RIGHT;
            case "U": return Key.ARROW_UP;
            case "D": return Key.ARROW_DOWN;
        }
        return null;
    };
    var keyCode = function() {
        switch (direction) {
            case "L": return 37;
            case "R": return 39;
            case "U": return 38;
            case "D": return 40;
        }
        return null;
    };
    var prettyDirection = function() {
        switch (direction) {
            case "L": return "left";
            case "R": return "right";
            case "U": return "up";
            case "D": return "down";
        }
        return "unknown";
    };

    self.check = model => true;
    
    self.run = async function(model, driver) {
        var initialUrl = await driver.getCurrentUrl();
        // Sending keys directly from Selenium to the 'playground'
        // does not seem to work anymore
        // The work-around has been to send the keys directly from the browser
        // :: await driver.findElement(By.id("playground")).sendKeys(key());
        await driver.executeScript(" \
            const ke = new KeyboardEvent('keydown', {bubbles: true, cancelable: true, keyCode: " + keyCode() + "}); \
            document.getElementById('playground').dispatchEvent(ke);                                 \
        ");
        var updatedUrl = await driver.getCurrentUrl();
        if (initialUrl != updatedUrl) {// url has change iff the move was possible
            model.play(direction);
        }
        return model.store(updatedUrl, await readGrid(driver));
    };
    self.name = "PlayMove(" + prettyDirection() + ")";
    self.toString = function() { return self.name; };
}

module.exports = PlayMove;
