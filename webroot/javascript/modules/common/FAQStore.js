define(['API', 'data/Task', 'store/RestStore', 'Q', 'underscore'], function (API, Task, RestStore, Q, _) {
    var FAQStore = RestStore.extend({
        getFAQById: function (id) {
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
        }
    });

    FAQStore.create = function (options) {
        var ret = new FAQStore();
        if (ret.initialize(options || {}) == false) {
            return false;
        }
        return ret;
    };

    return FAQStore;
});