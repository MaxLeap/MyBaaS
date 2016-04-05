define([
    'jquery',
    'underscore',
    'moment',
    'i18n'
],function($,_,moment,i18n){
    var defaults = {
        colors:[
            "#fcfbfa",
            "#f7f5f2",
            "#f4f2ee",
            "#eff3f5",
            "#e3eef9",
            "#ebf5ff",
            "#d9edff",
            "#c8e5ff"
        ],
        grid:{
            width:60,
            height:40,
            margin:5
        },
        date:{
            width:80
        },
        label:{
            formatter:function(d,ctx){
                return d+1;
            }
        },
        showCount:true,
        labelText:"analytics.tag.count",
        ranges:8,
        renderTo:"body",
        metadata:{
            rate:"retention_rate",
            date:"install_period",
            count:"total_install"
        },
        data:[]
    };

    var serverDateFormat = 'YYYYMMDD';

    function getRange(data){
        var max = 0;
        var min = 100;
        for(var i in data){
            var d = data[i].retention_rate;
            for(var j in d){
                if(d[j]>max)max = d[j];
                if(d[j]<min)min = d[j];
            }
        }
        return [min,max];
    }

    function checkRange(value,range,n){
        var n = n||8;
        var min = range[0];
        var max = range[1];
        var length = (max-min)/n;
        if(length==0)return 0;
        for(var i=0;i<=8;i++){
            min+=length;
            if(min>value){
                if(i>=7)i=7;
                return i;
            }
        }
    }

    function RetentionChart(options){
        this.initialize(options);
        this.render();
    }

    RetentionChart.prototype.initialize = function(options){
        var params = this.params = _.extend({},defaults,options);
        //create root
        this.root = $(params.renderTo);
        if(this.root.length==0)throw new Error('root "'+params.renderTo+'" does not exist!');
        //create container
        this.root.html('<div class="info_bar"></div><div class="data_content"></div>');
    };

    RetentionChart.prototype.render = function(){
        var params = this.params;
        this.range = getRange(params.data);
        this.clear();
        this.drawTitle();
        this.drawLabel();
        this.drawDate();
        this.drawRetentionRate();
    };

    RetentionChart.prototype.setData = function(data){
        delete this.params.data;
        this.params.data = data;
    };

    RetentionChart.prototype.setRange = function(range){
        this.rangeType = range;
    };

    RetentionChart.prototype.destroy = function(){
        this.root.remove();
        delete this;
    };

    RetentionChart.prototype.clear = function(){
        this.root.find('.info_bar').html('');
        this.root.find('.data_content').html('');
    };

    RetentionChart.prototype.drawTitle = function(){
        var conetnt = this.root.find('.info_bar');
        var params = this.params;
        var width = params.grid.width||60;
        var height = params.grid.height||40;
        var date_width = params.date.width||80;
        var margin = params.grid.margin||0;
        var header = $('<div class="retention_header"></div>').appendTo(conetnt);
        var row = $('<div class="retention_row"></div>').appendTo(header);
        if(!params.data||params.data.length==0)return;
        $('<div class="retention_grid" style="width:'+date_width+'px;height:'+height+'px;margin:0 '+margin+'px '+margin+'px 0;">'+i18n.t('analytics.tag.date')+'</div>').appendTo(row);
        if(params.showCount){
            $('<div class="retention_grid" style="width:'+width+'px;height:'+height+'px;margin:0 '+margin+'px '+margin+'px 0;">'+i18n.t(params.labelText)+'</div>').appendTo(row);
        }
    };

    RetentionChart.prototype.drawLabel = function(){
        var params = this.params;
        var length = params.data.length>1?(params.data[0].retention_rate.length||0):0;
        var width = params.grid.width||60;
        var height = params.grid.height||40;
        var margin = params.grid.margin||0;
        var content = this.root.find('.data_content');
        var header = $('<div class="retention_header"></div>').appendTo(content);
        header.css('width',length*(width+margin));
        var row = $('<div class="retention_row"></div>').appendTo(header);

        for(var i=0;i<length;i++){
            var label = params.label.formatter?params.label.formatter(i,this):i;
            row.append('<div class="retention_grid" style="width:'+width+'px;height:'+height+'px;margin:0 '+margin+'px '+margin+'px 0;">'+label+'</div>');
        }
    };

    RetentionChart.prototype.drawDate = function(){
        var conetnt = this.root.find('.info_bar');
        var params = this.params;
        var width = params.grid.width||60;
        var date_width = params.date.width||80;
        var height = params.grid.height||40;
        var margin = params.grid.margin||0;
        var body = $('<div class="retention_body"></div>').appendTo(conetnt);
        var date_text = params.metadata.date;
        for(var i in params.data){
            var item = params.data[i];
            var row = $('<div class="retention_row"></div>').appendTo(body);
            var date = params.date.formatter?params.date.formatter(item[date_text]):moment(item[date_text],serverDateFormat).format('ll');
            row.append('<div class="retention_grid" style="width:'+date_width+'px;height:'+height+'px;margin:0 '+margin+'px '+margin+'px 0;">'+date+'</div>');
            if(params.showCount){
                var count_text = params.metadata.count;
                row.append('<div class="retention_grid" style="width:'+width+'px;height:'+height+'px;margin:0 '+margin+'px '+margin+'px 0;">'+item[count_text]+'</div>');
            }
        }
    };

    RetentionChart.prototype.drawRetentionRate = function(){
        var params = this.params;
        var content = this.root.find('.data_content');
        var rate_text = params.metadata.rate;
        var date_text = params.metadata.date;
        var body = $('<div class="retention_body"></div>').appendTo(content);
        var length = params.data.length>1?(params.data[0].retention_rate.length||0):0;
        var width = params.grid.width||60;
        var height = params.grid.height||40;
        var margin = params.grid.margin||0;
        body.css('width',length*(width+margin));
        var range = this.range;
        for(var i in params.data){
            var retention_rate = params.data[i][rate_text];
            var row = $('<div class="retention_row"></div>').appendTo(body);
            for(var j in retention_rate){
                var value = retention_rate[j];
                var color = params.colors[checkRange(value,range)];
                row.append('<div class="retention_grid" data-index="'+(parseInt(j)+1)+'" data-date="'+params.data[i][date_text]+'" data-value="'+value+'" style="width:'+width+'px;height:'+height+'px;margin:0 '+margin+'px '+margin+'px 0;"><div class="content" style="background-color:'+color+';">'+value+'%</div></div>');
            }
        }
    };

    return RetentionChart;
});