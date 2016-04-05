(function () {
    var ALLOW_OP = /[.!\s\w\%,*`'"<>=()\+\-\;\[\]/]/;
    var FILED_ALLOW_OP = /\w/;
    var QUOTATION_OP = /[`'"]/;
    var OP = /^(=|\+|\-|\%|\/|\*|<|>|!>|!<|<>|!=|AND|OR|XOR|IN|REGEXP|LIKE|IS|BETWEEN|EXISTS)$/i;
    var CONDITION_OP = /^(AND|OR|XOR)$/i;
    var DECORATE_OP = /^(IN|LIKE|BETWEEN|EXISTS)$/i;
    var COMMAND_OP = /^(SELECT|UPDATE|DELETE|DROP|SHOW|DESC|EXPLAIN|CACHE|INSERT|CREATE|ALTER)$/i;

    var KEY_OP = /^(REGEXP|AND|OR|XOR|UNION|INTERSECT|EXCEPT|SELECT|UPDATE|DELETE|DROP|SHOW|DESC|EXPLAIN|CACHE|INSERT|CREATE|ALTER|DISTINCT|AS|IN|LIKE|BETWEEN|EXISTS|AND|OR|XOR|NOT|TABLE|COLUMN)$/i;
    var COLLECTION_OP = /^(UNION|INTERSECT|EXCEPT)$/i;

    var ERROR = {
        SYNTAX_ERROR:"Error : You have an error in your SQL syntax;",
        EMPTY_ERROR:"Error : Your have an empty sequence expression in your statement;",
        INVALID_CHARACTER:"Error : Your statement contains the illegal character;",
        INVALID_WHERE:"Error : Invalid use of WHERE",
        INVALID_HAVING:"Error : Invalid use of HAVING",
        INVALID_GROUP:"Error : Invalid use of GROUP BY",
        INVALID_ORDER:"Error : Invalid use of ORDER BY",
        INVALID_LIMIT:"Error : Invalid use of LIMIT",
        NOT_SUPPORT:"Error : Only support select statement",
        NO_TABLE:"Error : No table used"
    };

    var ORDER_PATTERN = [
        /^[.\w\[\]*`]+$/i, [0],
        /^[.\w\[\]*`]+ (DESC|ASC)$/i, [0],
        /^[.\w*`]+ ##QUERY## (DESC|ASC)$/i, [0]
    ];

    var GROUP_PATTERN = [
        /^[.\w\[\]*`]+$/i, [0]
    ];

    var SEQUENCE_PATTERN = [
        /^[.\w\[\]*`]+$/i, [0],
        /^(##STRING##|##QUERY##)$/i, [-1],
        /^[.\w*`]+ ##QUERY##$/i, [0],
        /^DISTINCT [.\w\*`]+ ##QUERY##$/i, [1]
    ];

    var COLUMN_PATTERN = [
        /^[.\w\[\]*`]+$/i, [0],
        //length = 2
        /^DISTINCT [.\w\[\]*`]+$/i, [1],
        /^[.\w\[\]*`]+ [.\w\[\]*`]+$/i, [0,1],
        /^[.\w*`]+ ##QUERY##$/i, [0],
        //length = 3
        /^[.\w\[\]*`]+ AS [.\w\[\]*`]+$/i, [0,2],
        /^[.\w\*`]+ ##QUERY## [.\w\[\]*`]+$/i, [0,2],
        /^DISTINCT [.\w\[\]*`]+ [.\w\[\]*`]+$/i, [1,2],
        /^DISTINCT [.\w\*`]+ ##QUERY##$/i, [1],
        //length = 4
        /^DISTINCT [.\w\[\]*`]+ AS [.\w\[\]*`]+$/i, [1,3],
        /^[.\w*`]+ ##QUERY## AS [.\w\[\]*`]+$/i, [3],
        /^DISTINCT [.\w*`]+ ##QUERY## [.\w\[\]*`]+$/i, [3],
        //length = 5
        /^DISTINCT [.\w*`]+ ##QUERY## AS [.\w\[\]*`]+$/i, [4]
    ];

    var WORD_PATTERN = [
        /^\d+$/i, [-1],
        /^(##STRING##|##QUERY##)$/i, [-1],
        /^DISTINCT [.\w\[\]*`]+$/i, [1],
        /^[.\w\[\]*`]+$/i, [0],
        /^[.\w*`]+ ##QUERY##$/i, [0]
    ];

    var TABLE_PATTERN = [
        /^##QUERY##$/i, [-1],
        /^##QUERY## [.\w\[\]*`]+$/i, [0,1],
        /^##QUERY## AS [.\w\[\]*`]+$/i, [0,2],
        /^[.\w\[\]*`]+$/i, [0],
        /^[.\w\[\]*`]+ [.\w\[\]*`]+$/i, [0,1],
        /^[.\w\[\]*`]+ AS [.\w\[\]*`]+$/i, [0,2]
    ];
    //distinct
    function PUSH(ARRAY,v,sl,sc,el,ec){
        ARRAY.push({
            value:v,
            start_line:sl,
            start_column:sc,
            end_line:el,
            end_column:ec
        });
    }
    //判断该字符是否被转义
    function IS_ESCAPE(query,index){
        if(index==0)return false;
        var count = 0;
        while(index>0&&query[index-1]=='\\'){
            count++;
            index--;
        }
        return !(count%2==0);
    }
    //分解查询并记录索引
    function SPLIT_QUERY(query,ctx){
        if(typeof query!="string")query = "";
        var new_word = "", flag_read = false, LAST_RIGHT_BRACKET,LAST_LEFT_BRACKET;
        var line = 0, column = -1;
        var start_column = 0,start_line = 0;
        var RESULT = [], IN_STACK = [];
        var string_flag = false;
        //初始化指针
        this.IN = RESULT;

        function PUSH_READ(ctx){
            PUSH(ctx,new_word,start_line,start_column,line,column-1);
            flag_read = false;
            new_word = "";
        }

        for (var i = 0 ; i < query.length ; i++) {
            //计算更新行号
            if(i>0&&query[i-1]=='\n'){
                line++;
                column = 0;
            }else{
                column++;
            }
            //处理引号
            if(QUOTATION_OP.test(query[i])){
                if(!string_flag){
                    if(flag_read){
                        //处理如 A"BC的情况,(+-会提前中断flag_read
                        ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,{
                            start_line:line,
                            start_column:column,
                            end_line:line,
                            end_column:column
                        });
                        throw new Error();
                    }
                    string_flag = query[i];
                    start_column = column;
                    start_line = line;
                    new_word = query[i];
                }else{
                    //处理闭合
                    new_word+=query[i];
                    if(string_flag==query[i]&&!IS_ESCAPE(query,i)){
                        PUSH_READ(this.IN);
                        string_flag = false;
                    }
                }
                //结尾还未闭合
                if(string_flag && i == query.length-1){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,{
                        start_line:start_line,
                        start_column:start_column,
                        end_line:line,
                        end_column:column
                    });
                    throw new Error();
                }
                continue;
            }else{
                //结尾还未闭合,或者字段名非法
                if(string_flag=="`"&&!FILED_ALLOW_OP.test(query[i])||string_flag&&i == query.length-1){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,{
                        start_line:start_line,
                        start_column:start_column,
                        end_line:line,
                        end_column:column
                    });
                    throw new Error();
                }
                if(string_flag){
                    new_word+=query[i];
                    continue;
                }
            }
            //检验非法字符
            if(!string_flag&&!ALLOW_OP.test(query[i])){
                ADD_ERROR(ctx.result,ERROR.INVALID_CHARACTER,{
                    start_line:line,
                    start_column:column,
                    end_line:line,
                    end_column:column
                });
                throw new Error();
            }
            //处理通用字符
            if(/\s/.test(query[i])){
                if(!flag_read)continue;
                PUSH_READ(this.IN);

            }else if(query[i]==';'){
                if(!/^\s*$/.test(query.slice(i+1))){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,{
                        start_line:line,
                        start_column:column,
                        end_line:line,
                        end_column:column
                    });
                    throw new Error();
                }
            }else if(/[,/\+\-<>=\%!]/.test(query[i])){
                if(flag_read)PUSH_READ(this.IN);
                if(
                    query[i]=='<'&&query[i+1]&&query[i+1]=='>'
                    ||query[i]=='!'&&query[i+1]&&/[<>=]/.test(query[i+1])
                ){
                    PUSH(this.IN,query[i]+query[i+1],line,column,line,column+1);
                    i++;
                }else{
                    PUSH(this.IN,query[i],line,column,line,column);
                }

            }else if(query[i] == "("){
                if(flag_read)PUSH_READ(this.IN);
                LAST_LEFT_BRACKET = {
                    start_line:line,
                    start_column:column,
                    end_line:line,
                    end_column:column
                };
                PUSH(IN_STACK,"(",line,column,line,column);
                var length = this.IN.push([]);
                this.IN = this.IN[length - 1];
                PUSH(this.IN,"(",line,column,line,column);

            }else if(query[i] == ")"){
                if(flag_read)PUSH_READ(this.IN);
                LAST_RIGHT_BRACKET = {
                    start_line:line,
                    start_column:column,
                    end_line:line,
                    end_column:column
                };
                var LEFT_BRACKET = IN_STACK.pop();
                if(typeof LEFT_BRACKET == "undefined"){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,{
                        start_line:line,
                        start_column:column,
                        end_line:line,
                        end_column:column
                    });
                    throw new Error();
                }
                var tmp = RESULT,depth = IN_STACK.length;
                while(depth>=0&&$.isArray(tmp[tmp.length-1])){
                    this.IN = tmp;
                    depth--;
                    tmp = tmp[tmp.length-1];
                }

            }else if(flag_read){
                new_word+=query[i];
            }else{
                start_column = column;
                start_line = line;
                flag_read = true;
                new_word = query[i];
            }
            //输入结尾字符
            if(i == query.length-1&&IN_STACK.length>0){
                ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,LAST_LEFT_BRACKET,LAST_RIGHT_BRACKET);
                throw new Error();
            }
            if(flag_read && i == query.length-1){
                PUSH(this.IN,new_word,start_line,start_column,line,column);
            }
        }
        return RESULT;
    }
    //往结果集添加错误
    function ADD_ERROR(result,msg,start,end){
        var _end = typeof end == "undefined"?start:end;
        result.push({
            from: CodeMirror.Pos(start.start_line, start.start_column),
            to: CodeMirror.Pos(_end.end_line, _end.end_column + 1),
            message: msg
        });
    }
    //效验子查询
    function VALIDATE_QUERY(query,ctx,sub){
        var STATEMENT = [],maybe_sequence = false,maybe_table = false,case_count = 0;
        for(var i = 0;i < query.length;i++){
            var input = query[i];
            if(IS_KEY(input.value,"JOIN"))maybe_table = true;
            if(case_count>0){
                continue;
            }else if($.isArray(input)){
                if(input.length==1&&input[0].end_column > (query[i-1].end_column+1)){
                    ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,input[0]);
                    throw new Error();
                }else{
                    VALIDATE_QUERY(input.slice(1),ctx,input[0]);
                }
                STATEMENT.push({
                    value:"##QUERY##",
                    start_line:input[0].start_line,
                    start_column:input[0].start_column,
                    end_line:input[input.length-1].end_line,
                    end_column:input[input.length-1].end_column
                });
            }else if(IS_KEY(input.value, "CASE")){
                if(case_count==0)
                    STATEMENT.push({
                        value:"##QUERY##",
                        start_line:input[0].start_line,
                        start_column:input[0].start_column,
                        end_line:input[input.length-1].end_line,
                        end_column:input[input.length-1].end_column
                    });
                case_count++;
                continue;
            }else if(IS_KEY(input.value, "END")){
                case_count--;
                continue;
            }else if(COLLECTION_OP.test(input.value)){
                if(
                    IS_KEY(input.value, "UNION")&&query[i+1]
                    &&(IS_KEY(query[i+1].value, "ALL")||IS_KEY(query[i+1].value, "DISTINCT"))
                ){
                    i++;
                }
                if(!(query[i+1]&&IS_KEY(query[i+1].value, ["SELECT","##QUERY##"]))){
                    ADD_ERROR(ctx.result, ERROR.NOT_SUPPORT, STATEMENT[STATEMENT.length - 1],input);
                    continue;
                }
                if(STATEMENT[STATEMENT.length-1].value!="##QUERY##"){
                    if (IS_KEY(STATEMENT[0].value, "SELECT")){
                        VALIDATE_SELECT(STATEMENT, ctx);
                        STATEMENT = [];
                    } else {
                        ADD_ERROR(ctx.result, ERROR.NOT_SUPPORT, STATEMENT[STATEMENT.length - 1],input);
                        continue;
                    }
                }else{
                    STATEMENT = [];
                }
            }else{
                if(/^["']/.test(input.value)){
                    STATEMENT.push({
                        value:"##STRING##",
                        start_line:input.start_line,
                        start_column:input.start_column,
                        end_line:input.end_line,
                        end_column:input.end_column
                    });
                }else{
                    if(input.value==",")maybe_sequence = true;
                    STATEMENT.push(input);
                }
            }
        }
        if(STATEMENT.length>0) {
            if(COMMAND_OP.test(STATEMENT[0].value)) {
                if (IS_KEY(STATEMENT[0].value, "SELECT")) {
                    VALIDATE_SELECT(STATEMENT, ctx);
                } else {
                    ADD_ERROR(ctx.result, ERROR.NOT_SUPPORT, STATEMENT[0], STATEMENT[STATEMENT.length - 1]);
                }
            }else if(maybe_table){
                VALIDATE_TABLE(STATEMENT,ctx);
            }else if(sub){
                if(maybe_sequence||STATEMENT.length==1&&!OP.test(STATEMENT[0].value)){
                    if(sub)STATEMENT.splice(0,0,sub);
                    VALIDATE_COLUMN(STATEMENT,true,ctx,SEQUENCE_PATTERN);
                }else{
                    if(sub)STATEMENT.splice(0,0,sub);
                    VALIDATE_CONDITION(STATEMENT,true,ctx);
                }
            }else{
                for(var j=0;j<STATEMENT.length;j++){
                    if(STATEMENT[j].value!="##QUERY##"){
                        ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,STATEMENT[j]);
                        break;
                    }
                }
            }
        }
    }

    function IS_KEY(str,array){
        if(typeof str !="string")return false;
        var text = str.toUpperCase(),result = false;
        if($.isArray(array)){
            for(var i=0;i<array.length;i++){
                if(text==array[i]){
                    result = true;
                    break;
                }
            }
        }else{
            result = (text == array);
        }
        return result;
    }

    function IS_NOT_KEY(query,index){
        for(var i in index){
            if(index[i]>-1){
                var word = query[index[i]].value.toUpperCase();
                if(KEY_OP.test(word))return false;
            }
        }
        return true;
    }
    //效验select
    function VALIDATE_SELECT(query,ctx){
        var read_from,read_where,read_group,read_have,read_order,read_limit;
        var columns = [], tables = [], where = [],group = [],have = [],order = [],limit = [];
        columns.push(query[0]);
        for(var i=1;i<query.length;i++) {
            var input = query[i];

            if (IS_KEY(input.value, "FROM")) {
                if (read_from || read_where || read_group || read_have || read_order || read_limit) {
                    ADD_ERROR(ctx.result, ERROR.SYNTAX_ERROR, query[0], input);
                    throw new Error();
                }
                read_from = true;
                tables.push(input);
                continue;
            }
            if (IS_KEY(input.value, "WHERE")) {
                if (!read_from || read_where || read_group || read_have || read_order || read_limit) {
                    ADD_ERROR(ctx.result, ERROR.INVALID_WHERE, query[0], input);
                    throw new Error();
                }
                read_where = true;
                where.push(input);
                continue;
            }
            if (IS_KEY(input.value, "GROUP") && i < query.length - 1 && IS_KEY(query[i + 1].value, "BY")) {
                if (!read_from || read_group || read_have || read_order || read_limit) {
                    ADD_ERROR(ctx.result, ERROR.INVALID_GROUP, query[0], input);
                    throw new Error();
                }
                i++;
                read_group = true;
                group.push({
                    value:input.value,
                    start_line:input.start_line,
                    start_column:input.start_column,
                    end_line:query[i].end_line,
                    end_column:query[i].end_column
                });
                continue;
            }
            if (IS_KEY(input.value, "HAVING")) {
                if (!read_from || read_have || read_order || read_limit) {
                    ADD_ERROR(ctx.result, ERROR.INVALID_HAVING, query[0], input);
                    throw new Error();
                }
                read_have = true;
                have.push(input);
                continue;
            }
            if (IS_KEY(input.value, "ORDER") && i < query.length - 1 && IS_KEY(query[i + 1].value, "BY")) {
                if (!read_from || read_order || read_limit) {
                    ADD_ERROR(ctx.result, ERROR.INVALID_ORDER, query[0], input);
                    throw new Error();
                }
                i++;
                read_order = true;
                order.push({
                    value:input.value,
                    start_line:input.start_line,
                    start_column:input.start_column,
                    end_line:query[i].end_line,
                    end_column:query[i].end_column
                });
                continue;
            }
            if (IS_KEY(input.value, "LIMIT")) {
                if (!read_from || read_limit) {
                    ADD_ERROR(ctx.result, ERROR.INVALID_LIMIT, query[0], input);
                    throw new Error();
                }
                read_limit = true;
                limit.push(input);
                continue;
            }

            if (read_limit)limit.push(input);
            else if (read_order)order.push(input);
            else if (read_have)have.push(input);
            else if (read_group)group.push(input);
            else if (read_where)where.push(input);
            else if (read_from)tables.push(input);
            else columns.push(input);
        }

        if(!read_from){
            ADD_ERROR(ctx.result,ERROR.NO_TABLE,query[0],query[query.length-1]);
        }
        //效验小句
        VALIDATE_COLUMN(columns,true,ctx);
        VALIDATE_COLUMN(group,read_group,ctx,GROUP_PATTERN);
        VALIDATE_COLUMN(order,read_order,ctx,ORDER_PATTERN);
        VALIDATE_LIMIT(limit,read_limit,ctx);
        VALIDATE_CONDITION(have,read_have,ctx);
        VALIDATE_CONDITION(where,read_where,ctx);
        VALIDATE_FROM(tables,read_from,ctx);
    }

    function VALIDATE_LIMIT(query,bool,ctx){
        if(!bool)return;
        var test = "";
        for(var w in query){test+=(" "+query[w].value);}
        test = test.slice(1);
        if(!/^LIMIT \d+$/i.test(test)&&!/^LIMIT \d+ , \d+$/i.test(test)){
            ADD_ERROR(ctx.result,ERROR.INVALID_LIMIT,query[0],query[query.length-1]);
        }
    }

    function VALIDATE_COLUMN(query,bool,ctx,pattern){
        if(!bool)return;
        if(query.length==1){
            ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,query[0],query[0]);
            return;
        }
        var word = [],lastIndex = 0;
        for(var i = 1;i<query.length;i++){
            var input = query[i];
            if(input.value == ","||i == query.length-1){
                if(input.value!=",")word.push(input);
                if(input.value == ",")lastIndex = i;
                if(!VALIDATE_PATTERN(word,pattern||COLUMN_PATTERN)){
                    VALIDATE_CONDITION([query[i]].concat(word),bool,ctx);
                    //ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,word[0],word[word.length-1]);
                }
                word = [];
            }else{
                word.push(input);
            }
            if(input.value == ","&&i == query.length-1){
                ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,input);
            }
        }
    }

    function VALIDATE_CONDITION(query,bool,ctx){
        if(!bool)return;
        if(query.length==1){
            ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,query[0],query[0]);
            return;
        }

        var word = [],op = [];
        for(var i = 1;i<query.length;i++){
            var input = query[i];
            if(OP.test(input.value)||i == query.length-1){
                if(i == query.length-1){
                    if(OP.test(input.value)||IS_KEY(input.value,"NOT")){
                        if(!(query.length==2&&input.value=="*")){
                            ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,input);
                            return;
                        }
                    }
                    word.push(input);
                }else{
                    op.push(input);
                }
                if(IS_KEY(input.value,"IS")&&query[i+1]&&IS_KEY(query[i+1].value,"NOT")){
                    if(i == query.length-2){
                        ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,input);
                        return;
                    }
                    i++;
                }
                if(IS_KEY(query[i].value,"EXISTS")&&query[i+1].value=="##QUERY##"){
                    i+=2;
                    continue;
                }
                if(word.length==0){
                    ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,query[i-1]);
                }else if(!VALIDATE_PATTERN(word,WORD_PATTERN)){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,word[0],word[word.length-1]);
                }
                word = [];
            }else if(IS_KEY(input.value,"NOT")&&query[i+1]&&DECORATE_OP.test(query[i+1].value)){
                if(i == query.length-2){
                    ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,input);
                    return;
                }
                i++;
                if(IS_KEY(query[i].value,"EXISTS")&&query[i+1].value=="##QUERY##"){
                    i+=2;
                    continue;
                }
                if(word.length==0){
                    ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,query[i-1]);
                }else if(!VALIDATE_PATTERN(word,WORD_PATTERN)){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,word[0],word[word.length-1]);
                }
                op.push(input);
                word = [];
            }else{
                word.push(input);
            }
        }
        VALIDATE_OP(op,ctx);
    }

    function VALIDATE_OP(query,ctx){
        var test = "";
        for(var i = 0;i<query.length;i++){
            var op = query[i].value;
            if(/^[/\+\-\*\%]$/.test(op)){
                test+="M";
            }else if(op == "="){
                test+="=";
            }else if(CONDITION_OP.test(op)){
                test+="C"
            }else{
                test+="K"
            }
        }
        if(/(==|KK|=M=|KMK|=K|K=)/.test(test)){
            ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,query[0],query[query.length-1]);
        }
    }

    function VALIDATE_FROM(query,bool,ctx){
        if(!bool)return;
        if(query.length==1){
            ADD_ERROR(ctx.result,ERROR.EMPTY_ERROR,query[0],query[0]);
            return;
        }
        var word = [];
        for(var i = 1;i<query.length;i++){
            var input = query[i];
            if(input.value == ","||i == query.length-1){
                if(input.value!=",")word.push(input);
                VALIDATE_TABLE(word,ctx);
                word = [];
            }else{
                word.push(input);
            }
            if(input.value == ","&&i == query.length-1){
                ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,input);
            }
        }
    }

    function VALIDATE_TABLE(query,ctx){
        var read_join = false,read_on = false,on = [],word = [];
        for(var i = 0;i<query.length;i++){
            var input = query[i];
            if(/^(INNER|LEFT|RIGHT|FULL)$/i.test(input.value)){
                if(read_join){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,query[0],input);
                    break;
                }
                if(read_on){
                    VALIDATE_CONDITION(on,read_on,ctx);
                    on = [];
                    read_on = false;
                }
                read_join = true;
                if(query[i+1]){
                    if(IS_KEY(query[i+1].value,"JOIN")){
                        i++;
                        if(!VALIDATE_PATTERN(word,TABLE_PATTERN)){
                            ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,word[0],word[word.length-1]);
                        }
                        word = [];
                    }else if(
                        IS_KEY(query[i+1].value,["OUTER","SEMI"])
                        &&query[i+2]&&IS_KEY(query[i+2].value,"JOIN")
                    ){
                        if(!VALIDATE_PATTERN(word,TABLE_PATTERN)){
                            ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,word[0],word[word.length-1]);
                        }
                        word = [];
                        i+=2;
                    }
                }
            }else if(IS_KEY(input.value,"JOIN")){
                if(read_join){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,query[0],input);
                    break;
                }
                read_join = true;
                if(read_on){
                    VALIDATE_CONDITION(on,read_on,ctx);
                    on = [];
                    read_on = false;
                }
                if(!VALIDATE_PATTERN(word,TABLE_PATTERN)){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,word[0],word[word.length-1]);
                }
                word = [];

            }else if(IS_KEY(input.value,"ON")){
                if(!read_join||read_on){
                    ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,query[0],input);
                    break;
                }
                read_on = true;
                on.push(input);
                if(read_join){
                    if(!VALIDATE_PATTERN(word,TABLE_PATTERN)){
                        ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,word[0],word[word.length-1]);
                    }
                    word = [];
                    read_join = false;
                }
            }else if(read_on){
                on.push(input);
            }else{
                word.push(input);
            }

            if(i == query.length-1){
                if(read_on){
                    VALIDATE_CONDITION(on,read_on,ctx);
                }else{
                    if(!VALIDATE_PATTERN(word,TABLE_PATTERN)){
                        ADD_ERROR(ctx.result,ERROR.SYNTAX_ERROR,word[0],word[word.length-1]);
                    }
                }
            }
        }
    }

    function VALIDATE_PATTERN(query,pattern){
        var match = false,test = "";
        for(var w in query){test+=(" "+query[w].value);}
        test = test.slice(1);
        for(var i = 0;i<pattern.length;i+=2){
            if(pattern[i].test(test) ){
                if(!IS_NOT_KEY(query,pattern[i+1]))continue;
                match = true;
                break;
            }
        }
        return match;
    }
    //构造函数
    function SQLParser(){
        this.result = [];
    }
    //查询入口
    SQLParser.prototype.validate = function(query){
        VALIDATE_QUERY(SPLIT_QUERY(query,this),this);
    };

    CodeMirror.registerHelper("lint", "sql", function (text) {
        var parser = new SQLParser();
        try{
            parser.validate(text);
        }catch(e){}
        return parser.result;
    });

})();