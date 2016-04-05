define(
    [
        'app',
        'U',
        'dispatcher',
        'tpl!./template.html',
        'component/table/PageTable/view',
        'jquery',
        'underscore',
        'extend/ui/AdvancedTable',
        'marionette'
    ],
    function (AppCube, U, Dispatcher, template, PageTable, $, _, AdvancedTable, Marionette) {

        return PageTable.extend({
            template: template,
            events: {
                'click .pagination .menu>.item': 'changePerpage',
                'click .page-btn>.button': 'changePage',
                'click th.sortable': 'changeSort',
                'click .open-subtable': 'openSub',
                'click .close-subtable': 'closeSub'
            },
            init: function () {
            },
            beforeShow: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.on('Request.getValue:' + eventName, this.getValue, this, 'Component');
                var storeName = this.options.storeName;
                Dispatcher.on('refresh:' + storeName, this.refreshHandler, this, 'Component');
                var subStoreName = this.options.subStoreName;
                Dispatcher.on('refresh:' + subStoreName, this.renderSubComponent, this, 'Component');
            },
            beforeHide: function () {
                var eventName = this.options.valueEventName;
                Dispatcher.off('Request.getValue:' + eventName, 'Component');
                var subStoreName = this.options.subStoreName;
                Dispatcher.off('refresh:' + subStoreName, 'Component');
                var storeName = this.options.storeName;
                Dispatcher.off('refresh:' + storeName, 'Component');
                if(this.table)this.table.destroy();
            },
            getValue: function () {
                var limit = this.perpage + 1;
                var skip = (this.page - 1) * (this.perpage);
                var order = this.order;
                return {
                    limit: limit,
                    skip: skip,
                    order: order
                };
            },
            showNoData: function () {
                this.$('.no-data-view').show();
                this.$('.advanced-table').hide();
                this.$('.pagination').hide();
            },
            hideNoData: function () {
                this.$('.no-data-view').hide();
                this.$('.advanced-table').show();
                this.$('.pagination').show();
            },
            renderSubGrid: function (data) {
                var tmp, end, next;
                //if(!data||data.length==0)this.showNoData();
                var start = (this.page - 1) * (this.perpage) + 1;
                if (data && data.length > this.perpage) {
                    this.maxPage = this.page + 1;
                    tmp = data.slice(0, -1);
                    end = start + data.length - 2;
                } else {
                    this.maxPage = this.page;
                    tmp = data;
                    end = start + data.length - 1;
                }
                this.renderPagebar(start, end);
                this.hideLoading();
                this.subtable.render(data, this.options.sub_columns, this.options.options);
            },
            showSubGrid: function () {
                this.$('.close-subtable').show();
                this.$('.subtable').addClass('on');
            },
            hideSubGrid: function () {
                this.$('.close-subtable').hide();
                this.subtable.clearBody();
                this.$('.subtable').removeClass('on');
            },
            closeSub: function () {
                this.sub_state = false;
                this.perpage = this.back_perpage;
                this.$('.pagination .ui.dropdown').dropdown('set value',this.perpage);
                this.page = this.back_page;
                this.maxPage = this.back_max;
                this.hideSubGrid();
                this.renderPagebar(this.back_start, this.back_end);
            },
            openSub: function (e) {
                this.sub_state = true;
                this.back_perpage = this.perpage;
                this.back_max = this.maxPage;
                this.back_page = this.page;
                this.back_start = this.pagebar.start;
                this.back_end = this.pagebar.end;
                this.page = 1;
                this.showLoading();
                var storeName = this.options.subStoreName;
                AppCube.DataRepository.refresh(storeName, {});
                e.stopPropagation();
            },
            changeSubPerpage: function (e) {
                var value = this.$('.pagination .ui.dropdown').dropdown('get value');
                this.perpage = parseInt(value);
                this.page = 1;
                if (!this.options.static_data) {
                    this.refresh();
                } else {
                    this.renderSubComponent();
                }
            },
            changePerpage: function (e) {
                if (this.sub_state) {
                    this.changeSubPerpage(e);
                } else {
                    PageTable.prototype.changePerpage.call(this, e);
                }
            },
            changePage: function (e) {
                if (this.sub_state) {
                    this.changeSubPage(e);
                } else {
                    PageTable.prototype.changePage.call(this, e);
                }
            },
            changeSubPage: function (e) {
                if ($(e.currentTarget).hasClass('disabled'))return;
                if ($(e.currentTarget).hasClass('prev')) {
                    if (this.page > 1) {
                        $(e.currentTarget).addClass('disabled');
                        this.page--;
                        if (!this.options.static_data) {
                            //this.refresh();
                        } else {
                            this.renderSubComponent();
                        }
                    }
                } else if ($(e.currentTarget).hasClass('next')) {
                    if (this.page < this.maxPage) {
                        $(e.currentTarget).addClass('disabled');
                        this.page++;
                        if (!this.options.static_data) {
                            //this.refresh();
                        } else {
                            this.renderSubComponent();
                        }
                    }
                }
            },
            refreshHandler: function () {
                if (this.sub_state) {
                    this.closeSub();
                }
                this.page = 1;
                PageTable.prototype.renderComponent.call(this);

            },
            renderSubComponent: function () {
                if(!this.sub_state)return false;
                var self = this;
                var storeName = this.options.subStoreName;
                var stateName = this.options.subStateName;
                var options = this.getValue();
                AppCube.DataRepository.fetch(storeName, stateName, options).done(function (data) {
                    self.renderSubGrid(data);
                    self.showSubGrid();
                });
            },
            render: function () {
                Marionette.ItemView.prototype.render.call(this);
                this.table = new AdvancedTable(this.$('.grid-content'), {}, this.options.columns, this.options.options);
                this.subtable = new AdvancedTable(this.$('.sub-grid-content'), {}, this.options.sub_columns, this.options.options);
                this.initGrid();
                if (!this.options.static_data) {
                    this.refresh();
                } else {
                    this.renderComponent();
                }
            }
        });
    });