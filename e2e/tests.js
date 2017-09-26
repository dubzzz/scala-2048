'use strict';

const assert = require('assert');
const {Builder, By, Key, until, promise} = require('selenium-webdriver');
const test = require('selenium-webdriver/testing');
const jsc = require('jsverify');
const jscCommands = require('./jscCommands/jscCommands.js');

const Model = require('./Model.js');
const CheckTiles = require('./commands/CheckTiles.js');
const JumpBackToPast = require('./commands/JumpBackToPast.js');
const PlayMove = require('./commands/PlayMove.js');
const RedoMove = require('./commands/RedoMove.js');
const StartNewGame = require('./commands/StartNewGame.js');
const UndoMove = require('./commands/UndoMove.js');

promise.USE_PROMISE_MANAGER = false;

const browserName = process.env.BROWSER;
const arraySize = +process.env.ARRAY_SIZE;

test.describe('Scala 2048', function() {
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
        var commands = jscCommands.numCommands(
            arraySize,
            jscCommands.command(CheckTiles),
            jscCommands.command(PlayMove, jsc.oneof(jsc.constant('L'), jsc.constant('R'), jsc.constant('U'), jsc.constant('D'))),
            jscCommands.command(RedoMove),
            jscCommands.command(UndoMove),
            jscCommands.command(StartNewGame),
            jscCommands.command(JumpBackToPast, jsc.nat)
        );
        var warmup = async function(seed) {
            await driver.get(rootUrl + "#seed=" + seed);
            return new Model();
        };
        var runall = async function(actions, model) {
            for (var idx = 0 ; idx != actions.length ; ++idx) {
                var ac = actions[idx];
                if (await ac.check(driver, model)) {
                    if (! await ac.run(driver, model)) {
                        console.error("Test failed @ step #" + idx + " on task " + ac);
                        return false;
                    }
                }
            }
            return true;
        };
        var teardown = async function() {
            await driver.get("about:blank");
        };

        

        var testNumber = 0;
        jsc.assert(jsc.forall(jsc.integer, commands, async function(seed, commands) {
            var actions = commands.map(c => c.command);
            console.log("#" + (++testNumber) + ": " + actions.join(', '));
            var model = await warmup(seed);
            var result = await runall(actions, model);
            await teardown();
            return result;
        }))
            .then(val => val ? done(val) : done())
            .catch(error => done(error));
    });
});
