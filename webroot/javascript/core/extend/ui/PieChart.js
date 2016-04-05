define(['jquery','underscore','d3','i18n'],function($,_,d3,i18n){
    var defaults = {
        innerRadius:40,
        outerRadius:50,
        data:[],
        text:['notification.tag.fail','notification.tag.success','notification.tag.scheduled'],
        fontsize:14
    };

    function PieChart(options){
        this.render(options);
    }

    PieChart.prototype.render = function(options){
        var params = this.params = _.extend({},defaults,options);
        var height = params.outerRadius*2;
        var render_root = d3.select(params.renderTo);
        this.root = render_root
            .append('svg')
            .attr('width',height+150)
            .attr('height',height);

        var svg =this.root;
        var dataset = params.data;
        var pie = d3.layout.pie();
        var color = params.color||d3.scale.category10();

        var arc = d3.svg.arc()
            .innerRadius(params.innerRadius)
            .outerRadius(params.outerRadius);
        var graph = svg.append("g")
            .attr("class","pie-graph");
        var info = svg.append("g")
            .attr("class","pie-desc");
        var percent = Math.round((dataset[1])/(dataset[0]+dataset[1]+dataset[2])*10000)/100;

        graph.append("text")
            .attr("x",50)
            .attr("y",56)
            .text(percent+"%")
            .attr('text-anchor','middle')
            .attr('font-family','sans-serif')
            .attr('font-size','18px');


        var arcs = graph.selectAll("g")
            .data(pie(dataset))
            .enter()
            .append("g")
            .attr("transform","translate("+params.outerRadius+","+params.outerRadius+")");

        arcs.append("path")
            .attr("fill",function(d,i){
                return color[i]||color(i);
            })
            .attr("d",function(d){
                return arc(d);
            });

        var desc = info.selectAll("g")
            .data(pie(dataset))
            .enter()
            .append("g");

        desc.append("rect")
            .attr("width",10).attr("height",10)
            .attr("x",120)
            .attr("y",function(d,i){
                return 15+30*i;
            })
            .attr("fill",function(d,i){
                return color[i]||color(i);
            });

        desc.append("text")
            .attr("x",140)
            .attr("y",function(d,i){
                return 15+30*i+(params.fontsize+10)/2;
            })
            .text(function(d,i){return d.data+' '+i18n.t(params.text[i]);})
            .attr('text-anchor','start')
            .attr('font-family','sans-serif')
            .attr('font-size',params.fontsize+'px');
    };

    return PieChart;
});