define(
    [
        'app',
        'component/highchart/BasicChart/view',
        'U',
        'dispatcher',
        'text!./template.html',
        'jquery',
        'underscore',
        'i18n'
    ],
    function (AppCube, BasicChart, U, Dispatcher,template, $, _, i18n) {

        return BasicChart.extend({
            template:template,
            beforeShow:function(){
                BasicChart.prototype.beforeShow.call(this);
                Dispatcher.on('Click:Filter',this.refresh,this,'Component');
            },
            beforeHide:function(){
                Dispatcher.off('Click:Filter','Component');
                BasicChart.prototype.beforeHide.call(this);
            },
            initChart: function () {
                var title = this.options.doI18n?i18n.t(this.options.title):this.options.title;
                this.$('.caption').html(title);
                this.ranges = this.options.ranges;
                this.stats = this.options.stats;
            },
            resizeChartHandler:function(){
                var chart = this.$('.chart-content').highcharts();
                if(chart)chart.setSize(this.$('.chart-content').width()-1, 200);
            },
            setRate:function(rate){
                if($.isNumeric(rate)){
                    var percent = (rate*100).toFixed(2);
                    if(percent!="0.00"){
                        var tag = rate>0?'<i class="fa fa-caret-up"></i>':'<i class="fa fa-caret-down"></i>';
                        this.$(".rate").html(tag+percent+'%');
                        this.$(".rate").removeClass("up down").addClass(rate>0?"up":"down");
                    }else{
                        this.$(".rate").removeClass("up down").html('');
                    }
                }
            },
            setTotal:function(total){
                var number = total+"";
                var money = "";
                while(number.length>3){
                    money = "," + number.slice(number.length-3)+money;
                    number = number.slice(0,number.length-3);
                }
                this.$('.total').html(number + money);
            },
            renderChart: function (data) {
                var chart = this.$('.chart-content').highcharts();
                if(!chart)return;
                if(chart)chart.colorCounter = 0;
                this.hideLoading();
                this.clearSeries(chart);
                this.setTotal(data?data.current||0:0);
                this.setRate((data&&data.previous!=0)?((data.current-data.previous)/data.previous):0);

                if (!data.dates || data.dates.length == 0||_.values(data.dates).length==0) {
                    chart.options.legend.enabled = true;
                    this.clearSeries(chart);
                    this.showNoData();
                    return;
                } else {
                    this.hideNoData();
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
                    if(data.stats&&data.stats[0]&&data.stats[0].data){
                        var total = 0;
                        for(var i in data.stats[0].data){
                            total+=data.stats[0].data[i];
                        }
                        if(total==0){
                            chart.yAxis[0].setExtremes(0, 1, false);
                        }
                    }
                    this.updateSeries(chart,data);
                }
                this.resizeChartHandler();
            },
            render: function () {
                this.$el.addClass('simple-chart');
                BasicChart.prototype.render.call(this);
            }
        });
    });