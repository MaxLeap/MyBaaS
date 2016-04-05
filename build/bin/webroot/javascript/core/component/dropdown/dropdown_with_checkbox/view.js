define([
    'app',
    'C',
    'U',
    'dispatcher',
    'core/functions/UI',
    'text!./template.html',
    'marionette',
    'jquery',
    'underscore',
    'i18n',
    'semanticui_dropdown',
    'semanticui_checkbox'
],function(AppCube,C,U,Dispatcher,UI,template,Marionette,$,_,i18n){
    return Marionette.ItemView.extend({
        template: _.template(template),
        events:{
            "click .item>.checkbox":"selectItem",
            "click .item.apply":"applySelect"
        },
        meta:{
            defaultText:'Select',
            valueName:'objectId',
            textName:'name',
            noDataInfo:'No Result Found'
        },
        init:function(){
            if(this.options.meta){
                var meta = _.clone(this.options.meta);
                if(this.options.doI18n) {
                    meta['defaultText'] = i18n.t(this.options.meta['defaultText']);
                    meta['noDataInfo'] = i18n.t(this.options.meta['noDataInfo']);
                }
                this.meta = _.extend({},this.meta,meta);
            }
            this.childTemplate = _.template('\
            <div class="item" data-name="<%= '+ this.meta.textName +' %>" data-value="<%= '+ this.meta.valueName +' %>">\
                <div class="ui checkbox">\
                <input type="checkbox">\
                <label><%= '+ this.meta.textName +' %></label>\
                </div>\
            </div>');
        },
        beforeShow: function () {
            var storeName = this.options.storeName;
            Dispatcher.on('refresh:' + storeName, this.renderComponent, this, 'Component');
            var eventName = this.options.valueEventName;
            Dispatcher.on('Request.getValue:' + eventName, this.getValue, this, 'Component');
        },
        beforeHide: function () {
            var storeName = this.options.storeName;
            Dispatcher.off('refresh:' + storeName, 'Component');
            var eventName = this.options.valueEventName;
            Dispatcher.off('Request.getValue:' + eventName, 'Component');

            this.$('.ui.dropdown').each(function(index,el){
                if($(el).data('moduleDropdown')){
                    $(el).dropdown('destroy')
                }
            });
        },
        renderDropdown: function (data,isEmpty) {
            var tmp = [];
            var template = this.childTemplate;
            _.forEach(data, function (item) {
                tmp.push($(template(item)));
            });
            this.$('.scrolling').html(tmp);
            if(isEmpty){
                this.$('.ui.dropdown').dropdown({
                    message:{
                        noResults:this.meta.noDataInfo
                    },
                    action:'nothing'
                });
            }else{
                this.$('.ui.dropdown').dropdown({
                    action:'nothing',
                    message:{
                        noResults:this.meta.noDataInfo
                    },
                    selector:{
                        item:".scrolling.menu>.item"
                    },
                    onNoResults:function(){
                        var menu = $(this).children('.menu');
                        if(menu.children('.message').length==0)$('<div class="message"></div>').insertBefore(menu.children('.divider'));
                        return true;
                    }
                });
            }
            this.$('.item>.checkbox').checkbox();
        },
        renderComponent: function () {
            var self = this;
            var storeName = this.options.storeName;
            var stateName = this.options.stateName;
            AppCube.DataRepository.fetch(storeName, stateName).done(function (res) {
                self.renderDropdown(res,res.length==0);
            });
        },
        render: function () {
            this.$el.html(this.template(this.meta));
            this.$el.i18n();
            this.setValue("");
            this.refresh();
        },
        refresh: function () {
            var storeName = this.options.storeName;
            AppCube.DataRepository.refresh(storeName);
        },
        setValue: function (value) {
            this.$('.ui.dropdown').dropdown('set value',value);
            this.$('.item>.checkbox').checkbox('uncheck');
            this.$('.ui.dropdown .item[data-value="'+value+'"] .checkbox').checkbox('check');
        },
        getValue: function () {
            return this.$('.ui.dropdown').dropdown('get value');
        },
        selectItem:function(e){
            var checked = $(e.currentTarget).checkbox('is checked');
            if(checked){
                this.$('.item>.checkbox').not(e.currentTarget).checkbox('uncheck');
                var $item = $(e.currentTarget).closest('.item');
                var value = $item.attr('data-value');
                this.setValue(value);
            }else{
                this.setValue("");
                this.$('.text').text(this.meta.defaultText);
            }
            e.stopPropagation();
        },
        applySelect:function(){
            this.$('.ui.dropdown').dropdown('hide');
        }
    })
});