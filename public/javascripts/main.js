/**
 * Created by tomas on 18-04-15.
 */

$(function(){
    var video = $("video")[0];
    if (video !== undefined) {
        video.ontimeupdate = function () {
            console.log(video.currentTime);
        }
    }
});