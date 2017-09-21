'use strict';

const assert = require('assert');
const {Builder, By, Key, until, promise} = require('selenium-webdriver');
const test = require('selenium-webdriver/testing');
const jsc = require('jsverify');

const Model = require('./Model.js');
const CheckTiles = require('./commands/CheckTiles.js');
const PlayMove = require('./commands/PlayMove.js');

promise.USE_PROMISE_MANAGER = false;

const browserName = process.env.BROWSER;

test.describe('Google Search', function() {
    let driver;
    let rootUrl = "https://dubzzz.github.io/scala-2048/";

    test.beforeEach(async function() {
        driver = await new Builder()
                .forBrowser(browserName)
                .build();
    });

    test.afterEach(async function() {
        await driver.quit();
    });

    test.it('random actions', done => {
        var commands = [
            new CheckTiles(),
            new PlayMove('L'),
            new PlayMove('R'),
            new PlayMove('U'),
            new PlayMove('D')
        ];
        var jscCommands = jsc.oneof.apply(this, commands.map(c => jsc.constant(c)));
        var warmup = async function() {
            await driver.get(rootUrl);
            return new Model();
        };
        var runall = async function(actions, model) {
            for (var idx = 0 ; idx != actions.length ; ++idx) {
                var ac = actions[idx];
                if (await ac.check(driver, model)) {
                    if (! await ac.run(driver, model)) {
                        return false;
                    }
                }
            }
            return true;
        };
        var teardown = async function() {
        };

        var testNumber = 0;
        jsc.assert(jsc.forall(jsc.array(jscCommands), async function(actions) {
            console.log("#" + (++testNumber) + ": " + actions.join(', '));
            var model = await warmup();
            var result = await runall(actions, model);
            await teardown();
            return result;
        }))
            .then(val => val ? done(val) : done())
            .catch(error => done(error));
    });
});
