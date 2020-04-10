var CONTEXT_PATH = "/recommend";
$(function(){
    $("input").focus(clear_error);
    $("#old-password").blur(check_oldpassword);
    $("#new-password").blur(check_newpassword);
    $("#confirm-password").blur(check_confirm);
    // $("#header-form").submit(check_data);
});

function check_oldpassword(){
    var pwd = $("#old-password").val();
    //校验密码
    var reg = /^\w{6,20}$/;
    if(reg.test(pwd)){
        return true;
    }else{
        $("#old-password").addClass("is-invalid");
        if(pwd.length == 0 || pwd == ""){
            $("#invalid-old-password").text("原密码不能为空");
        }else if(!reg.test(pwd)){
            $("#invalid-old-password").text("密码应是6到20个数字或字母");
        }
        return false;
    }
}

function check_newpassword(){
    var pwd = $("#new-password").val();
    //校验密码
    var reg = /^\w{6,20}$/;
    if(reg.test(pwd)){
        return true;
    }else{
        $("#new-password").addClass("is-invalid");
        if(pwd.length == 0 || pwd == ""){
            $("#invalid-new-password").text("新密码不能为空");
        }else if(!reg.test(pwd)){
            $("#invalid-new-password").text("新密码应包含6到20个数字或字母");
        }
        return false;
    }
}

function check_confirm(){
    var pwd1 = $("#new-password").val();
    var pwd2 = $("#confirm-password").val();
    if(pwd2.length == 0 || pwd2 == ""){
        $("#confirm-password").addClass("is-invalid");
        $("#invalid-confirm-password").text("确认密码不能为空");
        return false;
    }else if(pwd1 != pwd2) {
        $("#confirm-password").addClass("is-invalid");
        $("#invalid-confirm-password").text("两次输入密码不一致");
        return false;
    }
    return true;
}




function reset_password() {
    var f1 = check_oldpassword();
    var f2 = check_newpassword();
    var f3 = check_confirm();
    if(f1 && f2 && f3){
        var flag = false;
        params = {
            "oldPassword" : $("#old-password").val(),
            "newPassword" : $("#new-password").val(),
        };
        $.ajax({
            async:false,
            type: "POST",//方法类型
            dataType: "json",//预期服务器返回的数据类型
            url: CONTEXT_PATH + "/user/reset" ,//url
            data: params,
            complete: function(result){
                if (eval("(" + result.responseText + ")")){
                    flag = true;
                }else{
                    $("#div-alert").append(
                        '<div class="alert alert-danger alert-dismissible fade show" role="alert">'+
                        '原密码错误'+
                        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">'+
                        '<span aria-hidden="true">&times;</span>'+
                        '</button>'+
                        '</div>'
                    );
                    remove_alert();
                }
            }
        });
        if(flag){
            //刷新页面
            window.location.reload();
            return true;
        }
    }
    return false;
}



$(function () {
    function imgPreview(fileDom,e){
        var file = fileDom.files[0];
        var supportedTypes = ['image/jpg', 'image/jpeg', 'image/png'];
        if(file == null){ //值为空
            $("#head-image").addClass("is-invalid");
            $("#invalid-img").text("请选择图片");
        }
        if(!(supportedTypes.indexOf(file.type)>-1)){
            $("#head-image").addClass("is-invalid");
            $("#invalid-img").text("图片格式错误");
        }
        var size = file.size;
        if(size > 2048 * 1024){
            $("#head-image").addClass("is-invalid");
            $("#invalid-img").text("图片不得超过2MB");
        }
        var uploadFiles = e.target.files || e.dataTransfer.files;
        var size = file.size;
        for(var i=0;i<size;i++){
            /*读取文件*/
            var reader=new FileReader();
            reader.readAsDataURL(uploadFiles[i]);
            reader.onload=function (e) {
                var imgBase=e.target.result;
                $("#avatar-img").attr('src', imgBase).show();
            }
        }
    }
    $('#head-image').change(function (e) {
        imgPreview(this,e);
    });
    $('#header-form').submit(function(){
        var file = $("#head-image").get(0).files[0];
        if(file == null){ //值为空
            $("#head-image").addClass("is-invalid");
            $("#invalid-img").text("请选择图片");
            return false;
        }
        var supportedTypes = ['image/jpg', 'image/jpeg', 'image/png'];
        var size = file.size;
        if(!(supportedTypes.indexOf(file.type)>-1)){
            $("#head-image").addClass("is-invalid");
            $("#invalid-img").text("图片格式错误");
            return false;
        }else if(size > 2048 * 1024){
            $("#head-image").addClass("is-invalid");
            $("#invalid-img").text("图片不得超过2MB");
            return false;
        }
        return true;
    });
});

function remove_alert(){
    setTimeout(function(){
        $('[data-dismiss="alert"]').alert('close');
    },  3000);
}


function clear_error() {
    $(this).removeClass("is-invalid");
}