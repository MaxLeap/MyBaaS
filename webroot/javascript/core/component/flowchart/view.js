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
        'extend/ui/FlowChart',
        'i18n'
    ],
    function (AppCube, U, Dispatcher, template, Marionette, $, _, moment, FlowChart,i18n) {
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
            init: function () {
            },
            beforeShow: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.on('change:Time', this.refresh, this, 'Component');
                Dispatcher.on('Request.getValue:' + eventName, this.getValue, this, 'Component');

                var storeName = this.options.storeName;
                Dispatcher.on('refresh:' + storeName, this.renderComponent, this, 'Component');
            },
            beforeHide: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.off('Request.getValue:' + eventName, 'Component');
                Dispatcher.off('change:Time', 'Component');
                var storeName = this.options.storeName;
                Dispatcher.off('refresh:' + storeName, 'Component');
                var chart = this.chart;
                if (chart){
                    chart.clearAll();
                    this.chart = null;
                }
            },
            initChart: function () {
                var title = this.options.doI18n?i18n.t(this.options.title):this.options.title;
                this.$('.caption').html(title);
            },
            showLoading: function () {
                this.$('.view-placeholder').addClass('show');
                this.$('.view-placeholder>.loading-view').show();
            },
            hideLoading: function () {
                this.$('.view-placeholder').removeClass('show');
                this.$('canvas').show();
                this.$('.view-placeholder>.loading-view').hide();
            },
            showNoData: function () {
                this.$('.view-placeholder').addClass('show');
                this.$('canvas').hide();
                this.$('.view-placeholder>.no-data-view').show();
            },
            hideNoData: function () {
                this.$('.view-placeholder').removeClass('show');
                this.$('.view-placeholder>.no-data-view').hide();
            },
            renderChart: function (data) {
                this.hideLoading();
                if(data.length==0){
                    this.showNoData();
                    this.chart.clearAll();
                }else{
                    this.hideNoData();
                    this.chart.renderFull(data);
                }
            },
            renderComponent: function () {
                var self = this;
                var storeName = this.options.storeName;
                var stateName = this.options.stateName;
                AppCube.DataRepository.fetch(storeName, stateName, this.stats).done(function (res){
                    self.renderChart(res.stats);
                });
            },
            getValue: function () {
                return this.time_unit;
            },
            render: function () {
                Marionette.ItemView.prototype.render.call(this);
                this.$el.i18n();
                var chartOptions = this.options.flow;
                chartOptions.renderTo = this.$('.chart-content');
                this.chart = new FlowChart(chartOptions);
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