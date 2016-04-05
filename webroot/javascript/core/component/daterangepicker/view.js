define(
    [
        'app',
        'C',
        'Storage',
        'dispatcher',
        'marionette',
        'jquery',
        'underscore',
        'moment',
        'i18n',
        'daterangepicker'
    ],
    function (AppCube,C, Storage,Dispatcher, Marionette, $, _, moment,i18n) {

        var cache_format = 'YYYY-MM-DD HH:mm:ss.SSS';

        return Marionette.ItemView.extend({
            beforeShow: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.on('Request.getValue:' + eventName, this.getValue, this, 'Component');
            },
            renderRangerPicker: function () {
                var self = this;
                var options = $.extend(true,{},this.options.daterangepicker);
                var cache_start = Storage.get('cache_start_date');
                var cache_end = Storage.get('cache_end_date');
                var start = cache_start?cache_start:options.startDate.format(cache_format);
                var end = cache_end?cache_end:options.endDate.format(cache_format);
                if(cache_start)options.startDate = moment(cache_start,cache_format);
                if(cache_end)options.endDate = moment(cache_end,cache_format);
                //doI18n for daterange
                //locale
                _.forEach(options.locale,function(item,index){
                    options.locale[index] = i18n.t(item);
                });
                //label
                var range = {};
                _.forEach(options.ranges,function(item,index){
                    range[i18n.t(index)] = item;
                });
                options.ranges = range;

                this.$('.daterangepicker-parts').daterangepicker(options, function (start, end) {
                    var momentcode = C.get('moment');
                    Storage.set('cache_end_date',end.format(cache_format));
                    Storage.set('cache_start_date',start.format(cache_format));
                    self.$('.daterangepicker-parts>span').html(start.locale(momentcode).format(' ll') + ' - ' + end.locale(momentcode).format('ll'));
                });
                if(cache_start&&cache_end){
                    var days = options.endDate.diff(options.startDate, 'days');
                    setTimeout(function(){
                        Dispatcher.trigger('toggle:Time', days, 'Component');
                    },0)
                }
                start = moment(start,cache_format).format('ll');
                end = moment(end,cache_format).format('ll');
                this.$('.daterangepicker-parts>span').html(' ' + start + ' - ' + end);
                this.$('.daterangepicker-parts').on('apply.daterangepicker', function (ev, picker) {
                    var days = picker.endDate.diff(picker.startDate, 'days');
                    Dispatcher.trigger('change:Time', days, 'Component');
                });
            },
            getValue: function () {
                var calendar = this.$('.daterangepicker-parts').data('daterangepicker');
                return calendar ? calendar : null;
            },
            render: function () {
                this.$el.html('<div class="ui icon basic button daterangepicker-parts pull-right">' +
                '<i class="icomoon icomoon-calendar"></i><span></span><i class="icon dropdown"></i></div>');
                this.renderRangerPicker();
            },
            beforeHide: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.off('Request.getValue:' + eventName, 'Component');
                var calendar = this.$('.daterangepicker-parts').data('daterangepicker');
                if (calendar)calendar.remove();
            }
        });
    });