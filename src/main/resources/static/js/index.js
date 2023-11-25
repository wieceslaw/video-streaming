function _(el) {
    return document.getElementById(el);
}

async function fetchData() {
    try {
        const response = await fetch("/videos", {
            method: "GET",
        })
        const result = await response.json()
        const list = _("list-id")
        for (let video of result) {
            let li = document.createElement('li');
            let a = document.createElement('a');
            a.innerHTML = video.name
            a.href = `/view?video-id=${video.id}`
            li.append(a)
            list.append(li)
        }
        console.log("Success:", result)
    } catch (error) {
        console.error("Error:", error)
    }
}

fetchData()
