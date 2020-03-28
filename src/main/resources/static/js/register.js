var CONTEXT_PATH = "/recommend";
$(function(){
	$("form").submit(check_data);
	$("input").focus(clear_error);
	$("#email").blur(check_email);
	$("#username").blur(check_username);
	$("#password").blur(check_password);
	$("#confirm-password").blur(check_confirm)
});

function check_email() {
	var email = $("#email").val();
	var reg = /^[a-z0-9]+([._\\-]*[a-z0-9])*@([a-z0-9]+[-a-z0-9]*[a-z0-9]+.){1,63}[a-z0-9]+$/;
	if(reg.test(email)){
		var flag = false;
		$.ajax({
			async:false,
			type:"POST",
			url:CONTEXT_PATH + "/check/email",
			data:"email="+email,
			dataType:"json",
			complete:function(result){
				if (!eval("(" + result.responseText + ")")) {
					$("#email").addClass("is-invalid");
					$("#invalid-email").text("该邮箱已被使用");
					flag = true;
				}
			}
		});
		if(flag){
			return false;
		}else{
			return true;
		}
	}else{
		$("#email").addClass("is-invalid");
		if(email.length == 0 || email == ""){
			$("#invalid-email").text("邮箱不能为空");
		}else if(!reg.test(email)){
			$("#invalid-email").text("请输入正确的邮箱");
		}
		return false;
	}
}



function check_username() {
	var username = $("#username").val();
	//校验用户名
	var reg = /^[\u4e00-\u9fa5_a-zA-Z0-9_]{2,16}$/
	if(reg.test(username)){
		var flag = false;
		$.ajax({
			async:false,
			type:"POST",
			url:CONTEXT_PATH + "/check/username",
			data:"username="+username,
			dataType:"json",
			complete:function(result){
				if (!eval("(" + result.responseText + ")")){
					$("#username").addClass("is-invalid");
					$("#invalid-username").text("该用户名已被使用");
					flag = true;
				}
			}
		})
		if(flag){
			return false;
		}else{
			return true;
		}
	}else{
		$("#username").addClass("is-invalid");
		if(username.length == 0 || username == ""){
			$("#invalid-username").text("用户名不能为空");
		}else if(!reg.test(username)){
			$("#invalid-username").text("用户名长度应在2到16个字符之间");
		}
		return false;
	}
}

function check_password() {
	var pwd = $("#password").val();
	//校验密码
	var reg = /^\w{6,20}$/;
	if(reg.test(pwd)){
		return true;
	}else{
		$("#password").addClass("is-invalid");
		if(pwd.length == 0 || pwd == ""){
			$("#invalid-password").text("密码不能为空");
		}else if(!reg.test(pwd)){
			$("#invalid-password").text("密码应包含6到20个数字或字母");
		}
		return false;
	}
}

function check_confirm() {
	var pwd1 = $("#password").val();
	var pwd2 = $("#confirm-password").val();
	if(pwd2.length == 0 || pwd2 == ""){
		$("#confirm-password").addClass("is-invalid");
		$("#invalid-confirm").text("确认密码不能为空");
	}else if(pwd1 != pwd2) {
		$("#confirm-password").addClass("is-invalid");
		$("#invalid-confirm").text("两次输入密码不一致");
		return false;
	}
	return true;
}

function check_data() {
	var f1, f2, f3, f4;
	f1 = check_email();
	f2 = check_username();
	f3 = check_password();
	f4 = check_confirm();
	if(f1 && f2 && f3 && f4){
		return true;
	}else{
		return false;
	}
}
function clear_error() {
	$(this).removeClass("is-invalid");
}