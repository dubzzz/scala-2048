'use strict';

function Model() {
    var self = this;
    self.current = "";
    self.history = "";
    self.states = {};

    self.play = function(direction) {
        if (self.current.length == self.history.length || self.history[self.current.length] == direction) {
            self.current += direction;
            return self;
        }
        self.current += direction;
        self.history += direction;
        return self;
    };
    self.undo = function() {
        self.current = self.current.substr(0, self.current.length -1);
        return self;
    };
    self.redo = function() {
        self.current += self.history[self.current.length];
        return self;
    };

    self.store = function(url, game) {
        if (self.states[self.current]) {
            var prev = self.states[self.current];
            return prev.url == url && prev.game == game;
        }
        self.states[self.current] = {url: url, game: game};
        return true;
    };
};

module.exports = Model;
