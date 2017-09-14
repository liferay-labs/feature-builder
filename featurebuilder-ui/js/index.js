
//jQuery time
var current_fs, next_fs, previous_fs; //fieldsets
var left, opacity, scale; //fieldset properties which we will animate
var animating; //flag to prevent quick multi-click glitches

$(".next").click(function(){
	current_fs = $(this).parent();
	next_fs = $(this).parent().next();
	
	//activate next step on progressbar using the index of next_fs
	$("#progressbar li").eq($("fieldset").index(next_fs)).addClass("active");

	//show the next fieldset
	next_fs.show();
    current_fs.hide();
});

$(".previous").click(function(){
	current_fs = $(this).parent();
	previous_fs = $(this).parent().prev();
	
	//de-activate current step on progressbar
	$("#progressbar li").eq($("fieldset").index(current_fs)).removeClass("active");
	
	//show the previous fieldset
	current_fs.hide();
    previous_fs.show();
});

$('#msform').submit(function(e){
    //de-activate current step on progressbar

    console.log("Sending data");

    e.preventDefault();

    $.ajax({
        type: "POST",
        async: true,
        url: 'https://api-featurebuilder.wedeploy.io/build',
        data:$('#msform').serialize(),
        success: function (buildId)
        {
            console.log("SUCESS " + buildId)

            var fieldsets = document.getElementsByTagName('fieldset');

            for(var i=0; i<fieldsets.length; i++){
                if(fieldsets[i].id != "logs"){
                    fieldsets[i].style.display="none";
                }
            }

            var textarea = document.getElementById("logs");
            textarea.style.display="block";

            textarea.value = "Creating pull request ";

            poll(buildId, 10000, 500);

        },
        error: function (msg)
        { console.log("ERROR " + msg.responseText)}
    });
})

function poll(buildId, timeout, interval) {
    var endTime = Number(new Date()) + (timeout || 2000);
    interval = interval || 100;

    fetch("https://api-featurebuilder.wedeploy.io/build/" + buildId, {method: 'get'})
        .then(response => response.json())
        .then(data => {
            var textareas = document.getElementsByTagName("textarea");
            textareas[0].value = "Creating pull request " + data.logs.join("\n");
            if (data.finished) {
                console.log("FINISHED!!");

                document.getElementById("logs").style.display="none";

                var links = document.getElementById("link");
                links.style.display="block";

                var pullLink = document.getElementById("pullLink");

                pullLink.href = data.pullRequestURL
                pullLink.text = "Check your pull in Github"

            } else if (Number(new Date()) < endTime) {
                setTimeout(() => {
                    poll(buildId, timeout, interval);
                }, interval);
            }
        })
        .catch(error => { console.log("ERROR " + error) });
}