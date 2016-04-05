define(
    [
        'app',
        'tpl!./template.html',
        'component/dialog/BasicDialog',
        'jquery'
    ],
    function (AppCube, template, BasicDialog, $) {

        return BasicDialog.extend({
            events: {
                "keyup .field input": "autoCheck"
            },
            template: template,
            getValue: function () {
                var name = this.$('[name=name]').val();
                return {name: name}
            }
        });
    });