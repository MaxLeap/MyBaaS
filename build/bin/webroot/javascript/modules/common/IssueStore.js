define([
    'app',
    'Timezone',
    'API',
    'data/Task',
    'store/RestStore',
    'Q',
    'underscore'
], function (AppCube, Timezone, API, Task, RestStore, Q, _) {
    var IssueStore = RestStore.extend({
        getIssueById: function (id) {
            var defer = Q.defer();
            var data = this._origin_data.data.result;
            var result = _.find(data, function (item) {
                return item.objectId == id;
            });
            if (result) {
                defer.resolve(result);
                return defer.promise;
            } else {
                return this.getDataById(id);
            }
        },
        getDataById: function (id) {
            var task = this._origin_data.data;
            var defer = Q.defer();
            var url = task.options.url + '/' + id;
            Task.create({
                url: url,
                formatter: function (res) {
                    res.createdAt = Timezone.parseServerTime(res.createdAt);
                    res.updatedAt = Timezone.parseServerTime(res.updatedAt);
                    res.lastReply = Timezone.parseServerTime(res.lastReply);
                    if(res.msgs && res.msgs.length>0){
                        Timezone.handlerTimePropertiesNoFormat(res.msgs,[
                            'createdAt',
                            'updatedAt'
                        ]);
                    }
                    return res;
                }
            }).start().done(function (res) {
                defer.resolve(res);
            });
            return defer.promise;
        },
        addMessage: function (options, id) {
            var defer = Q.defer();
            var url = API.get('Issue') + '/msg/' + id;
            var tmp_task = Task.create({
                url: url,
                method: 'post',
                params: options
            });
            tmp_task.start().done(function (res) {
                if (tmp_task.state != 3) {
                    defer.resolve(res);
                } else {
                    defer.reject(res);
                }
            });
            return defer.promise;
        },
        batchData: function (options) {
            var defer = Q.defer();
            var url = API.get('Issue') + '/batch';
            var tmp_task = Task.create({
                url: url,
                method: 'put',
                params: options
            });
            tmp_task.start().done(function (res) {
                if (tmp_task.state != 3) {
                    defer.resolve(res);
                } else {
                    defer.reject(res);
                }
            });
            return defer.promise;
        },
        addData: function (options) {
            var task = this._origin_data.data;
            var defer = Q.defer();
            var type = this._origin_data.type;
            if (type == 'data') {
                throw new Error('RestStore: "add" static data to be implement');
            } else {
                var tmp_task = Task.create({
                    url: task.options.url+'/create',
                    method: 'post',
                    header: {
                        'X-LAS-AppId': options.appId
                    },
                    params: options
                });
                tmp_task.start().done(function (res) {
                    if (tmp_task.state != 3) {
                        defer.resolve(res);
                    } else {
                        defer.reject(res);
                    }
                });
            }
            return defer.promise;
        },
        updateData: function (id, options) {
            var task = this._origin_data.data;
            var defer = Q.defer();
            var type = this._origin_data.type;
            if (type == 'data') {
                var item = _.find(task, function (o) {
                    return o.id == id;
                });
                _.extend(item, options);
                //todo
                defer.resolve(item);
            } else {
                var url = task.options.url + '/' + id;
                var tmp_task = Task.create({
                    url: url,
                    method: 'put',
                    header: {
                        'X-LAS-AppId': options.appId
                    },
                    params: options
                });
                tmp_task.start().done(function (res) {
                    if (tmp_task.state != 3) {
                        defer.resolve(res);
                    } else {
                        defer.reject(res);
                    }
                });
            }
            return defer.promise;
        }
    });

    IssueStore.create = function (options) {
        var ret = new IssueStore();
        if (ret.initialize(options || {}) == false) {
            return false;
        }
        return ret;
    };

    return IssueStore;
});