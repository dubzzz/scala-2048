"use strict";
const jsc = require('jsverify');

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
    commands: arbCommands,
    numCommands: arbNumCommands
};
