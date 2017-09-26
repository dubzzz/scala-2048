const jsc = require('jsverify');

"use strict";

var arbCommand = function(TypeName, ...arbs) {
    var Builder = function(parameters) {
        TypeName.apply(this, parameters);
    }
    Builder.prototype = TypeName.prototype;

    return jsc.bless({
        generator: function(size) {
            var parameters = arbs.length === 0 ? [] : jsc.tuple(arbs).generator(size);
            return {
                command: new Builder(parameters),
                parameters: parameters
            };
        },
        shrink: function(cmd) {
            var parameters = arbs.length === 0 ? [] : jsc.tuple(arbs).shrink(cmd.parameters);
            return {
                command: new Builder(parameters),
                parameters: parameters
            };
        },
        show: function(cmd) {
            return cmd.command.toString();
        }
    });
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
            var arrsize = jsc.random(0, maxSize);
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

var arbNumCommands = function(num, ...commands) {
    return jscCommandsArray(
        jsc.oneof.apply(this, commands),
        num || 100);
};

var arbCommands = function(...commands) {
    return arbNumCommands.apply(this, [undefined].concat(commands));
};

module.exports = {
    command: arbCommand,
    commands: arbCommands,
    numCommands: arbNumCommands
};
