#!/usr/bin/env node

var program = require('commander');
var promptly = require('promptly');
var exec = require('child_process').exec;
var fs = require('fs');

var MODULE = {
    module: '',
    actions: {},
    path: {
        root: 'src/javascript',
        views: 'views',
        template: 'template',
        modules: 'src/javascript/modules'
    }
};

function AddMenu(module) {
    promptly.prompt('Add Menu to [Module ' + module + ']: (Enter n to exit)', function (err, value) {
        if (value != 'n' && value != 'N') {
            if (!MODULE.actions[value])MODULE.actions[value] = [];
            AddAction(value, module);
        } else {
            CreateModule();
        }
        return value;
    });
}


function AddAction(menu, module) {
    promptly.prompt('Add [Action] to [Menu ' + menu + ']: (Enter n to exit)', function (err, value) {
        if (value != 'n' && value != 'N') {
            MODULE.actions[menu].push(value);
            AddAction(menu, module);
        } else {
            AddMenu(module);
        }
    });
}

function CreateModule() {
    var mn = MODULE.module.toLowerCase();

    var module_path = MODULE.path.modules + '/' + mn;
    if (!fs.existsSync(module_path)) {
        //create dir
        fs.mkdirSync(module_path, 0777);
        console.log('create module ' + MODULE.module + ' at "' + module_path + '"');

        //create main.js
        var bootstrapper_buf = fs.readFileSync(MODULE.path.template + '/moduleMain', 'utf8');
        var bootstrapper = bootstrapper_buf.replace(/\$MODULE_NAME\$/g, mn);
        fs.writeFileSync(MODULE.path.root + '/' + mn + 'Main.js', bootstrapper, 'utf8');

        //create view.html
        var menu = '',
            content = '',
            action_layout = '',
            action_list = '',
            action_render = '';
        var item_buf = fs.readFileSync(MODULE.path.template + '/views/item', 'utf8');
        for (var menu_name in MODULE.actions) {
            var sub_menu = '';
            var actions = MODULE.actions[menu_name];
            for (var i in actions) {
                var an = actions[i];
                sub_menu += '<li><a href="#' + an.toLowerCase() + '">' + an + '</a></li>\n\t\t\t\t\t\t\t';
                content += '<div id="' + mn + '-' + an.toLowerCase() + '"></div>\n\t\t\t';
                action_layout += '\'./' + an.toLowerCase() + '/layout\',\n\t';
                var uc_an = an.charAt(0).toUpperCase() + an.substring(1).toLowerCase();
                action_list += uc_an + ',';
                action_render += 'LayoutRender.render(' + uc_an + ');\n\t';
                CreateAction(an, mn);
            }
            menu += item_buf.replace(/\$MENU_NAME\$/g, menu_name)
                .replace(/\$ITEM\$/g, sub_menu);
        }
        content += '<!-- $APPEND_MENU$ -->';
        var layout_buf = fs.readFileSync(MODULE.path.template + '/views/layout', 'utf8');
        var layout = layout_buf.replace(/\$MODULE_NAME\$/g, mn)
            .replace(/\$SIDEBAR\$/g, menu)
            .replace(/\$CONTENT\$/g, content);
        fs.writeFileSync(MODULE.path.views + '/' + mn + '.html', layout, 'utf8');

        //create bootstrapper,router,global_store
        var boot_buf = fs.readFileSync(MODULE.path.template + '/module/bootstrapper', 'utf8');
        var boot = boot_buf.replace(/\$ACTION_LAYOUT\$/g, action_layout)
            .replace(/\$ACTION_LIST\$/g, action_list)
            .replace(/\$ACTION_RENDER\$/g, action_render);
        fs.writeFileSync(module_path + '/bootstrapper.js', boot, 'utf8');

        var router = fs.readFileSync(MODULE.path.template + '/module/router', 'utf8');
        fs.writeFileSync(module_path + '/router.js', router, 'utf8');

        var store = fs.readFileSync(MODULE.path.template + '/module/store', 'utf8');
        fs.writeFileSync(module_path + '/store.js', store, 'utf8');

    } else {
        console.log('create module ' + MODULE.module + ' Failed:already existed');
    }
}

function CreateAction(an, mn) {

    var action_path = MODULE.path.modules + '/' + mn.toLowerCase() + '/' + an.toLowerCase();

    if (!fs.existsSync(action_path)) {
        //create dir
        fs.mkdirSync(action_path, 0777);
        console.log('add action ' + an + ' to module ' + MODULE.module + ' at "' + action_path + '"');

        var layout_buf = fs.readFileSync(MODULE.path.template + '/module/action/layout', 'utf8');
        var layout = layout_buf.replace(/\$MODULE_NAME\$/g, mn.toLowerCase())
            .replace(/\$ACTION_NAME\$/g, an.toLowerCase());
        fs.writeFileSync(action_path + '/layout.js', layout, 'utf8');

        var template_buf = fs.readFileSync(MODULE.path.template + '/module/action/template', 'utf8');
        var template = template_buf.replace(/\$MODULE_NAME\$/g, mn.toLowerCase())
            .replace(/\$ACTION_NAME\$/g, an.toLowerCase());
        fs.writeFileSync(action_path + '/template.html', template, 'utf8');

    } else {
        console.log('add action ' + an + ' Failed:already existed');
    }
}

program.command('new [module]')
    .option('-p, --path [path]')
    .action(function (module) {
        if (!module) {
            promptly.prompt('Please input your [Module] name: ', function (err, value) {
                MODULE.module = value;
                AddMenu(value);
            });
        } else {
            MODULE.module = module;
            AddMenu(module);
        }
    });

program.command('remove [module]')
    .action(function (module) {
        if (!module) {
            console.log('You must input module name!');
        } else {
            var module_path = MODULE.path.modules + '/' + module.toLowerCase();
            if (fs.existsSync(module_path)) {
                exec('rm -rf ' + module_path);
            }
            var main = MODULE.path.root + '/' + module.toLowerCase() + 'Main.js';
            if (fs.existsSync(main)) {
                fs.unlinkSync(main);
            }
            var view = MODULE.path.views + '/' + module.toLowerCase() + '.html';
            if (fs.existsSync(view)) {
                fs.unlinkSync(view);
            }
        }
    });

program.parse(process.argv);




