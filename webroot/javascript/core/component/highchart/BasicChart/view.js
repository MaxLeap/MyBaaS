define(
    [
        'app',
        'C',
        'U',
        'dispatcher',
        'tpl!./template.html',
        'marionette',
        'jquery',
        'underscore',
        'moment',
        'i18n',
        'highcharts',
        'daterangepicker'
    ],
    function (AppCube, C, U, Dispatcher, template, Marionette, $, _, moment,i18n) {

        return Marionette.ItemView.extend({
            template: template,
            events: {
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
                $(window).bind('resize.'+this.cid,this,this.resizeChart.bind(this));
            },
            beforeHide: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.off('Request.getValue:' + eventName, 'Component');
                Dispatcher.off('change:Time', 'Component');
                Dispatcher.off('toggle:Time', 'Component');
                $(window).unbind('resize.'+this.cid);
                var chart = this.$('.chart-content').highcharts();
                if (chart)chart.destroy();
                this.$('.button.compare').each(function (index, item) {
                    var dtp = $(item).data('daterangepicker');
                    if (dtp)dtp.remove();
                });
            },
            initChart: function () {
                var title = this.options.doI18n?i18n.t(this.options.title):this.options.title;
                this.$('.caption').html(title);
                this.ranges = this.options.ranges;
                if (this.options.compared) {
                    this.initCompare();
                }
                if (this.options.stats_list && this.options.stats_list.length > 0) {
                    this.initStats();
                }else{
                    this.stats = this.options.stats;
                }
                if (this.options.time && this.options.time.length > 0) {
                    this.initTime();
                }
                if (this.options.timerange && this.options.timerange.length > 0) {
                    this.initTimeRange();
                }
            },
            initCompare: function () {
                var self = this;
                this.$('.portlet-title').after('' +
                '<div class="ui icon basic button compare">' +
                '<i class="icomoon icomoon-compare"></i>' + i18n.t('analytics.tag.compare') +
                '</div>');

                this.$('.button.compare').daterangepicker(_.extend({}, C.get('UI.DateTimePicker')));

                this.$('.button.compare').on('apply.daterangepicker', function (ev, picker) {
                    self.addSeries(picker.startDate);
                });
            },
            initStats: function () {
                var self = this;
                var stats = this.options.stats_list;
                var node = $('<div class="ui buttons stats"></div>');
                _.forEach(stats, function (tab) {
                    var tabName = self.options.doI18n?i18n.t(tab.name):tab.name;
                    node.append('<div class="ui button" data-unit="'+
                    (tab.unit||'analytics.unit.count')+'" data-value="' +
                    tab.stats + '">' + tabName + '</div>');
                });
                this.stats = this.options.stats;
                var item =  _.find(stats,function(item){return item.stats==self.stats;})||{};
                this.stats_unit = item.unit;

                node.children().first().addClass('active');
                this.$('.portlet-title').after(node);
            },
            initTime: function () {
                var self = this;
                var times = this.options.time;
                var default_time_unit = this.options.default_time_unit;
                this.time_unit = times[default_time_unit || 0].value;
                _.forEach(times, function (time, index) {
                    var timeName = self.options.doI18n?i18n.t(time.name):time.name;
                    self.$('.tabs').append('<div class="' + (index == default_time_unit ? 'active' : '') +
                    '" data-value="' + time.value + '">' +
                    timeName + '</div>');
                });
                if (this.setTimeUnit)this.setTimeUnit(this.ranges, true);
            },
            initTimeRange:function (){
                var self = this;
                var times = this.options.timerange;
                var default_time_unit = this.options.default_time_unit;
                this.time_unit = times[default_time_unit || 0].value;
                _.forEach(times, function (time, index) {
                    var timeName = self.options.doI18n?i18n.t(time.name):time.name;
                    var node = $('<div class="' + (index == default_time_unit ? 'active' : '') +
                        timeName + '</div>');
                    node.attr('data-value',JSON.stringify(time.value));
                    self.$('.tabs').append(node);
                });
            },
            setDefaultTimeUnit: function(length){
                var time_unit = this.setTimeUnit(length,true);
                this.$('.tabs div').removeClass('active');
                this.$('.tabs div[data-value=' + time_unit + ']').addClass('active');
                this.time_unit = time_unit;
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
                if (days > 7) {
                    self.$('.tabs div[data-value=hourly]').addClass('disabled');
                    if (time_unit == 'hourly')time_unit = 'daily';
                }
                if (!refresh)this.changeTimeUnit(time_unit);
                return time_unit;
            },
            clickTabs: function (e) {
                if ($(e.currentTarget).hasClass('disabled'))return;
                var time_unit = $(e.currentTarget).attr('data-value');
                this.changeTimeUnit(time_unit);
            },
            changeTimeUnit: function (time_unit) {
                this.$('.tabs div').removeClass('active');
                this.$('.tabs div[data-value=' + time_unit + ']').addClass('active');
                this.time_unit = time_unit;
                this.refresh();
            },
            clickStats: function (e) {
                if ($(e.currentTarget).hasClass('disabled'))return;
                var stats = $(e.currentTarget).attr('data-value');
                this.changeStats(stats);
            },
            changeStats: function (stats) {
                this.$('.buttons.stats>.button').removeClass('active');
                this.$('.buttons.stats>.button[data-value=' + stats + ']').addClass('active');
                this.stats = stats;
                this.stats_unit = this.$('.buttons.stats>.button[data-value=' + stats + ']').attr('data-unit');
                this.renderComponent();
            },
            showLoading: function () {
                this.$('.view-placeholder').addClass('show');
                this.$('.view-placeholder>.no-data-view').hide();
                this.$('.view-placeholder>.loading-view').show();
            },
            hideLoading: function () {
                this.$('.view-placeholder').removeClass('show');
                this.$('svg').show();
                this.$('.view-placeholder>.loading-view').hide();
            },
            showNoData: function () {
                this.$('.view-placeholder').addClass('show');
                this.$('svg').hide();
                this.$('.view-placeholder>.no-data-view').show();
            },
            hideNoData: function () {
                this.$('.view-placeholder').removeClass('show');
                this.$('.view-placeholder>.no-data-view').hide();
            },
            updateSeries:function(chart,data){
                var self = this;
                if (chart) {
                    _.forEach(data.stats,function(item,index){
                       if(chart.series[index]&&item.data){
                           chart.series[index].setData(item.data,false);
                       }else{
                           item.showInLegend = self.options.showLegend || (data.stats.length > 1);
                           var series = chart.addSeries(item,false);
                           series.series_type = self.options.storeName;
                       }
                    });
                }
            },
            clearSeries: function (chart) {
                if (chart&&chart.series.length != 0) {
                    do {
                        chart.series[0].remove();
                    }
                    while (chart.series.length > 0);
                }
            },
            addSeries: function (end_moment) {
                var self = this;
                var stats = this.options.stats;
                var storeName = this.options.storeName;
                var chart = self.$('.chart-content').highcharts();
                this.showLoading();
                AppCube.DataRepository.fetchNew(storeName, {
                    end_date: end_moment.clone(),
                    is_compared: true
                }).done(function (res) {
                    self.hideLoading();
                    var data;
                    var stateFormatter = self.options.stateFormatter;
                    if(chart)chart.symbolCounter = 0;
                    if(typeof stateFormatter=='function'){
                        data = stateFormatter(res,stats);
                    }else{
                        var tmp = [];
                        for(var index in res.stats){
                            tmp.push(res.stats[index][stats]);
                        }
                        data = {
                            dates:res.dates,
                            is_compared:res.is_compared,
                            stats:[{
                                data:tmp,
                                name:res.chart_name||'name'
                            }]
                        };
                    }
                    //todo merge 2 axis
                    if (data.is_compared && data.stats.length) {
                        self.hideNoData();
                        if (chart.series && chart.series[0]) {
                            chart.series[0].options.showInLegend = true;
                        }
                        var series = chart.addSeries(data.stats[0]);
                        series.series_type = storeName;
                    } else {
                        self.showNoData();
                        console.log('No compared data');
                    }
                });
            },
            renderChart: function (data) {
                var self = this;
                var chart = this.$('.chart-content').highcharts();
                if(!chart)return;
                this.hideLoading();
                if (!data.dates || data.dates.length == 0||_.values(data.dates).length==0) {
                    chart.options.legend.enabled = true;
                    this.clearSeries(chart);
                    this.showNoData();
                    return;
                } else {
                    this.hideNoData();
                    this.clearSeries(chart);
                    //calculate interval
                    var length = _.isArray(data.dates)?data.dates.length:_.values(data.dates).length;
                    var maxlength = this.options.maxlength || 15;
                    var tick = 1;
                    if (length > maxlength) {
                        tick = Math.floor(length / maxlength);
                    }
                    //calculate date
                    chart.xAxis[0].setCategories(U.formatAxis(data.dates),false);
                    chart.xAxis[0].update({tickInterval: tick},false);
                    this.updateSeries(chart,data);
                }
                this.resizeChartHandler();
            },
            resizeChart:function(){
                var self = this;
                if (this.onInterval) {
                    clearTimeout(this.onInterval);
                }
                this.onInterval = setTimeout(function () {
                    self.resizeChartHandler();
                }, 200);
            },
            resizeChartHandler:function(){
                var chart = this.$('.chart-content').highcharts();
                if(chart)chart.setSize(this.$('.chart-content').width(), 300);
            },
            renderComponent: function () {
                var self = this;
                var storeName = this.options.storeName;
                var stateName = this.options.stateName;
                var chart = this.$('.chart-content').highcharts();
                this.showLoading();
                if(chart)chart.colorCounter = 0;
                if(chart)chart.symbolCounter = 0;
                AppCube.DataRepository.fetch(storeName, stateName, this.stats).done(function (res) {
                    self.renderChart(res);
                });
            },
            getValue: function () {
                return this.time_unit;
            },
            render: function () {
                Marionette.ItemView.prototype.render.call(this);
                this.$el.i18n();
                this.$('.chart-content').highcharts(this.options.options);
                this.initChart();
                this.refresh();
            },
            refresh: function () {
                var storeName = this.options.storeName;
                this.showLoading();
                Dispatcher.trigger('startRefresh:'+storeName,{},'Component');
                AppCube.DataRepository.refresh(storeName);
            }
        });
    });