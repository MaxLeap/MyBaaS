define([
    'dispatcher',
    'marionette',
    'jquery',
    'underscore',
    'i18n'
], function (Dispatcher, Marionette, $, _,i18n) {
    /**
     * valueEventName
     * buttons: name,value
     * defaultValue
     */
    return Marionette.ItemView.extend({
        template: _.template('<div class="ui basic buttons"></div>'),
        events: {
            "click .buttons>.button": "clickItem"
        },
        init: function () {
        },
        beforeShow: function () {
            var eventName = this.options.valueEventName;
            Dispatcher.on('Request.getValue:' + eventName, this.getValue, this, 'Component');
        },
        getValue: function () {
            return this.$('.buttons').attr('data-value');
        },
        clickItem: function (e) {
            if ($(e.currentTarget).hasClass('active')) {
                return false;
            } else {
                this.$('.buttons>.button').removeClass('active');
                $(e.currentTarget).addClass('active');
                var value = $(e.currentTarget).attr('data-value');
                this.$('.buttons').attr('data-value', value);
            }
            var eventName = this.options.valueEventName;
            Dispatcher.trigger('Click:'+eventName,this.getValue(),'Component');
        },
        render: function () {
            var tmp = [];
            var self = this;
            Marionette.ItemView.prototype.render.call(this);
            _.forEach(this.options.buttons, function (item, index) {
                var className = item.className || '';
                var labelName = self.options.doI18n?i18n.t(item.name):item.name;
                var node = $('<div class="ui button ' + className + '" data-value="' + item.value + '">' + labelName + '</div>');
                tmp.push(node);
            });
            this.$('.buttons').html(tmp);
            if (typeof this.options.defaultValue != 'undefined') {
                var value = this.options.buttons[this.options.defaultValue].value;
                this.$('.buttons').attr('data-value', value);
                this.$('.buttons>.button:eq(' + this.options.defaultValue + ')').addClass('active');
            }
        },
        beforeHide: function () {
            var eventName = this.options.valueEventName;
            Dispatcher.off('Request.getValue:' + eventName, 'Component');
        }
    });
});