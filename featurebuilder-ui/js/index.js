
//jQuery time
var current_fs, next_fs, previous_fs; //fieldsets
var left, opacity, scale; //fieldset properties which we will animate
var animating; //flag to prevent quick multi-click glitches

$(".next").click(function(){
	current_fs = $(this).parent();
	next_fs = $(this).parent().next();

    console.log("NEXT " + next_fs[0].name)

    document.getElementById("error").style.display="none";
    document.getElementById("error").innerHTML = "";

    if (current_fs[0].name == "account") {
        //activate next step on progressbar using the index of next_fs
        $("#progressbar li").eq($("fieldset").index(next_fs)).addClass("active");

        next_fs.show();
        current_fs.hide();
    }
    else if (current_fs[0].name == "feature") {
        //activate next step on progressbar using the index of next_fs
        $("#progressbar li").eq($("fieldset").index(next_fs)).addClass("active");

        if(document.getElementById("add").checked) {
            add("Implement the feature", "Choose the button title", next_fs[0], ["Add new character", "Add something", "Add"]);

            next_fs.show();

            current_fs.hide();
        }
        else if(document.getElementById("slay").checked) {
            console.log("Implementing Slay feature");

            add("Implement the feature", "Choose the button label", next_fs[0], ["Hug", "Kiss", "Kill"]);

            next_fs.show();

            current_fs.hide();
        }
        else if(document.getElementById("search").checked) {
            console.log("Implementing Search feature");

            add("Implement the feature", "Choose the input name", next_fs[0], ["box", "keywords", "whatever"]);

            next_fs.show();

            current_fs.hide();
        }
        else {
            document.getElementById("error").style.display="block";
            document.getElementById("error").innerHTML += "Select a valid feature";
        }
    }

    //show the next fieldset
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
        error: function (msg) {
            console.log("ERROR " + msg.responseText)
            document.getElementById("error").style.display="block";
            document.getElementById("error").innerHTML += msg.responseText;
        }
    });
})

function poll(buildId, timeout, interval) {
    var endTime = Number(new Date()) + (timeout || 2000);
    interval = interval || 100;

    fetch("https://api-featurebuilder.wedeploy.io/build/" + buildId, {method: 'get'})
        .then(response => response.json())
        .then(data => {

            console.log("LOG " + data.logs);

            var textareas = document.getElementsByTagName("textarea");

            textareas[0].value = "Creating pull request " + data.logs.join("\n");

            if (data.finished) {

                console.log("FINISHED!!");

                console.log("FINISHED!!" + data.finishStatus);

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
        .catch(error => {
            console.log("ERROR " + error);
            document.getElementById("error").style.display="block";
            document.getElementById("error").innerHTML += error;
        });
}

function add(title, subtitle, parent, options) {
    console.log("Implementing Add feature");

    var oldH2 = parent.getElementsByTagName('h2')[0];

    if (oldH2 != null) {
        parent.removeChild(oldH2);
    }

    var oldH3 =parent.getElementsByTagName('h3')[0];

    if (oldH3 != null) {
        parent.removeChild(oldH3);
    }

    var oldDiv =parent.getElementsByTagName('div')[0];

    if (oldDiv != null) {
        parent.removeChild(oldDiv);
    }

    var first = parent.firstChild;

    var h2 = document.createElement('h2');

    h2.className="fs-title";

    h2.innerHTML = title;

    parent.insertBefore(h2, first);

    var h3 = document.createElement('h3');

    h3.className="fs-title";

    h3.innerHTML=subtitle;

    parent.insertBefore(h3, first);

    var div = document.createElement('div');
    div.className="box";

    parent.insertBefore(div, first);

    var cont = 1;

    options.forEach(function(element) {
        var radio = document.createElement('input');
        radio.value=cont;
        radio.id="devOptionId" + cont;
        radio.name="devOptionId";
        radio.type="radio";

        div.appendChild(radio);

        var label = document.createElement('label');
        label.htmlFor="devOptionId" + cont;
        label.innerHTML = element;

        div.appendChild(label);

        cont++;
    });
}

$(function () {
    $('[data-toggle="popover"]').popover()
})

// Add custom JS here
$('a[rel=popover]').popover({
    html: true,
    trigger: 'click',
    placement: 'bottom',
    content: function(){return '<img src="'+$(this).data('img') + '" />';}
});