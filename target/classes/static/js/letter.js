$(function(){
	$("#sendBtn").click(send_letter);
	$("input").focus(clear_error);
});

function send_letter() {
	var toName = $("#recipient-name").val();
	if(toName == "" || toName.length == 0){
		$("#recipient-name").addClass("is-invalid");
		return false;
	}
	var content = $("#message-text").val();
	if(content == "" || content.length == 0){
		$("#message-text").addClass("is-invalid");
		return false;
	}
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName" : toName, "content" : content},
		function (data) {
			data = $.parseJSON(data);
			if(data.code == 0 || data.code == 500){
				$("#sendModal").modal("hide");
				if(data.code == 0) {
					$("#hintBody").text("发送成功");
				}else{
					$("#hintBody").text(data.msg);
				}
				$("#hintModal").modal("show");
				setTimeout(function(){
					$("#hintModal").modal("hide");
					if(data.code == 0){
						location.reload();
					}
				}, 2000);
			}else{
				$("#recipient-name").addClass("is-invalid");
				$("#invalid-target").text(data.msg);
			}
		}
	);
}
function clear_error() {
	$(this).removeClass("is-invalid");
}
