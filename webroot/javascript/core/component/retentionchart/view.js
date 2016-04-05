define(
    [
        'app',
        'U',
        'dispatcher',
        'tpl!./template.html',
        'marionette',
        'jquery',
        'underscore',
        'moment',
        'extend/ui/RetentionChart',
        'i18n'
    ],
    function (AppCube, U, Dispatcher, template, Marionette, $, _, moment, RetentionChart, i18n) {
        /**
         * title
         * ranges
         * tabs
         * time
         * stats
         * options (highchart options)
         * compared
         * maxLength
         * storeName
         * stateName
         * valueEventName
         */

        return Marionette.ItemView.extend({
            template: template,
            events: {
                "mousemove .data_content>.retention_body .retention_grid":"showTip",
                "mouseleave .data_content>.retention_body .retention_grid":"hideTip",
                "click .tabs div": "clickTabs",
                "click .buttons.stats>.button": "clickStats"
            },
            init: function () {
                var storeName = this.options.storeName;
                Dispatcher.on('refresh:' + storeName, this.renderComponent, this, 'Component');
            },
            beforeShow: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.on('change:Time', this.setTimeUnit, this, 'Component');
                Dispatcher.on('toggle:Time', this.setDefaultTimeUnit, this, 'Component');
                Dispatcher.on('Request.getValue:' + eventName, this.getValue, this, 'Component');
            },
            beforeHide: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.off('Request.getValue:' + eventName, 'Component');
                Dispatcher.off('change:Time', 'Component');
                Dispatcher.off('toggle:Time', 'Component');
                var chart = this.chart;
                if (chart){
                    chart.destroy();
                    this.chart = null;
                }
            },
            initChart: function () {
                var title = this.options.doI18n?i18n.t(this.options.title):this.options.title;
                this.$('.caption').html(title);
                this.ranges = this.options.ranges;
                if (this.options.time && this.options.time.length > 0) {
                    this.initTime();
                }
                if (this.options.timerange && this.options.timerange.length > 0) {
                    this.initTimeRange();
                }
            },
            initTime: function () {
                var times = this.options.time;
                var self = this;
                var default_time_unit = this.options.default_time_unit;
                this.time_unit = times[default_time_unit || 0].value;
                _.forEach(times, function (time, index) {
                    var timeName = self.options.doI18n?i18n.t(time.name):time.name;
                    self.$('.tabs').append('<div class="' + (index == default_time_unit ? 'active' : '') + '" data-value="' + time.value + '">' +
                        timeName + '</div>');
                });
                if (this.setTimeUnit)this.setTimeUnit(this.ranges, true);
            },
            initTimeRange:function (){
                var times = this.options.timerange;
                var self = this;
                var default_time_unit = this.options.default_time_unit;
                this.time_unit = times[default_time_unit || 0].value;
                _.forEach(times, function (time, index) {
                    var node = $('<div class="' + (index == default_time_unit ? 'active' : '') +
                        time.name + '</div>');
                    node.attr('data-value',JSON.stringify(time.value));
                    self.$('.tabs>.nav').append(node);
                });
            },
            setTimeUnit: function (length, refresh) {
                var self = this;
                var times = this.options.time;
                var time_unit = this.time_unit;
                var days = length;
                this.ranges = days;
                this.$('.tabs div').addClass('disabled');
                var prev = "";
                for (var i in times) {
                    var time = times[i];
                    if (days >= time.length - 1) {
                        prev = time.value;
                        self.$('.tabs div[data-value=' + time.value + ']').removeClass('disabled');
                    } else {
                        var node = self.$('.tabs div[data-value=' + time_unit + ']');
                        if (node.hasClass('disabled'))time_unit = prev;
                        break;
                    }
                }
                if (!refresh)this.changeTimeUnit(time_unit);
                return time_unit;
            },
            setDefaultTimeUnit: function(length){
                var time_unit = this.setTimeUnit(length,true);
                this.$('.tabs div').removeClass('active');
                this.$('.tabs div[data-value=' + time_unit + ']').addClass('active');
                this.time_unit = time_unit;
            },
            clickTabs: function (e) {
                if ($(e.currentTarget).hasClass('disabled'))return;
                var time_unit = $(e.currentTarget).attr('data-value');
                this.changeTimeUnit(time_unit)
            },
            changeTimeUnit: function (time_unit) {
                this.$('.tabs div').removeClass('active');
                this.$('.tabs div[data-value=' + time_unit + ']').addClass('active');
                this.time_unit = time_unit;
                this.refresh();
            },
            showLoading: function () {
                this.chart.clear();
                this.$('.view-placeholder').addClass('show');
                this.$('.view-placeholder>.loading-view').show();
            },
            hideLoading: function () {
                this.$('.view-placeholder').removeClass('show');
                this.$('.view-placeholder>.loading-view').hide();
            },
            showNoData: function () {
                this.chart.clear();
                this.$('.view-placeholder').addClass('show');
                this.$('.view-placeholder>.no-data-view').show();
            },
            hideNoData: function () {
                this.$('.view-placeholder').removeClass('show');
                this.$('.view-placeholder>.no-data-view').hide();
            },
            renderChart: function (data) {
                this.hideLoading();
                if(!data||!data.stats||data.stats.length==0){
                    this.showNoData();
                }else{
                    this.hideNoData();
                    this.chart.setData(data.stats);
                    this.chart.setRange(this.time_unit);
                    this.chart.render();
                }
            },
            renderComponent: function () {
                var self = this;
                var storeName = this.options.storeName;
                var stateName = this.options.stateName;
                AppCube.DataRepository.fetch(storeName, stateName, this.stats).done(function (res){
                    self.renderChart(res);
                });
            },
            getValue: function () {
                return this.time_unit;
            },
            render: function () {
                Marionette.ItemView.prototype.render.call(this);
                this.$el.i18n();
                var chartOptions = this.options.retention;
                chartOptions.renderTo = '.retention-view>.chart-content';
                chartOptions.data = [];
                this.chart = new RetentionChart(chartOptions);
                this.initChart();
                this.refresh();
            },
            refresh: function () {
                var storeName = this.options.storeName;
                this.showLoading();
                AppCube.DataRepository.refresh(storeName);
            },
            getTipContent:function(date,index,value){
                var unit = this.time_unit=='daily'?'day':(this.time_unit=='weekly'?'week':'month');
                return i18n.t('analytics.tips.retention-tips',{
                    rate:value+'%',
                    date:moment(date,'YYYYMMDD').format('ll'),
                    time:i18n.t('analytics.tag.'+unit,{d:index})
                });
            },
            showTip:function(e){
                var value = $(e.currentTarget).attr('data-value');
                var index = $(e.currentTarget).attr('data-index');
                var date = $(e.currentTarget).attr('data-date');
                if(this.tooltip == null){
                    var tip = this.getTipContent(date,index,value);
                    this.tooltip = $('<div class="tip">'+tip+'</div>').appendTo(this.$('.retention-view'));
                }else{
                    this.tooltip.text(this.getTipContent(date,index,value));
                }
                var event = e.originalEvent;
                var width = this.tooltip.outerWidth();
                this.tooltip.css({
                    top:event.clientY - 100,
                    left:event.clientX - width/2
                });
            },
            hideTip:function(e){
                this.tooltip.remove();
                this.tooltip = null;
            }
        });
    });