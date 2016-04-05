define([
    'dispatcher',
    'marionette',
    'jquery',
    'underscore',
    'i18n'
], function (Dispatcher, Marionette, $, _) {
    return Marionette.ItemView.extend({
        template: _.template('<div class="ui button"></div>'),
        events: {
            "click": "clickItem"
        },
        init: function () {
        },
        beforeShow: function () {

        },
        getValue: function () {
            return this.options.value ? this.options.value : this.options.content;
        },
        clickItem: function (e) {
            if (this.options.valueEventName) {
                Dispatcher.trigger('Click:' + this.options.valueEventName, {}, 'Component');
            }
        },
        render: function () {
            if (this.options.isLink){
                this.$el.html('');
                var node = $(this.options.content).appendTo(this.$el);
                node.addClass('ui button');
                if (this.options.className)node.addClass(this.options.className);
            }else{
                Marionette.ItemView.prototype.render.call(this);
                if (this.options.content)this.$('.ui.button').html(this.options.content);
                if (this.options.className)this.$('.ui.button').addClass(this.options.className);
            }
            this.$el.i18n();
        },
        beforeHide: function () {

        }
    });
});