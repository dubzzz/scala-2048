"use strict";
const {command} = require('./arbitrary/command.js')
const {commands, numCommands} = require('./arbitrary/commands.js')

module.exports = {
    command: command,
    commands: commands,
    numCommands: numCommands
};
