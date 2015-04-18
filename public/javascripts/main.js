/**
 * Created by tomas on 18-04-15.
 */

$(function(){
    var video = $("video")[0];

    if (video !== undefined) {
        var updateTime = function () {
            console.log(video.currentTime);

            $.ajax({
                url: "/watch?" + $.param({
                    movieId: $(video).attr("data-id"),
                    state: "playing",
                    offset: video.currentTime
                }),
                method: "post"
            });

        };
        video.ontimeupdate = _.throttle(updateTime, 5000);
    }
});