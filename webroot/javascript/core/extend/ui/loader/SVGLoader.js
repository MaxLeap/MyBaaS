define([
    'Q',
    'Snap',
    'jquery',
    'underscore'
], function (Q, Snap, $, _) {

    var defaults = {
        speedIn: 500,
        easingIn: 'linear',
        initialPath: 'M 0,0 0,60 80,60 80,0 Z M 80,0 80,60 0,60 0,0 Z',
        openingSteps: 'M 0,0 0,60 80,60 80,0 Z M 40,30 40,30 40,30 40,30 Z',
        closingSteps: ''
    };

    function createSVG(el) {
        return document.createElementNS('http://www.w3.org/2000/svg', el);
    }

    function SVGLoader(el, options) {
        this.el = el;
        this.options = _.extend({}, defaults, options);
        this._init();
    }

    SVGLoader.prototype._init = function () {
        var root = $(this.el).children('#dom-loader');
        if (root.length == 0) {
            $(this.el).append('<div id="dom-loader"></div>');
            this._init();
            return;
        }
        root.html('');
        root.hide();
        this.root = root.get(0);
        var s = createSVG('svg');
        this.root.appendChild(s);
        var svg = Snap(s);
        svg.attr({
            width: '100%',
            height: '100%'
        });
        this.svg = svg;

        var rect = svg.rect(0, 0, '100%', '100%');
        rect.attr({
            fill: '#F0F3F9'
        });
        var mask1 = svg.rect(0, 0, '100%', '100%');
        mask1.attr({
            fill: '#FFFFFF'
        });
        var mask2 = svg.rect(0, 0, '100%', '100%');
        mask2.attr({
            fill: '#000000'
        });
        this.mask = mask2;
        var mask_group = svg.group(mask1, mask2);
        rect.attr({
            mask: mask_group
        });

        this._isAnimating = false;
    };

    SVGLoader.prototype.hide = function () {
        if (this._isAnimating)return false;
        var defer = Q.defer();
        var self = this;
        $(this.root).show();
        this._isAnimating = true;
        var baseHeight = $(window).height() - 50;
        var baseWidth = this.mask.node.width.baseVal.value;
        var animation = Snap.animate(0, 100, function (val) {
            val = val ? (100 - val) : 100;
            self.mask.transform(Snap.matrix().scale(val / 100, val / 100, baseWidth / 2, baseHeight / 2));
        }, 400, function () {
            self._isAnimating = false;
            defer.resolve();
        });
        this.mask.stop().animate(animation);
        return defer.promise;
    };

    SVGLoader.prototype.show = function () {
        if (this._isAnimating)return false;
        var defer = Q.defer();
        var self = this;
        $(this.root).show();
        this._isAnimating = true;
        var baseHeight = $(window).height() - 50;
        var baseWidth = this.mask.node.width.baseVal.value;
        var animation = Snap.animate(0, 100, function (val) {
            val = val ? val : 0;
            self.mask.transform(Snap.matrix().scale(val / 100, val / 100, baseWidth / 2, baseHeight / 2));
        }, 400, function () {
            self._isAnimating = false;
            $(self.root).hide();
            defer.resolve();
        });
        this.mask.stop().animate(animation);
        return defer.promise;
    };

    return SVGLoader;
});