var CONTEXT_PATH = "/recommend"
$(function(){
    $("input").focus(clear_error);
    $("#email").blur(check_email);
    $("#password").blur(check_password);
    $("#verifycode").blur(check_verifycode);
});
function check_email() {
    var email = $("#email").val();
    var reg = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(.[a-zA-Z0-9_-])+/;
    // var reg = new RegExp("^[a-z0-9A-Z]+[- | a-z0-9A-Z . _]+@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-z]{2,}$");
    if(reg.test(email)){
        return true;
    }else{
        $("#email").addClass("is-invalid");
        if(email.length == 0 || email == ""){
            $("#invalid-email").text("邮箱不能为空");
        }else if(!reg.test(email)){
            $("#invalid-email").text("邮箱格式错误");
        }
        return false;
    }
}

function check_password() {
    var pwd = $("#password").val();
    if(pwd.length == 0 || pwd == ""){
        $("#password").addClass("is-invalid");
        $("#invalid-password").text("密码不能为空");
        return false;
    }else{
        return true;
    }
}

function check_verifycode() {
    var verifycode = $("#verifycode").val();
    if(verifycode.length == 0 || verifycode == ""){
        $("#verifycode").addClass("is-invalid");
        $("#invalid-verifycode").text("验证码不能为空");
        return false;
    }else{
        return true;
    }
}

function login(){
    var f1 = check_email();
    var f2 = check_password();
    var f3 = check_verifycode();
    if(f1 && f2 && f3) {
        var flag = false;
        params = {
            "email": $("#email").val(),
            "password": $("#password").val(),
            "code": $("#verifycode").val(),
            "rememberme": $("#remember-me").val()
        };
        $.post(CONTEXT_PATH + "/login",
            params,
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    window.location.replace(CONTEXT_PATH + "/");
                    // return true;
                    // flag = true;
                } else {
                    $("#div-alert").append(
                        '<div class="alert alert-danger alert-dismissible fade show" role="alert">' +
                        data.msg +
                        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
                        '<span aria-hidden="true">&times;</span>' +
                        '</button>' +
                        '</div>'
                    );
                    remove_alert();
                }
            });
    }
    //     $.ajax({
    //         async:false,
    //         type: "POST",//方法类型
    //         // dataType: "json",预期服务器返回的数据类型
    //         url: CONTEXT_PATH + "/login" ,//url
    //         data: params,
    //         complete: function(data){
    //             data = $.parseJSON(data);
    //             if(data.code == 0){
    //                 flag = true;
    //             }else{
    //                 $("#div-alert").append(
    //                     '<div class="alert alert-danger alert-dismissible fade show" role="alert">'+
    //                      data.msg +
    //                     '<button type="button" class="close" data-dismiss="alert" aria-label="Close">'+
    //                     '<span aria-hidden="true">&times;</span>'+
    //                     '</button>'+
    //                     '</div>'
    //                 );
    //                 remove_alert();
    //             }
    //         }
    //     });
    //     if(flag){
    //         window.location.replace(CONTEXT_PATH + "/");
    //         return true;
    //     }
    // }
    // return false;
}

function remove_alert(){
    setTimeout(function(){
        $('[data-dismiss="alert"]').alert('close');
    },  3000);
}


function clear_error() {
    $(this).removeClass("is-invalid");
}