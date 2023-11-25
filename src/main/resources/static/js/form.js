function _(el) {
    return document.getElementById(el);
}

function uploadFile(event) {
    event.preventDefault()
    const file = _("file1").files[0];
    if (file === undefined) {
        console.log("No file")
        noFileHandler()
        return
    }
    const formData = new FormData();
    formData.append("file1", file);
    const ajax = new XMLHttpRequest();
    ajax.upload.addEventListener("progress", progressHandler, false);
    ajax.addEventListener("load", completeHandler, false);
    ajax.addEventListener("error", errorHandler, false);
    ajax.addEventListener("abort", abortHandler, false);
    ajax.open("POST", `/upload?filename=${file.name}`);
    ajax.send(formData);
}

function progressHandler(event) {
    _("loaded_n_total").innerHTML = "Uploaded " + event.loaded + " bytes of " + event.total;
    var percent = (event.loaded / event.total) * 100;
    _("progressBar").value = Math.round(percent);
    _("status").innerHTML = Math.round(percent) + "% uploaded... please wait";
}

function completeHandler(event) {
    _("status").innerHTML = `Success: ${event.target.responseText}`;
    _("progressBar").value = 0;
}

function errorHandler(event) {
    _("status").innerHTML = "Upload Failed";
}

function noFileHandler(event) {
    _("status").innerHTML = "Choose file";
}

function abortHandler(event) {
    _("status").innerHTML = "Upload Aborted";
}

_("upload_form").addEventListener("submit", (event) => uploadFile(event));
