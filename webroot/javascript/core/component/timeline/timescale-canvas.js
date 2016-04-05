define(['Storage','U','jquery','underscore','moment'],function(Storage,U,$,_,moment){
    var defaults = {
        start_date:null,
        scale_padding:180,
        scale_color:"rgba(221,221,221,1)",
        fontColor:"rgba(119,119,119,1)",
        fontSize:14
    };

    var scaleSpeed = 125;
    var mainScaleLength = 10;
    var subScaleLength = 5;
    var interval = 1000/60;

    var scaleRate = [
        {
            name:"week",
            step:[1,'w'],
            value:1
        },{
            name:"day",
            step:[1,'d'],
            value:7
        },{
            name:"6hour",
            step:[6,'h'],
            value:4
        },{
            name:"hour",
            step:[1,'h'],
            value:6
        },{
            name:"10min",
            step:[10,'m'],
            value:6
        },{
            name:"5min",
            step:[5,'m'],
            value:5
        }
    ];

    function TimeScale(options){
        this.init(options);
    }

    function clearBoard(ctx){
        ctx.clearRect(0,0,ctx.canvas.width,ctx.canvas.height);
        ctx.save();
        ctx.fillStyle = "#FFFFFF";
        ctx.fillRect(0,ctx.canvas.height/2,ctx.canvas.width,ctx.canvas.height/2);
        ctx.restore();
    }

    function drawScaleItem(ctx,xPos,length,Label,fSize,fColor,sColor){
        var yPos = ctx.canvas.height/2;
        ctx.save();
        ctx.translate(55,0);
        ctx.lineWidth = 1;
        ctx.strokeStyle = sColor;
        ctx.beginPath();
        ctx.moveTo(xPos,yPos);
        ctx.lineTo(xPos,yPos - length);
        ctx.closePath();
        ctx.stroke();
        if(Label){
            ctx.fillStyle = fColor;
            ctx.font = fSize + "px Arial";
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            ctx.fillText(Label,xPos,yPos + fSize + 10);
        }
        ctx.restore();
    }


    TimeScale.prototype.init = function(options){
        var params = this.params = _.extend({},defaults,options);
        if(!params.renderTo)throw new Error('no root to render!');
        if(!params.start_date)throw new Error('no time range!');

        this.root = params.renderTo;
        var canvas = $('<canvas></canvas>').appendTo(params.renderTo);
        this.canvas = canvas.get(0);
        this.inTransition = false;
        this.xAxis_pos = 0;
        this.scaleLevel = 1;
        this.offset_xAxis_pos = this.xAxis_pos;

        //display start point, value changed when move end
        this.start_date = this.params.start_date.clone();
        this.end_date = this.params.start_date.clone();

        //backup of start_date, value changed when moving
        this.offset_start_date = this.start_date.clone();
        this.xAxis = [];
        this.ctx = this.canvas.getContext('2d');
        this.pr = U.getPixelRatio(this.ctx);
        if(this.pr<2)this.pr = 1;
        this.resizeCanvas();
        var self = this;
        $(window).bind('resize.timescale',function(){
            self.resizeCanvas();
        });
    };

    TimeScale.prototype.resizeCanvas = function(){
        var width = this.root.width();
        this.canvas.width = width*this.pr;
        this.canvas.height = 100*this.pr;
        this.updateScale();
        this.render();
    };

    TimeScale.prototype.render = function(){
        clearBoard(this.ctx);
        this.sub_xAxis = [];
        this.drawScale();
    };

    TimeScale.prototype.renderTransition = function(scaleRate,progress,smaller){
        clearBoard(this.ctx);
        if(smaller){
            this.drawSubScale(1/scaleRate,1-progress,smaller);
            this.drawScale(1/scaleRate,smaller);
        }else{
            this.drawSubScale(scaleRate,progress,smaller);
            this.drawScale(scaleRate,smaller);
        }
    };

    function addTimeScaleLabel(result,maxLength,date,maxLevel){
        var scaleCurrent = scaleRate[maxLevel];
        var stepCount = scaleCurrent.step[0];
        var stepUnit = scaleCurrent.step[1];
        var start = date.clone();
        for(var i = 0;i < maxLength;i++){
            if(start.hour()==0&&start.minute()==0){
                result.push(start.format('ll'));
            }else{
                result.push(start.format('HH:mm'));
            }
            start = start.add(stepCount,stepUnit);
        }
        return i>0?start.subtract(stepCount,stepUnit):start;
    }

    TimeScale.prototype.updateScale = function(forceUpdate){
        var xSize = Math.floor((this.canvas.width - this.offset_xAxis_pos)/this.params.scale_padding) + 1;
        if(forceUpdate||xSize != this.xAxis.length){
            var date = this.offset_start_date.clone();
            var result = [];
            this.end_date = addTimeScaleLabel(result,xSize,date,this.scaleLevel);
            this.xAxis = result;
        }
    };

    TimeScale.prototype.updateSubScale = function(scaleRate){
        var date = this.offset_start_date.clone();
        var result = [];
        addTimeScaleLabel(result,this.xAxis.length * scaleRate,date,this.scaleLevel);
        this.sub_xAxis = result;
    };

    TimeScale.prototype.drawScale = function(scaleRate,smaller){
        if(!scaleRate)scaleRate = 1;
        var padding = this.params.scale_padding*scaleRate;
        var sub_padding = smaller?padding*this.scaleCount:padding;
        for(var i = 0;i< this.xAxis.length;i++){
            drawScaleItem(
                this.ctx,
                (this.offset_xAxis_pos + i * sub_padding) * this.pr,
                mainScaleLength,
                this.xAxis[i],
                this.params.fontSize * this.pr,
                this.params.fontColor,
                this.params.scale_color
            );
        }
    };

    TimeScale.prototype.drawSubScale = function(scaleRate,progress,smaller){
        if(!scaleRate)scaleRate = 1;
        var padding = this.params.scale_padding*scaleRate;
        var count = this.scaleCount;
        var sub_padding = smaller?padding:padding/count;
        for(var i = 0;i< this.sub_xAxis.length;i++){
            if(i % count == 0)continue;
            drawScaleItem(
                this.ctx,
                this.offset_xAxis_pos + i * sub_padding * this.pr,
                subScaleLength,
                this.sub_xAxis[i],
                this.params.fontSize * this.pr,
                "rgba(119,119,119,"+progress+")",
                this.params.scale_color
            );
        }
    };

    TimeScale.prototype.move = function(origin, now){
        if(this.inTransition)return;
        var delta = now - origin;
        var offset_scale = Math.floor((this.xAxis_pos + delta)/this.params.scale_padding);
        var offset = delta - this.params.scale_padding * offset_scale;
        var scale_step = scaleRate[this.scaleLevel].step;
        this.offset_start_date = this.start_date.clone().subtract(offset_scale*scale_step[0],scale_step[1]);
        this.updateScale(true);
        this.offset_xAxis_pos = this.xAxis_pos + offset;
        this.render();
    };

    TimeScale.prototype.moveEnd = function(){
        if(this.inTransition)return;
        this.xAxis_pos = this.offset_xAxis_pos;
        this.start_date = this.offset_start_date;
    };

    TimeScale.prototype.setStartDate = function(moment){
        if(!moment)return;
        this.start_date = this.offset_start_date = moment.clone();
    };

    TimeScale.prototype.setScale = function(level){
        var newScale = Math.min(Math.max(level,0),scaleRate.length-1);
        if(newScale > this.scaleLevel){
            this.runBiggerTransition(newScale);
        }else if(newScale < this.scaleLevel){
            this.runSmallerTransition(newScale);
        }
    };

    TimeScale.prototype.tick = function(es_time,startRate,endRate,step,smaller){
        var self = this;
        this.transition = window.requestAnimFrame(function(){
            if(Date.now() - es_time>interval){
                if(startRate >= endRate){
                    self.inTransition = false;
                    self.updateScale(true);
                    self.render();
                    return false;
                }else{
                    startRate += step;
                    self.renderTransition(startRate,(startRate-1)/(endRate-1),smaller);
                }
                es_time = Date.now();
            }
            self.tick(es_time,startRate,endRate,step,smaller);
        });
    };

    TimeScale.prototype.runBiggerTransition = function(newScale){
        if(this.inTransition){
            cancelRequestAnimFrame(this.transition);
            this.updateScale(true);
        }
        this.inTransition = true;
        var scale = scaleRate[newScale];
        //if(newScale == 0){
        //    this.offset_start_date = this.start_date.startOf('week');
        //    this.start_date = this.offset_start_date.clone();
        //}else if(newScale == 1||newScale == 2){
        //    this.offset_start_date = this.start_date.startOf('day');
        //    this.start_date = this.offset_start_date.clone();
        //}else{
        //    this.offset_start_date = this.start_date.startOf('hour');
        //    this.start_date = this.offset_start_date.clone();
        //}
        this.scaleLevel = newScale;
        this.scaleCount = parseInt(scale.value);
        this.updateSubScale(scale.value);
        this.tick(Date.now(), 1, scale.value, (scale.value-1)/scaleSpeed*interval);
    };

    TimeScale.prototype.runSmallerTransition = function(newScale){
        if(this.inTransition){
            cancelRequestAnimFrame(this.transition);
            this.updateScale(true);
        }
        this.inTransition = true;
        var scale = scaleRate[this.scaleLevel];
        if(newScale == 0){
            this.offset_start_date = this.start_date.startOf('week');
            this.start_date = this.offset_start_date.clone();
        }else if(newScale == 1||newScale == 2){
            this.offset_start_date = this.start_date.startOf('day');
            this.start_date = this.offset_start_date.clone();
        }else{
            this.offset_start_date = this.start_date.startOf('hour');
            this.start_date = this.offset_start_date.clone();
        }
        this.updateSubScale(scale.value);
        this.scaleLevel = newScale;
        this.scaleCount = parseInt(scale.value);
        if(newScale <=1)this.updateScale(true);
        this.tick(Date.now(), 1, scale.value, (scale.value-1)/scaleSpeed*interval,true);
    };

    TimeScale.prototype.destroy = function(){
        $(window).unbind('resize.timescale');
    };

    return TimeScale;
});