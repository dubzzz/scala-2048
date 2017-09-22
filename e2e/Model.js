'use strict';

function Model() {
    var self = this;
    self.gameId = 0;
    self.current = "";
    self.history = "";
    self.states = [{}];

    self.canUndo = function() {
        return self.current.length > 0;
    };
    self.canRedo = function() {
        return self.current.length < self.history.length;
    };

    self.play = function(direction) {
        if (self.current.length == self.history.length) {
            self.current += direction;
            self.history += direction;
        }
        else if (self.history[self.current.length] == direction) {
            self.current += direction;
        }
        else {
            self.current += direction;
            self.history = self.current.substr(0);
        }
        return self;
    };
    self.undo = function() {
        if (! self.canUndo()) {
            return self;
        }
        self.current = self.current.substr(0, self.current.length -1);
        return self;
    };
    self.redo = function() {
        if (! self.canRedo()) {
            return self;
        }
        self.current += self.history[self.current.length];
        return self;
    };
    self.newGame = function() {
        ++self.gameId;
        self.current = "";
        self.history = "";
        self.states.push({});
        return self;
    };

    self.store = function(url, game) {
        var data = {url: url, game: game};
        if (self.states[self.gameId][self.current]) {
            var prev = self.states[self.gameId][self.current];
            if (prev.url != url || prev.game != game) {
                console.warn("Expecting state '" + self.current + "' to be " + JSON.stringify(prev) + " received " + JSON.stringify(data));
                return false;
            }
        }
        self.states[self.gameId][self.current] = data;
        return true;
    };
};

module.exports = Model;
