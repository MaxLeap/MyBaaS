define([
    'app',
    'U',
    'dispatcher',
    'Logger',
    'core/functions/UI',
    'container/BasicContainer',
    '../main/sidebar/newClass/view',
    'jquery',
    'underscore',
    'i18n'
], function (AppCube,U , Dispatcher, Logger, UI, BasicContainer, newClass, $, _, i18n) {
    var Container = BasicContainer.extend({
        events: {
            "click .app-button.add-class": "addClass"
        },
        render:function(options){
            BasicContainer.prototype.render.call(this,options);
            this._root.i18n();
        },
        addClass:function(){
            UI.showDialog(i18n.t("clouddata.title.new-class"),newClass,{
                success:function(view){
                    if(view.isValid()) {
                        var value = view.getValue();
                        view.showLoading();
                        AppCube.DataRepository.getStore('Store:Schemas').addData(value).then(function(res){
                            window.location.reload();
                        }).finally(function(){
                            view.hideLoading();
                        });
                    }
                }
            });
        }
    });

    Container.create = function (options) {
        var ret = new Container();
        //options.template = template;
        options.root = '.container-content';
        if (ret.initialize(options) == false) {
            return false;
        }
        return ret;
    };

    return Container;
});