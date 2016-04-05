define([
    'app',
    'API',
    './BasicLayout',
    'jquery',
    'underscore'
], function (AppCube, API, BasicLayout, $, _) {

    var FadeLayout = BasicLayout.extend({
        initialize: function (options) {
            BasicLayout.prototype.initialize.call(this, options);
            //init loader
            var content = $(this.options.root).parent();
            if (content.find('#fade-loader').length == 0) {
                content.append('<div id="fade-loader"></div>');
                var loader = {};
                loader.init = false;
                $('#fade-loader').data('loader', loader);
            }
        },
        show: function (options) {
            if (this.options.action == options.action) {
                this.changeState(options.state || this._defaultState);
                if (this.options.store)this.loadStoreData();
                var self = this;
                AppCube.current_action = options.action;
                AppCube.current_state = this._currentState;
                var loader = $('#fade-loader').data('loader');
                if (!loader.init) {
                    loader.init = true;
                    self.renderState(options.options);
                    $(self.options.root).fadeIn(200);
                } else {
                    var interval = loader.animateInterval;
                    if (interval) {
                        clearTimeout(interval);
                    }
                    loader.animateInterval = setTimeout(function () {
                        self.renderState(options.options);
                        $(self.options.root).fadeIn(200);
                    }, 400);
                }
            }
        },
        hide: function (options) {
            if (this.options.action != options.action) {
                if ($(this.options.root).css('display') != 'none') {
                    var self = this;
                    $(this.options.root).fadeOut(350, function () {
                        self.clearState();
                    });
//                    var loader = $('#dom-loader').data('loader');
//                    var self = this;
//                    loader.hide().done(function(){
//                        $(self.options.root).hide();
//                        self.clearState();
//                    });
                }
            }
        }
    });

    FadeLayout.create = function (options) {
        var ret = new FadeLayout();
        if (ret.initialize(options) == false) {
            return false;
        }
        return ret;
    };

    return FadeLayout;
});