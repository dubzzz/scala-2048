'use strict';

const {Builder, promise} = require('selenium-webdriver');
const test = require('selenium-webdriver/testing');
const fc = require('fast-check');

const Model = require('./Model.js');
const CheckTiles = require('./commands/CheckTiles.js');
const JumpBackToPast = require('./commands/JumpBackToPast.js');
const PlayMove = require('./commands/PlayMove.js');
const RedoMove = require('./commands/RedoMove.js');
const StartNewGame = require('./commands/StartNewGame.js');
const UndoMove = require('./commands/UndoMove.js');

const {waitLoad} = require('./helpers');

promise.USE_PROMISE_MANAGER = false;

const browserName = process.env.BROWSER;
const arraySize = +process.env.ARRAY_SIZE || 100;

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

    test.it('random actions', async () => {
        var cmdArbs = [
            fc.constant(new CheckTiles()),
            fc.constantFrom('L', 'R', 'U', 'D').map(d => new PlayMove(d)),
            fc.constant(new RedoMove()),
            fc.constant(new UndoMove()),
            fc.constant(new StartNewGame()),
            fc.nat().map(d => new JumpBackToPast(d))
        ];
        var warmup = async function(seed) {
            await driver.get(rootUrl + "#seed=" + seed);
            await waitLoad(driver);
            return {state: driver, model: new Model()};
        };
        var teardown = async function() {
            await driver.get("about:blank");
        };

        await fc.assert(
            fc.asyncProperty(
                fc.nat(),
                fc.commands(cmdArbs, arraySize),
                async (seed, cmds) => {
                    const {state, model} = await warmup(seed);
                    try {
                        await fc.asyncModelRun(() => ({model, real: state}), cmds);
                    }
                    finally {
                        await teardown();
                    }
                }
            )
        );
    });
});
