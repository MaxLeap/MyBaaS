define([
    'app',
    'API',
    './BasicLayout',
    'jquery',
    'underscore'
], function (AppCube, API, BasicLayout, $, _) {

    var PageLayout = BasicLayout.extend({

        show: function (options) {
            console.log("show:" + options.action + "===============in " + JSON.stringify(this.options.action) + " stay:" + this.options.stay);
            //is showing now
            if ($(this.options.root).css('display') != 'none') {
                return
            }
            if (this.beforeShow) {
                this.beforeShow();
            }
            //some action should be stay
            if (this.options.stay || this.options.action == 'all' || ($.inArray(this.options.action, options.actions) > -1)) {
                if (!options.options.stayState) {
                    this.changeState(options.state || this._defaultState);
                }
                // AppCube.current_action = 'all';
                // AppCube.current_state = 'list';
                if (this.options.store)this.loadStoreData();
                this.renderState(options.options);
                $(this.options.root).show();
            }
            //msg:show this action
            else if (this.options.action == options.action) {
                if (!options.options.stayState) {
                    this.changeState(options.state || this._defaultState);
                }
                if (this.options.store)this.loadStoreData();
                AppCube.current_action = options.action;
                AppCube.current_state = this._currentState;
                this.renderState(options.options);
                $(this.options.root).show();
            }
        },
        hide: function (options) {
        }
    });

    PageLayout.create = function (options) {
        var ret = new PageLayout();
        if (ret.initialize(options) == false) {
            return false;
        }
        return ret;
    };

    return PageLayout;
});