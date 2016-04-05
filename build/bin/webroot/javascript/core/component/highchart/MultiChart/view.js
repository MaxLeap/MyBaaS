define(
    [
        'app',
        'component/highchart/BasicChart/view',
        'U',
        'dispatcher',
        'jquery',
        'underscore',
        'i18n',
        'highcharts'
    ],
    function (AppCube, BasicChart, U, Dispatcher, $, _, i18n) {
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

        return BasicChart.extend({
            events: {
                "click .tabs div": "clickTabs",
                "click .buttons.stats>.button": "clickStats"
            },
            initChart: function () {
                var title = this.options.doI18n?i18n.t(this.options.title):this.options.title;
                this.$('.caption').html(title);
                this.ranges = this.options.ranges;
                if (this.options.stats_list && this.options.stats_list.length > 0) {
                    this.initStats();
                }else{
                    this.stats = this.options.stats;
                }
                if (this.options.time && this.options.time.length > 0) {
                    this.initTime();
                }
            },
            updateSeries:function(chart,data){
                var self = this;
                if (chart) {
                    _.forEach(data.stats,function(item,index){
                        if(chart.series[index]&&item.data){
                            chart.series[index].setData(item.data,false);
                        }else{
                            item.showInLegend = true;
                            var series = chart.addSeries(item,false);
                            series.series_type = self.options.storeName;
                        }
                    });
                }
            },
            renderChart: function (data) {
                var self = this;
                var chart = this.$('.chart-content').highcharts();
                if(!chart)return;
                if(chart)chart.yAxis[0].update({
                    title:{
                        text:this.stats_unit||'count'
                    }
                });
                this.hideLoading();
                if (!data.dates || data.dates.length == 0) {
                    this.showNoData();
                    chart.options.legend.enabled = true;
                    this.clearSeries(chart);
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
            changeStats: function (stats) {
                //todo
                this.$('.buttons.stats>.button').removeClass('active');
                this.$('.buttons.stats>.button[data-value=' + stats + ']').addClass('active');
                this.stats = stats;
                this.stats_unit = this.$('.buttons.stats>.button[data-value=' + stats + ']').attr('data-unit');
                this.refresh();
            },
            getValue: function () {
                //todo
                return {
                    time_unit:this.time_unit,
                    stats:this.stats
                };
            }
        });
    });