var CONTEXT_PATH = "/recommend";
function follow(btn, entityType, entityId) {
	$.post(
		CONTEXT_PATH + "/follow",
		{"entityType":entityType, "entityId":entityId},
		function(data){
			data = $.parseJSON(data);
			if(data.code == 0){
				if(data.hasFollowed){
					$(btn).addClass("follow");
					$(btn).text("已关注")
				}else{
					$(btn).removeClass("follow");
					$(btn).text("关注")
				}
				$(btn).parent().parent().parent().find(".follower-count").text(data.followerCount);
				// $("#follower-count").text(data.followerCount);
			}else{
				alert(data.msg);
			}
		}
	);
}
$(document).on('click', '.pagination a', function(){
	var current = $(this).attr('pageIndex');
	var path = $(this).parents("nav").parent().attr('path');
	var data_div = $(this).parents("nav").parent().parent().parent().parent();
	$.ajax({
		type:'get',
		url: CONTEXT_PATH + path,
		dataType:'html',
		contentType : "application/json",
		data: {"current": current, "async":true},
		success: function (data) {
			$(data_div).html(data);
			$(document).scrollTop($(data_div).offset().top - 120);
			console.log(data);
		},
		error: function () {
			alert("服务器错误");
		}
	});
});

//切换
$(document).on('click', '.head-list a', function () {
	$(".collapse.show").removeClass("show");
	var data_div = $(this).attr("bond");
	$(data_div).addClass("show");
	$(".head-list a.active").removeClass("active");
	$(this).addClass("active");
});