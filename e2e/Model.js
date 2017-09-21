'use strict';

function Model() {
    var self = this;
    self.current = "";
    self.history = "";

    self.play = function(direction) {
        if (self.current.length == self.history.length || self.history[self.current.length] == direction) {
            self.current += direction;
            return;
        }
        self.current += direction;
        self.history += direction;
    };
    self.undo = function() {
        self.current = self.current.substr(0, self.current.length -1);
    }
    self.redo = function() {
        self.current += self.history[self.current.length];
    }
};

module.exports = Model;
