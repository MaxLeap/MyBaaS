define(
    [
        'app',
        'dispatcher',
        'marionette',
        'jquery',
        'underscore',
        'semanticui_dropdown',
        'i18n',
    ],
    function (AppCube, Dispatcher, Marionette, $, _,semanticui_dropdown,i18n) {

        return Marionette.ItemView.extend({
            init: function () {
                this.options = this.options || {};
                var storeName = this.options.storeName;
                Dispatcher.on('refresh:' + storeName, this.renderComponent, this, 'Component');
                Dispatcher.on('Request.getValue', this.getValue, this, 'Component');
            },
            showNoData: function () {
                console.log('show no data');
            },
            hideNoData: function () {
                console.log('hide no data');
            },
            showLoading: function () {
                console.log('show loading');
            },
            hideLoading: function () {
                console.log('hide loading');
            },
            getValue: function () {
                return {}
            },
            refresh: function () {
                var storeName = this.options.storeName;
                AppCube.DataRepository.refresh(storeName);
            },
            renderComponent:function(){
                console.log('renderComponent'); 
            },
            renderDropdown:function(){
                this.$('.dropdown-parts').each(function(index,e){
                    if(!$(e).data('moduleDropdown')){
                        $(e).dropdown();
                    }
                });
            },
            render:function(){
                Marionette.ItemView.prototype.render.call(this);
                this.renderDropdown();
                this.$el.i18n();
            },
            beforeHide:function(){
                if( this.$('.select2-parts,.dropdown-parts').length>0){
                    this.$('.select2-parts,.dropdown-parts').each(function (index, e) {
                        if($(e).data('moduleDropdown2')){
                            $(e).dropdown2('destroy');
                        }else if($(e).data('moduleDropdown')){
                            $(e).dropdown('destroy');
                        }
                    });
                }

                var eventName = this.options.valueEventName ? ':' + this.options.valueEventName : '';
                Dispatcher.off('Request.getValue' + eventName, 'Component');
                var storeName = this.options.storeName;
                Dispatcher.off('refresh:' + storeName, 'Component');
            },
            destroy: function () {
                Dispatcher.off('Request.getValue', 'Component');
            }
        });
    });