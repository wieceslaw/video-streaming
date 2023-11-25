function _(el) {
    return document.getElementById(el);
}

const player = videojs("video-id")
player.reset()
const params = new URLSearchParams(document.location.search)
const videoId = params.get("video-id")
if (videoId !== null) {
    const leftTime = localStorage.getItem(videoId)
    if (leftTime !== null) {
        console.log("left time:", leftTime)
        player.currentTime(parseInt(leftTime))
    }
    console.log("after time:", player.currentTime())
    player.src({type: 'video/mp4', src: `/download/${videoId}`})
}

player.on('timeupdate', function () {
    const currentTime = player.currentTime();
    console.log("Current time: ", currentTime)
    if (videoId) {
        localStorage.setItem(videoId, currentTime)
    }
});
