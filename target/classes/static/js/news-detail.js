var CONTEXT_PATH = "/recommend";
function check(fm){
    var box = fm.elements[0];
    var content = box.value;
    if(content.length == 0 || content == "") {
        alert( "评论不能为空")
        return false;
    }else{
        return true;
    }
}

function like(btn, entityType, entityId, entityUserId, newsId){
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "newsId":newsId},
        function(data){
            data = $.parseJSON(data);
            if(data.code == 0){
                $(btn).children("span").text(data.likeCount);
                if(data.likeStatus == 1){
                    $(btn).addClass("btn-active");
                }else{
                    $(btn).removeClass("btn-active");
                }

            }else{
                alert(data.msg);
            }
        }
    )
}

function favorite(btn, newsId) {
    $.post(
        CONTEXT_PATH + "/favorite",
        {"newsId": newsId},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                $(btn).children("span").text(data.favoriteCount);
                if(data.favoriteStatus == true){
                    $(btn).addClass("btn-active");
                }else{
                    $(btn).removeClass("btn-active");
                }
            }else{
                alert(data.msg);
            }
        }
    )
}

function follow(btn, entityType, entityId){
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
                $("#follower-count").text(data.followerCount);
            }else{
                alert(data.msg);
            }
        }
    );
}

//动态分页
$(document).on('click', '.pagination a', function(){
    var current = $(this).attr('pageIndex');
    var path = $(this).parents("nav").parent().attr('path');
    // var data_div = $(this).parents("nav").parent().attr('bond');
    var data_div = $(this).parents("nav").parent().parent().parent();
    var type = $(this).parents("nav").parent().attr('type');
    var commentId = $(this).parents("nav").parent().attr('commentId');
    $.ajax({
        type:'get',
        url: CONTEXT_PATH + path,
        dataType:'html',
        contentType : "application/json",
        data: {"current": current, "type": type, "commentId": commentId},
        success: function (data) {
            $(data_div).html(data);
            $(document).scrollTop($(data_div).offset().top - 140);
            console.log(data);
        },
        error: function () {
            alert("服务器错误");
        }
    });
});