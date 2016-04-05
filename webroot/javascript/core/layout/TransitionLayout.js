define([
    'app',
    'API',
    'ui/loader/SVGLoader',
    './BasicLayout',
    'jquery',
    'underscore'
], function (AppCube, API, SVGLoader, BasicLayout, $, _) {

    var TransitionLayout = BasicLayout.extend({
        initialize: function (options) {
            BasicLayout.prototype.initialize.call(this, options);
            //init loader
            var content = $(this.options.root).parent();
            if (content.find('#dom-loader').length == 0) {
                var loader = new SVGLoader(content.get(0));
                loader.init = false;
                $('#dom-loader').data('loader', loader);
            }
        },
        show: function (options) {
            if (this.options.action == options.action) {
                this.changeState(options.state || this._defaultState);
                if (this.options.store)this.loadStoreData();
                var self = this;
                var loader = $('#dom-loader').data('loader');
                AppCube.current_action = options.action;
                AppCube.current_state = this._currentState;
                if (!loader.init) {
                    loader.init = true;
                    self.renderState();
                    $(self.options.root).show();
                } else {
                    var interval = loader.animateInterval;
                    if (interval) {
                        clearTimeout(interval);
                    }
                    loader.animateInterval = setTimeout(function () {
                        loader.show();
                        self.renderState();
                        $(self.options.root).show();
                    }, 1000);
                }
            }
        },
        hide: function (options) {
            if (this.options.action != options.action) {
                if ($(this.options.root).is(':visible')) {
                    var loader = $('#dom-loader').data('loader');
                    var self = this;
                    loader.hide().done(function () {
                        $(self.options.root).hide();
                        self.clearState();
                    });
                }
            }
        }
    });

    TransitionLayout.create = function (options) {
        var ret = new TransitionLayout();
        if (ret.initialize(options) == false) {
            return false;
        }
        return ret;
    };

    return TransitionLayout;
});