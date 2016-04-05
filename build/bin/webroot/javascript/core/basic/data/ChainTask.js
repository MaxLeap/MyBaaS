define([
    'data/Task',
    'Q',
    'underscore'
], function (Task, Q, _) {
    var INIT = 0, PENDING = 1, SUCCESS = 2, ERROR = 3;
    var defaults = ['params','method','header','url','timeout','formatter','buffer','next','queryUrl','extractUrl'];

    var ChainTask = Task.extend({
        initialize: function (taskArray,options,mainOptions) {
            if (!_.isArray(taskArray))throw new Error('ChainTask: ChainTask need a Task Array to initialize');
            this._taskStack = taskArray;
            this._configure(options);
            this.mainOptions = _.pick(mainOptions, defaults);
            this.result = [];
            this.state = INIT;
            this.defer = Q.defer();
            return true;
        },
        _configure: function(options){
            var new_options = _.extend({},this._taskStack[0].options,options);
            this._taskStack[0].options = new_options;
            this.options = new_options;
        },
        start: function () {
            if (this.state == PENDING) {
                return this.defer.promise;
            } else if (this.state == SUCCESS || this.state == ERROR) {
                this.defer = Q.defer();//Update defer
            }
            this.state = PENDING;
            this.result = [];
            var count = 0;
            this.startTask(count);
            return this.defer.promise;
        },
        startTask: function (count, options) {
            var self = this;
            var task = this._taskStack[count];
            var newOptions = _.extend({},task.options,options||{});
            task._configure(newOptions);
            task.start().done(function (res) {
                self.result.push(res);
                var data = task.options.next ? task.options.next(res, task.options) : null;
                var nextTaskIndex = (data&&typeof data.goNextIndex == 'number')?data.goNextIndex:(count + 1);
                if (nextTaskIndex < self._taskStack.length) {
                    if(data.goNext === false){
                        self.state = SUCCESS;
                        self.result = self.mainOptions.formatter?self.mainOptions.formatter(self.result):self.result;
                        self.defer.resolve(data.result);
                    }else{
                        self.startTask(nextTaskIndex, data.options);
                    }
                } else {
                    self.state = SUCCESS;
                    self.result = self.mainOptions.formatter?self.mainOptions.formatter(self.result):self.result;
                    self.defer.resolve(self.result);
                }
                task.result = task.options.buffer||{};
            });
        }
    });

    ChainTask.create = function (taskArray,options) {
        var ret = new ChainTask();
        if (ret.initialize(taskArray,options) == false) {
            return false;
        }
        return ret;
    };

    return ChainTask;
});