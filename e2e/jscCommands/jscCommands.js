"use strict";
const {command} = require('./arbitrary/command.js');
const {commands, numCommands, filter} = require('./arbitrary/commands.js');
const {forallCommands, forallCommandsSeeded} = require('./runners.js');

module.exports = {
    command: command,
    commands: commands,
    numCommands: numCommands,
    filter: filter,
    forallCommands: forallCommands,
    forallCommandsSeeded: forallCommandsSeeded
};
