define(['API', 'data/Task', 'store/RestStore', 'Q', 'underscore'], function (API, Task, RestStore, Q, _) {
    var SVStore = RestStore.extend({
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
        getSVById: function (id) {
            var data = this._origin_data.data.result;
            if ('new' == id)return {"where": "{\"status\":\"0\"}"};
            if ('open' == id)return {"where": "{\"status\":\"1\"}"};
            if ('closed' == id)return {"where": "{\"status\":{\"$gt\":\"1\"}}"};
            if ('all' == id)return false;
            if (id) {
                var result = _.find(data, function (item) {
                    return item.objectId == id;
                });
                return result ? result : false;
            }

        },
        orderData: function (id, nextId, prevId) {
            var defer = Q.defer();
            var url = API.get('Issue') + '/sview/seq/' + id;
            var params = {};
            if (nextId)params.nextSeq = parseFloat(nextId);
            if (prevId)params.preSeq = parseFloat(prevId);
            if (nextId || prevId) {
                var tmp_task = Task.create({
                    url: url,
                    method: 'put',
                    params: params
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

    SVStore.create = function (options) {
        var ret = new SVStore();
        if (ret.initialize(options || {}) == false) {
            return false;
        }
        return ret;
    };

    return SVStore;
});