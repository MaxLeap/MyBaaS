define([
    'container/BasicContainer',
    'text!./CascadeContainer.html',
    'core/functions/ComponentFactory',
    'jquery',
    'underscore'
], function (BasicContainer, template, ComponentFactory, $, _) {
    var CascadeContainer = BasicContainer.extend({
        events: {
            "click .btn-prev": "clickPrev",
            "click .btn-next": "clickNext"
        },
        clickPrev: function () {
            if (this._currentindex > 0) {
                this._currentindex--;
                var root = this._containerRoot;
                var li = root.find('.form-bootstrapWizard>li');
                li.removeClass('active');
                li.eq(this._currentindex).addClass('active');
                root.find('.form-pane');
                var pane = root.find('.form-pane');
                pane.hide();
                pane.eq(this._currentindex).show();
            }
        },
        clickNext: function (state) {
            if (this._currentindex <= this._renderindex - 1) {
                this._currentindex++;
                var root = this._containerRoot;
                var li = root.find('.form-bootstrapWizard>li');
                li.removeClass('active');
                li.eq(this._currentindex - 1).addClass(state || 'success');
                li.eq(this._currentindex).addClass('active');
                var pane = root.find('.form-pane');
                pane.hide();
                pane.eq(this._currentindex).show();
            }
        },
        addComponent: function (options) {
            this._components.push({
                name: options.name,
                description: options.description,
                constructor: options.constructor,
                factory: options.factory || ComponentFactory,
                options: options.options,
                extend: options.extend
            });
        },
        getContainer: function (index, options) {
            var node = '<div id="' + index + '" class="form-pane" ' + (this._renderindex == 0 ? '' : 'style="display:none"') + ' data-value="step' + (this._renderindex + 1) + '" ></div>';
            return $(node);
        },
        addStep: function (name, index, text) {
            var root = this._containerRoot;
            root.find('.form-bootstrapWizard').append('<li ' + (index == 1 ? 'class="active"' : '') + ' data-target="step1">' +
            '<span class="step">' + index + '</span>' +
            '<span class="title">' + name + '</span>' +
            (text ? '<span class="text">' + text + '</span>' : '') +
            '</li>');
        },
        renderComponents: function (options) {
            this._currentindex = 0;
            this._renderindex = 0;
            var self = this;
            var containerRoot;
            if (this.options.root) {
                var tmp = this._containerRoot.find(this.options.root);
                containerRoot = tmp.length ? tmp : $(this._containerRoot.get(0));
            } else {
                containerRoot = $(this._containerRoot.get(0));
            }
            _.forEach(this._components, function (item) {
                var name = item.name;
                if (typeof self._componentStack[name] == 'undefined') {
                    self._componentStack[name] = self.createComponent(item, options || {});
                }
                var component = self._componentStack[name];
                var el = self.getContainer(name, options || {}).appendTo(containerRoot).get(0);

                component.setElement(el);
                component.beforeShow();
                component.render(options || {});
                self.addStep(name, self._renderindex + 1, item.description);
                self._renderindex++;
            });
            this.addStep('Complete', this._renderindex + 1);
            containerRoot.append('' +
            '<div id="Complete" class="form-pane" style="display:none" data-value="step' + (this._renderindex + 1) + '"><br/>' +
            '<h1 class="text-center text-success"><strong><i class="fa fa-check fa-lg"></i> Complete</strong></h1>' +
            '<h4 class="text-center">Click next to finish</h4>' +
            '<br/><br/></div>');
        }
    });

    CascadeContainer.create = function (options) {
        var ret = new CascadeContainer();
        options.template = template;
        options.root = '.form-content';
        if (ret.initialize(options) == false) {
            return false;
        }
        return ret;
    };

    return CascadeContainer;
});