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
    'semanticui_dropdown'
],function(AppCube,C,U,Dispatcher,UI,template,Marionette,$,_,i18n){
    return Marionette.ItemView.extend({
        template: _.template(template),
        meta:{
            defaultClass:'',
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
            <div class="item" data-value="<%= '+ this.meta.valueName +' %>">'+
                (this.meta.content?this.meta.content:'<%= '+ this.meta.textName +' %>')+
            '</div>');
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
            this.$('.menu').html(tmp);
            if(isEmpty){
                this.$('.dropdown>.menu').append($('<div class="message">'+i18n.t('common.title.not-found')+'</div>'));
            }
            this.$('.ui.dropdown').dropdown('refresh');
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
            this.$('.ui.dropdown').dropdown({
                message:{
                    noResults:this.meta.noDataInfo
                },
                action:this.options.action||'activate',
                onNoResults:function(){
                    var menu = $(this).children('.menu');
                    setTimeout(function(){
                        menu.children('.message').text(i18n.t('common.title.not-found'));
                    });
                    return true;
                }
            });
            this.setValue("");
            this.refresh();
        },
        refresh: function () {
            var storeName = this.options.storeName;
            AppCube.DataRepository.refresh(storeName);
        },
        setValue: function (value) {
            this.$('.ui.dropdown').dropdown('set value',value);
        },
        getValue: function () {
            return this.$('.ui.dropdown').dropdown('get value');
        }
    })
});