updateTime = ->
  $.ajax
    url: '/watch?' + $.param(
      movieId: $(this).attr('data-id')
      state: 'playing'
      offset: this.currentTime
      token: $(this).attr('data-token'))
    method: 'post'

searchVideo = ->
  $searchResult = $('#search-result')
  query = $(this).val()

  console.log query

  if query.length > 0
    $searchResult?.show()
    $.getJSON '/search/movie?' + $.param(q: query), (res) ->
      res?.forEach (el) ->
        console.log(el)
  else
    $searchResult?.hide()

$ ->
  # searching
  $search = $('#search')
  $search?.keyup _.throttle searchVideo.bind($search), 1500

  # video
  video = $('video')?[0]
  video?.ontimeupdate = _.throttle updateTime.bind(video), 5000