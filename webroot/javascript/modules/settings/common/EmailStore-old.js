define([
    'API',
    'C',
    'data/Task',
    'store/RestStore',
    'Q',
    'underscore'
], function (API,C, Task, RestStore, Q, _) {
    Q.longStackSupport = true;
    var EmailStore = RestStore.extend({
        updateEmail:function(data){
            var defer = Q.defer();
            if(!_.isArray(data)||data.length==0){
                throw new Error('length must gt 0');
            }
            var requests = [];
            _.forEach(data,function(item){
               var request = {
                   method:'put',
                   path:'/2.0/apps/'+ C.get('User.AppId')+'/emailtemplates/'+item.oid,
                   body:{
                       from:item.from,
                       subject:item.subject,
                       content:item.content
                   }
               };
                requests.push(request);
            });
            var tmp_task = Task.create({
                url: API.get('Data.Batch'),
                method: 'post',
                params: {
                    requests:requests
                }
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
        //增删改查
        crudTemplate: function(type, objectId, options) {
            //arguments处理
            if (typeof objectId !== "string") {
                options = objectId || {};
                objectId = undefined;
            }
            var defer = Q.defer();
            var self = this;
            var tmp_task = Task.create({
                url: API.get("Data.EmailTpl") + (objectId ? ("/" + objectId) : ""),
                method: type,
                params: options.params || {},
                header: options.header || {}
            });
            tmp_task.start().done(function(res) {
                if (tmp_task.state != 3) {
                    defer.resolve(res);
                } else {
                    defer.reject(res);
                }
            });
            return defer.promise;
        },
        deleteTemplate: function(id) {
            var self = this;
            var options = {
                header: self.header
            };
            return self.crudTemplate("delete", id, options);
        },
        addTemplate: function(options) {
            var self = this;
            return self.crudTemplate("post", options);
        },
        updateTemplate: function(id,options) {
            var self = this;
            return self.crudTemplate("put",id, options);
        },
        getTemplates:function(){
            var self=this;
            return self.crudTemplate("get",options);
        }
    });

    EmailStore.create = function (options) {
        var ret = new EmailStore();
        if (ret.initialize(options || {}) == false) {
            return false;
        }
        return ret;
    };

    return EmailStore;
});