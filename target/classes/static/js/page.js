//动态分页
$(document).on('click', '.pagination a', function(){
    var current = $(this).attr('pageIndex');
    var path = $(this).parents("nav").parent().attr('path');
    var data_div = $(this).parents("nav").parent().attr('bond');
    $.ajax({
        type:'get',
        url: CONTEXT_PATH + path,
        dataType:'html',
        contentType : "application/json",
        data: {"current": current, "async": true},
        success: function (data) {
            $(data_div).html(data);
            $(document).scrollTop($(data_div).offset().top);
        },
        error: function () {
            alert("服务器错误");
        }
    });
});