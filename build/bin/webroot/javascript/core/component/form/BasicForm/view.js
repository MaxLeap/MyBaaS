define(
    [
        'app',
        'config/Directive',
        'dispatcher',
        'marionette',
        'jquery',
        'underscore',
        'bootstrap',
        'select2'
    ],
    function (AppCube, directive, Dispatcher, Marionette, $, _) {

        var basic_directives = [
//            {
//                name:'select2',
//                compile:function(view){
//                    view.$('[cb-select2]').each(function(index,element){
//                        var store = 'Store:'+element.getAttribute('cb-select2');
//                        var multiple = element.getAttribute('multiple')?true:false;
//                        AppCube.DataRepository.fetch(store).done(function(){
//                            $(element).select2();
//                        });
//
//                    });
//                }
//            },
            {
                name: 'date',
                compile: function ($el) {

                }
            },
            {
                name: 'show',
                compile: function (view) {
                    var channel = 'Form.' + view.cid;
                    view.$('[cb-show]').each(function (index, element) {
                        var bind_value = element.getAttribute('cb-bind');
                        Dispatcher.on('Change:' + bind_value, view.CB_SHOW.bind(view, element), null, channel);
                    });
                }
            },
            {
                name: 'model',
                compile: function (view) {
                    var channel = 'Form.' + view.cid;
                    view.$('[cb-model]').each(function (index, element) {
                        element.onchange = view.CB_CHANGE.bind(view, element);
                    });
                }
            }
        ];

        return Marionette.ItemView.extend({
            init: function () {
                var storeName = this.options.storeName;
                var extend_directives = directive;
                var custom_directives = this.options.directives;
                this.directives = _.extend({}, basic_directives, extend_directives, custom_directives);
                Dispatcher.on('refresh:' + storeName, this.renderComponent, this, 'Component');
            },
            CB_SHOW: function (element, value) {
                if (element.getAttribute('cb-show') == value) {
                    $(element).show();
                } else {
                    $(element).hide();
                }
            },
            CB_MODEL: function () {

            },
            beforeShow: function () {
            },
            showLoading: function () {
            },
            hideLoading: function () {
            },
            renderComponent: function () {
                var self = this;
                var storeName = this.options.storeName;
                var stateName = this.options.stateName;
                this.showLoading();
                AppCube.DataRepository.fetch(storeName, stateName).done(function (res) {
                    self.loadComponents(res);
                });
            },
            getValue: function () {
                var value = {};
                var self = this;
                return value;
            },
            loadComponents: function (data) {
                var self = this;
            },
            compileDirectives: function () {
                for (var i in this.directives) {
                    this.directives[i].compile(this);
                }
            },
            decompileDirectives: function () {
                for (var i in this.directives) {
                    //this.directives[i].decompile(root);
                }
            },
            render: function () {
                this.$el.html(this.options.template);
                this.compileDirectives();
                this.refresh();
            },
            refresh: function () {
                //var storeName = this.options.storeName;
                //AppCube.DataRepository.refresh(storeName);
            },
            isValid: function () {
                return true;
            },
            beforeHide: function () {
                this.decompileDirectives();
            }
        });
    });