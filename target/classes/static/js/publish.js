$(function(){
    $("form").submit(check_data);
});
$('#news-content').summernote({
    lang:'zh-CN',
    placeholder: '请输入正文',
    tabsize: 2,
    height: 300,
    toolbar: [
        ['style', ['style']],
        ['font', ['bold', 'underline', 'clear']],
        ['color', ['color']],
        ['para', ['ul', 'ol', 'paragraph']],
        ['table', ['table']],
        ['insert', ['link', 'picture',]],
        ['view', ['codeview', 'help']]
    ],
    callbacks:{
        onImageUpload: function(files, editor, $editable) {
            UploadFiles(files,insertImg);
        }
    }
});
function insertImg(imageUrl){
    $('#news-content').summernote('editor.insertImage',imageUrl, function($image){
        $image.css('width', '100%');
        $image.css('height', 'auto');
    });
}
function UploadFiles(files,func){
    var file = files[0];
    if(file == null) {
        alert_info("图片不能为空");
        return;
    }
    var supportedTypes = ['image/jpg', 'image/jpeg', 'image/png'];
    if(!(supportedTypes.indexOf(file.type)>-1)){
        alert_info("图片格式错误");
        return
    }
    if(files[0].size > 1024 * 1024){
        alert_info("图片大小不得超过1MB")
        return;
    }
    var formData = new FormData();
    formData.append("file", files[0]);
    $.ajax({
        data: formData,
        type: "POST",
        url: CONTEXT_PATH + "/news/uploadMultipleFile",
        cache: false,
        contentType: false,
        processData: false,
        success: function(imageUrl) {
            insertImg(imageUrl);
        },
        error: function() {
            console.log("uploadError");
        }
    })
}

function check_data() {
    var title = $("#title").val();
    if(title.length == 0|| title == "" ){
        alert_info("标题不能为空");
        return false;
    }
    if($('#news-content').summernote('isEmpty')){
        alert_info("正文不能为空");
        return false;
    }
    return true;
}



function alert_info(message) {
        $(".alert-box").append(
                '<div class="alert alert-danger alert-dismissible fade show" role="alert">'+
                    message +
                '<button type="button" class="close" data-dismiss="alert" aria-label="Close">'+
                '<span aria-hidden="true">&times;</span>'+
                '</button>'+
                '</div>'
            );
        remove_alert();
}
function remove_alert(){
    setTimeout(function(){
        $('[data-dismiss="alert"]').alert('close');
    },  3000);
}