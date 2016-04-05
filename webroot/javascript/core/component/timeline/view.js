define(
    [
        'app',
        'Storage',
        './timescale-canvas',
        'marionette',
        'tpl!./flag_template.html',
        'tpl!./template.html',
        'moment',
        'jquery',
        'underscore',
        'i18n'
    ],
    function(AppCube, Storage, TimeScale, Marionette, flag_template, template, moment, $, _, i18n) {

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
                name:"1min",
                step:[5,'m'],
                value:5
            }
        ];

        var scale_padding = 180;

        return Marionette.ItemView.extend({
            template: template,
            events:{
                "click .timeline-item .link":"scaleDetail",
                "click .timeline-modifier-plus":"scaleBigger",
                "click .timeline-modifier-minus":"scaleSmaller"
            },
            beforeShow:function(){
                moment.locale(Storage.get('moment'));
            },
            beforeHide:function(){
                if(this.timescale){
                    this.timescale.destroy();
                    this.timescale = null;
                }
                AppCube.DataRepository.getStore("Store:TimeLine").clear();
                this.$('.timeline-slider').unbind('.timeline');
            },
            initTimeline:function(){
                this.moveOffset = 0;
                this.offsetIndex = 0;
                var start = moment().startOf('week');
                this.page = {
                    start:start,
                    end:start.clone().add(12,'w')
                };
                this.originStart = start.clone();
                this.dirtyPage = [];
                this.initTimeScale();
                this.initDragEvent();
                this.setScale(1);
            },
            initTimeScale:function(){
                this.timescale = new TimeScale({
                    scale_padding:scale_padding,
                    renderTo:this.$('.timeline-scale'),
                    start_date:this.page.start
                });
                this.timescale.render();
            },
            initDragEvent:function(){
                this.isOnDrag = false;
                this.$('.timeline-slider').unbind('.timeline');
                this.$('.timeline-slider').bind('mousedown.timeline',this.OnDragStart.bind(this));
                this.$('.timeline-slider').bind('mousemove.timeline',this.OnDragMove.bind(this));
                this.$('.timeline-slider').bind('mouseup.timeline',this.OnDragEnd.bind(this));
                this.$('.timeline-slider').bind('mouseleave.timeline',this.OnDragEnd.bind(this));
            },
            OnDragStart:function(e){
                if(this.isOnDrag||e.target!= e.currentTarget) return;
                this.originX = e.offsetX;
                this.originY = e.offsetY;
                this.isOnDrag = true;
            },
            OnDragMove:function(e){
                if(!this.isOnDrag) return;
                var offset = e.offsetX;
                if(e.target != e.currentTarget){
                    offset += e.target.getBoundingClientRect().left - e.currentTarget.getBoundingClientRect().left;
                }
                this.timescale.move(this.originX, offset);
                this.lastmoveOffset = this.moveOffset + offset - this.originX;
                this.$('.timeline-list').css("left", this.lastmoveOffset);

                //check page
                var end_date = this.timescale.end_date;
                var start_date = this.timescale.offset_start_date;
                if(end_date.diff(moment(this.dirtyPage[this.dirtyPage.length-1],'YYMMDD').add(12,'w'))>=0){
                    this.nextPage();
                }
                if(start_date.diff(moment(this.dirtyPage[0],'YYMMDD'))<0){
                    this.previousPage();
                }
            },
            OnDragEnd:function(e){
                if(!this.isOnDrag) return;
                if(e.target != e.currentTarget&&e.type != "mouseup")return;
                this.moveOffset = this.lastmoveOffset;
                this.originX = e.offsetX;
                this.isOnDrag = false;
                this.timescale.moveEnd();
                this.$('.timeline-list').css("left", this.moveOffset);
            },
            addCurrentPage:function(insert){
                var page = this.page.start.format('YYMMDD');
                if(!_.contains(this.dirtyPage,page)){
                    if(insert){
                        this.dirtyPage.splice(0,0,page);
                    }else{
                        this.dirtyPage.push(page);
                        this.dirtyPage.sort();
                    }
                    return true;
                }
                return false;
            },
            scaleDetail:function(e){
                var node = $(e.currentTarget).parents('.timeline-item');
                if(!node.hasClass('multi'))return;
                var page = node.parent().attr('data-page');
                var data = this.current_page[page][node.attr('data-value')];
                var e1 = moment(data.event[0].timestamp);
                var e2 = moment(data.event[1].timestamp);
                for(var i = this.scale+1;i<scaleRate.length-1;i++){
                    var step = scaleRate[i].step;
                    if(e2.diff(e1,step[1])>=step[0]){
                        break;
                    }
                }
                this.scale = i;
                this.timescale.setStartDate(e1);
                this.setScale();
                this.refresh();
                this.updateMoveOffset();
                this.updateOffsetIndex();
            },
            scaleBigger:function(){
                if(this.scale < scaleRate.length-1){
                    this.scale++;
                    this.setScale();
                    this.refresh();
                    this.updateMoveOffset();
                    this.updateOffsetIndex();
                }
            },
            scaleSmaller:function(){
                if(this.scale > 0){
                    this.scale--;
                    this.setScale();
                    this.refresh();
                    this.updateMoveOffset();
                    this.updateOffsetIndex();
                }
            },
            setScale:function(index){
                if(typeof index == "number" && index>=0 && index<scaleRate.length){
                    this.scale = index;
                }
                var scale = scaleRate[this.scale];
                //clear all item when scale
                this.$('.timeline-list').html('');
                this.dirtyPage = [];
                this.current_page = {};
                this.addCurrentPage();

                this.$('.timeline-modifier-plus').toggleClass('disabled',this.scale == scaleRate.length-1);
                this.$('.timeline-modifier-minus').toggleClass('disabled',this.scale == 0);
                this.$('.timeline-modifier-label').text(scale.name).css('bottom',(17*this.scale + 16)+'px');
                this.$('.timeline-modifier-bar').css('height',17*this.scale+'px');
                this.timescale.setScale(this.scale);
                this.originStart = this.page.start.clone();
            },
            updateMoveOffset:function(){
                this.moveOffset = this.timescale.xAxis_pos;
                this.$('.timeline-list').css("left", this.moveOffset);
            },
            updateOffsetIndex:function(){
                var step = scaleRate[this.scale].step;
                this.offsetIndex = this.timescale.start_date.diff(this.page.start,step[1])/step[0];
            },
            getDeltaOffset:function(page){
                var result = 1,i = 0;
                while(i<this.scale){
                    result*=scaleRate[i+1].value;
                    i++;
                }
                var page_count = moment(page,'YYMMDD').diff(this.originStart,'w');
                return page_count * result * scale_padding;
            },
            renderEventToList:function(data,page){
                var self = this;
                this.current_page[page] = data;
                //console.log('render page:' + page);
                var offset = this.getDeltaOffset(page);
                var root = $('<div class="page" data-page="'+page+'" style="left:'+offset+'px"></div>').appendTo(this.$('.timeline-list'));
                _.forEach(data,function(item,index){
                    if(item)self.drawFlag(item,index,root);
                });
            },
            drawFlag:function(data,index,root){
                if(data.count==0)return;
                var flag = flag_template({
                    event:data.event[0],
                    count:data.count,
                    id:index,
                    offset:(index-this.offsetIndex)*scale_padding+scale_padding/2
                });
                $(flag).appendTo(root).fadeIn(500);
            },
            getScaleLevel:function(){
                return scaleRate[this.scale];
            },
            getTimeRange:function(){
                return {
                    start_date:this.page.start,
                    end_date:this.page.end
                }
            },
            getValue:function(){
                var range = this.getTimeRange();
                return {
                    scale:this.getScaleLevel(),
                    start_date:range.start_date.format("YYYYMMDDHH"),
                    end_date:range.end_date.format("YYYYMMDDHH")
                }
            },
            nextPage:function(){
                this.page.start = this.page.end;
                this.page.end = this.page.start.clone().add(12,'w');
                if(this.addCurrentPage()){
                    this.refresh();
                }
                if(this.dirtyPage.length>3){
                    var oldPage = this.dirtyPage.slice(0,this.dirtyPage.length-3);
                    var self = this;
                    _.forEach(oldPage,function(page){
                        self.$('.timeline-list>.page[data-page='+page+']').remove();
                        self.current_page[page] = null;
                    });
                    this.dirtyPage.splice(0,this.dirtyPage.length-3);
                }
            },
            previousPage:function(){
                this.page.end = this.page.start;
                this.page.start = this.page.end.clone().subtract(12,'w');
                if(this.addCurrentPage()){
                    this.refresh();
                }
                if(this.dirtyPage.length>3){
                    var oldPage = this.dirtyPage.slice(3);
                    var self = this;
                    _.forEach(oldPage,function(page){
                        self.$('.timeline-list>.page[data-page='+page+']').remove();
                        self.current_page[page] = null;
                    });
                    this.dirtyPage.splice(3,this.dirtyPage.length-3);
                }
            },
            render: function(options) {
                Marionette.ItemView.prototype.render.call(this);
                this.$el.i18n();
                this.initTimeline();
            },
            refresh:function(){
                AppCube.DataRepository.refresh("Store:TimeLine",this.getValue());
            }
        });
    });
