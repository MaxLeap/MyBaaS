define([
    'app',
    'U',
    'i18n',
    'Logger',
    'jquery',
    'underscore',
    'semanticui_dropdown2'
], function (AppCube, U, i18n, Logger, $, _, semanticui_dropdown2) {
    return {
        initSelect: function (options) {
            //静态数据
            var $el = options.el,
                data = options.data || [],
                placeholder = options.placeholder || "",
                multiple = options.multiple || false;

            //格式化
            data = _.map(data, function (item) {
                if (item) {
                    return {
                        id: item[options.valueName || 'id'] || item,
                        text: item[options.textName || 'text'] || item
                    }
                }
            });
            //dropdown Options
            var dropdownOptions = _.extend({}, options);
            if (options.multiple) {
                dropdownOptions.action = 'multiple';
            }
            if (options.tags) {
                dropdownOptions.allowCreate = true;
            }
            if (options.placeholder) {
                dropdownOptions.metadata = dropdownOptions.metadata || {};
                {
                    placeholderText:options.placeholder;
                }
            }

            if (!$el.data('moduleDropdown2')) {
                $el.dropdown2(dropdownOptions);
            } else {
                this.setValue(options);
            }

            if (typeof options.callback == 'function') {
                options.callback();
            }
        },
        loadSelect: function (options) {
            var self = this;
            //加载数据
            options = options || {};
            if (options.storeName) {
                AppCube.DataRepository.fetch(options.storeName, options.storeState, options.storeOptions).done(function (res) {
                    if (!res) {
                        return Logger.error("");
                    }
                    //排序在 store 中进行
                    options.data = res;

                    self.initSelect(options);
                });
            } else {
                self.initSelect(options);
            }
        },
        initLang: function (options) {
            //语言
            options.storeName = options.storeName || "Store:Lang";
            options.placeholder = options.placeholder || "Language";
            this.loadSelect(options);
        },
        initCountry: function (options) {
            //国家
            options.storeName = options.storeName || "Store:Country";
            options.placeholder = options.placeholder || "Country";
            this.loadSelect(options);
        },
        getValue: function (options) {
            options = options || {};
            var $el = options.el;
            if(!$el){
                return ""
            }
            return $el.dropdown2('get value') || $el.val() || "";
        },
        getText: function (options) {
            options = options || {};
            var $el = options.el;
            var rtn = $el.dropdown2('get text');
            if(!rtn){
                rtn = $el.text();
            }
            return rtn
        },
        setValue: function (options) {
            options = options || {};
            var $el = options.el;
            if (options.data) {
                $el.dropdown2('set data', options.data);
            } else if (options.storeName) {
                AppCube.DataRepository.fetch(options.storeName, options.storeState, options.storeOptions).done(function (res) {
                    if (!res) {
                        return
                    }
                    //排序在 store 中进行
                    var data = _.map(res, function (item) {
                        if (item) {
                            return {
                                id: item[options.valueName || 'id'] || item,
                                text: item[options.textName || 'text'] || item
                            }
                        }
                    });
                    $el.dropdown2('set data', data);
                });
            }

        },
        destoryAll: function (options) {

        }
    }
});