define(
    [
        'app',
        'Storage',
        'C',
        'dispatcher',
        'marionette',
        'jquery',
        'underscore',
        'moment',
        'daterangepicker'
    ],
    function (AppCube, Storage, C, Dispatcher, Marionette, $, _, moment) {

        var cache_format = 'YYYY-MM-DD HH:mm:ss.SSS';

        return Marionette.ItemView.extend({
            beforeShow: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.on('Request.getValue:' + eventName, this.getValue, this, 'Component');
            },
            renderTimePicker: function () {
                var self = this;
                var startDate,startCache;

                this.$('.datetimepicker-parts').daterangepicker(_.extend({}, C.get('UI.DateTimePicker')));


                if(startCache = Storage.get('cache_start_date')){
                    startDate = moment(startCache,cache_format);
                    this.$('.datetimepicker-parts').data('daterangepicker').setStartDate(startDate);
                    this.$('.datetimepicker-parts').data('daterangepicker').setEndDate(startDate);
                }else if(this.options.datetimepicker.start){
                    startDate = moment().subtract(-(this.options.datetimepicker.start||0),'days');
                    this.$('.datetimepicker-parts').data('daterangepicker').setStartDate(startDate);
                    this.$('.datetimepicker-parts').data('daterangepicker').setEndDate(startDate);
                }

                this.$('.datetimepicker-parts>span').text(startDate.format('ll'));

                this.$('.datetimepicker-parts').on('apply.daterangepicker', function (ev, picker) {
                    self.$('.datetimepicker-parts>span').text(picker.startDate.format('ll'));
                    Storage.set('cache_start_date',picker.startDate.format(cache_format));
                    Dispatcher.trigger('change:Time', 0 , 'Component');
                });

            },
            getValue: function () {
                var calendar = this.$('.datetimepicker-parts').data('daterangepicker');
                return calendar ? calendar.endDate : null;
            },
            render: function () {
                this.$el.html('<div class="ui basic icon button datetimepicker-parts pull-right">' +
                '<i class="icomoon icomoon-calendar"></i><span></span><i class="icon dropdown"></i></div>');
                this.renderTimePicker();
            },
            beforeHide: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.off('Request.getValue:' + eventName, 'Component');
                var calendar = this.$('.datetimepicker-parts').data('daterangepicker');
                if(calendar&&this.$('.datetimepicker-parts').is(':visible')){
                    calendar.remove();
                }
            }
        });
    });