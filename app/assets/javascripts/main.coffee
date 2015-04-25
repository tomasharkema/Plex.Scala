$ ->

    ws = $.websocket(jsRoutes.controllers.MovieController.socket().webSocketURL())

    video = $('video')[0]
    if video != undefined

        updateTime = ->
            console.log video.currentTime
            $.ajax
                url: '/watch?' + $.param(
                    movieId: $(video).attr('data-id')
                    state: 'playing'
                    offset: video.currentTime
                    token: $(video).attr('data-token'))
                method: 'post'


        video.ontimeupdate = _.throttle(updateTime, 5000)