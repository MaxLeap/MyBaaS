define(['U','jquery','underscore'],function(U,$,_){
    var defaults = {
        labelColor:"#57A3F2",
        labelBorderColor:"#57A3F2",
        labelDisabledColor:"#e5e5e5",
        labelHighlightColor:"#4BD083",
        fontsize:14,
        maxLevel:5
    };

    function FlowChart(options){
        this.init(options);
    }

    function preprocess(children){
        var tmp;
        var list = _.chain(children).map(function(item,index){
            var obj = {};
            obj.name = index;
            obj.value = item;
            return obj;
        }).reject(function(item){
            return item.name == 'End Session';
        }).sortBy(function(item){return -item.value;}).value();
        if(list.length<=4){
            list.push({
                name:'End Session',
                value:children['End Session']
            });
            tmp = _.sortBy(list,function(item){return -item.value;});
        }else{
            var total = 0;
            _.forEach(list.slice(4),function(item){
                total+=item.value;
            });
            tmp = list.slice(0,4);
            tmp.push({
                name:'End Session',
                value:children['End Session']
            });
            tmp = _.sortBy(tmp,function(item){return -item.value;});
            tmp.push({
                name:'Others',
                value:total
            });
        }
        return tmp;
    }

    FlowChart.prototype.init = function(options,data){
        var params = this.params = _.extend({},defaults,options);
        if(!params.renderTo)throw new Error('no root to render!');
        var render_root = $('<div class="flowchart-container"></div>');
        $(params.renderTo).append(render_root);
        this.root = render_root;
        var canvas = $('<canvas class="line-canvas" width="900" height="800"></canvas>').appendTo(render_root);
        this.canvas = canvas.get(0);
        this.clickStack = [];
        var ctx = this.canvas.getContext('2d');
        var pr = U.getPixelRatio(ctx);
        this.pr = pr;
        if(pr>1){
            this.canvas.width = 900*pr;
            this.canvas.height = 800*pr;
            this.canvas.style.width = '100%';

        }
    };

    FlowChart.prototype.renderBody = function(data,params){
        for(var i=0;i<this.clickStack.length;i++){
            var name = this.clickStack[i];
            var node = this.root.find('.line:last>.activity[data-name="'+name+'"]');
            if(!node.hasClass('disabled')){
                var data = this.getData(name);
                if(data&&data.out){
                    this.renderChild(node,data.out,params||{});
                }
            }else{
                break;
            }
        }
    };

    FlowChart.prototype.renderFull = function(data,params){
        if(data.length==0){
            throw new Error('data is broken!');
        }
        this.data = data;
        var rootdata = _.find(data,function(item){
            return item.root == true;
        });
        if(!rootdata)return;
//        render full default
//
//        if(this.clickStack.length==0){
//            var count = 0;
//            var current = rootdata;
//            while(count<4){
//                count++;
//                current = this.getMaxData(current);
//                if(current){
//                    this.clickStack.push(current.activity);
//                }else{
//                    break;
//                }
//            }
//        }
        this.setRoot(rootdata,params||{});
        this.renderBody();
        this.bindEvent();
    };
    FlowChart.prototype.setRoot = function(dataline,params){
        if(!dataline)return;
        this.rootName = dataline.activity;
        this.renderRoot(dataline,params);
    };

    FlowChart.prototype.renderRoot = function(dataline,params){
        var rootName = dataline.activity;
        var rootline = $('<div class="root line"></div>').appendTo(this.root);
        var rootNode = $('<div class="activity root" data-name="'+rootName+'" data-value=100 >'+rootName+'</div>').appendTo(rootline);
        var childNode = dataline.out;
        this.renderChild(rootNode,childNode,params||{});
    };

    FlowChart.prototype.renderChild = function(parent,children,params){
        var render_data = preprocess(children);
        var level = this.root.find('.line').length;
        if(level>=defaults.maxLevel){
            return;
        }
        var line = $('<div class="line"></div>').appendTo(this.root);
        var self = this;
        _.forEach(render_data,function(item){
            $('<div class="activity'+(item.name==self.clickStack[level-1]?" highlight":"")+'" data-name="'+item.name+'" data-value="'+item.value+'">'+item.name+'</div>').appendTo(line);
        });
        //var dataline = this.getData(parent.attr('data-value'));
        //var rootPercent = parent.attr('data-percent');
        line.children('.activity').each(function(){
            var percent = $(this).attr('data-value');
            var name = $(this).attr('data-name');
            var value = Math.round(percent*10000)/100;
            //var value = rootPercent*child_value/100;
            if(value<0.1||name=="End Session"||name=="Others"||level>=defaults.maxLevel-1){
                $(this).addClass('disabled');
            }
            //$(this).attr('data-value',value);
            var display_value = Math.round(value*100)/100+'%';
            var highlight = self.clickStack[level-1]==name;
            self.drawLine(parent.get(0),this,display_value,params||{},highlight);
        });
    };

    FlowChart.prototype.clearChild = function(root){
        var line = root.parent();
        line.nextAll().remove();
        var c = this.canvas;
        var b = c.getContext("2d");
        var node = root.get(0);
        b.clearRect(0,node.offsetTop+node.offsetHeight/2, c.width, c.height);
    };

    FlowChart.prototype.clearAll = function(bool){
        var root = this.root;
        if(!bool)this.clickStack = [];
        root.find('.line').remove();
        var c = this.canvas;
        var b = c.getContext("2d");
        b.clearRect(0, 0, c.width, c.height);
    };

    FlowChart.prototype.clearBody = function(){
        var root = this.root;
        root.find('.line').not('.root').remove();
        var c = this.canvas;
        var b = c.getContext("2d");
        b.clearRect(0, 0, c.width, c.height);
    };

    FlowChart.prototype.drawLine = function(startNode,endNode,data,params,highlight){
        var c = this.canvas;
        var start = {
            x:startNode.offsetLeft+startNode.offsetWidth/2,
            y:startNode.offsetTop+startNode.offsetHeight
        };
        var end = {
            x:endNode.offsetLeft+endNode.offsetWidth/2,
            y:endNode.offsetTop
        };
        var color;
        if($(endNode).hasClass('disabled')){
            color = params.labelDisabledColor||defaults.labelDisabledColor;
        }else if(highlight){
            color = params.labelHighlightColor||defaults.labelHighlightColor;
        }else{
            color = params.labelColor||defaults.labelColor;
        }
        var b = c.getContext("2d");
        var pr = this.pr;
        b.save();
        b.lineWidth = pr;
        b.strokeStyle = color;
        b.beginPath();
        b.moveTo(start.x*pr,start.y*pr);
        b.lineTo(end.x*pr,end.y*pr);
        b.closePath();
        b.stroke();
        b.restore();
        b.fillStyle = color;
        b.beginPath();
        b.rect(((start.x+end.x)/2-27)*pr,((end.y+start.y)/2-11)*pr,54*pr,22*pr);
        b.closePath();
        b.fill();
        b.restore();
        b.textAlign = "center";
        b.fillStyle = $(endNode).hasClass('disabled')?"#777":"#ffffff";
        b.font=12*pr + "px Arial";
        b.fillText(data,(start.x+end.x)/2*pr,((end.y+start.y)/2+4)*pr);
        b.restore();
    };

    FlowChart.prototype.getMaxData = function(data){
        if(!data.out){
            return false;
        }
        var activity = '';
        var max = 0;
        _.forEach(data.out,function(item,index){
            if(item>max){
                max = item;
                activity = index;
            }
        });
        return this.getData(activity);
    };

    FlowChart.prototype.getData = function(name){
        var dataline = _.find(this.data,function(item){
            return item.activity == name;
        });
        return dataline?dataline:false;
    };

    FlowChart.prototype.bindEvent = function(){
        var self = this;
        this.root.off('click.activity').on('click.activity','.activity',function(){
            var root = $(this);
            if($(this).hasClass('disabled'))return;
            var level = $(this).parent().index()-1;
            var length = self.clickStack.length;
            var count = length-level+1;
            while(count>0){
                count--;
                self.clickStack.pop();
            }
            if(!$(this).hasClass('root')){
                var name = $(this).attr('data-name');
                self.clickStack.push(name);
            }
            self.clearAll(true);
            self.renderRoot(self.getData(self.rootName));
            self.renderBody();
        });
    };

    return FlowChart;
});