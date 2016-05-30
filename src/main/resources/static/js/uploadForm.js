$(document).ready(function() {
    $('#i_file').change( function(event) {
    var tmppath = URL.createObjectURL(event.target.files[0]);
        $("#source-img").fadeIn("fast").attr('src',URL.createObjectURL(event.target.files[0]));
    });
});