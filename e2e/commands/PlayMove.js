'use strict';

const {By, Key} = require('selenium-webdriver');
const {waitD3} = require('../helpers');

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
    var prettyDirection = function() {
        switch (direction) {
            case "L": return "left";
            case "R": return "right";
            case "U": return "up";
            case "D": return "down";
        }
        return "unknown";
    };

    self.check = async function(driver, model) {
        return true;
    };
    self.run = async function(driver, model) {
        await driver.findElement(By.id("playground")).sendKeys(key());
        await waitD3(driver);
        return true;
    };
    self.toString = function() { return "PlayMove(" + prettyDirection() + ")"; };
}

module.exports = PlayMove;
