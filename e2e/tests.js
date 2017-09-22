'use strict';

const assert = require('assert');
const {Builder, By, Key, until, promise} = require('selenium-webdriver');
const test = require('selenium-webdriver/testing');
const jsc = require('jsverify');

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
        var commands = [
            new CheckTiles(),
            new PlayMove('L'),
            new PlayMove('R'),
            new PlayMove('U'),
            new PlayMove('D'),
            new RedoMove(),
            new UndoMove(),
            new StartNewGame(),
            new JumpBackToPast()
        ];
        var jscCommands = jsc.oneof.apply(this, commands.map(c => jsc.constant(c)));
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

        var jscCommandsArray = function(gen, maxSize) {
            /**
             * jsc.array uses logsize function as a limiter of its size...
             * 
             * // Helper, essentially: log2(size + 1)
             * function logsize(size) {
             *   return Math.max(Math.round(Math.log(size + 1) / Math.log(2), 0));
             * }
             */
            return jsc.bless({
                generator: (size) => {
                    var arrsize = jsc.random(0, maxSize || 100);
                    var arr = new Array(arrsize);
                    for (var i = 0; i < arrsize; i++) {
                        arr[i] = gen.generator(size);
                    }
                    return arr;
                },
                shrink: jsc.array(gen).shrink,
                show: jsc.array(gen).show
            });
        };

        var testNumber = 0;
        jsc.assert(jsc.forall(jsc.integer, jscCommandsArray(jscCommands, arraySize), async function(seed, actions) {
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
