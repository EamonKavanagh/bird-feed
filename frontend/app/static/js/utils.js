function parse(val) {
    var result = "Not found",
        tmp = [];
    location.search
    .substr(1)
        .split("&")
        .forEach(function (item) {
        tmp = item.split("=");
        if (tmp[0] === val) result = decodeURIComponent(tmp[1]);
    });
    return result.replace("+", " ");
}

function displayNearMe(event, circle) {
    $.get(
        'track/_near_me',
        {
            'family'  : family,
            'name': name,
            'location': event.latLng.lat() + ',' + event.latLng.lng(),
            'distance': circle.getRadius()
        },
        function(data) {
            var nearMe = $('#near-me')
            nearMe.empty()
            for (i = 0; i < data.result.length; i++) {
                var bird = $('<div class="bird">')
                var img = $('<img>')
                img.attr('src', '/static/birds/' + data.result[i][2])
                bird.append(data.result[i][0] + ': ' + data.result[i][1])
                bird.append('<br>')
                bird.append(img)
                nearMe.append(bird)
            }
        }
    );
}

function displayTrending() {
    $.get(
        'track/_trending',
        {
            'family': family
        },
        function(data) {
            var trending = $('#trending')
            trending.empty()
            for (i = 0; i < data.result.length; i++) {
                var bird = $('<div class="bird">')
                var img = $('<img>')
                img.attr('src', '/static/birds/' + data.result[i][2])
                bird.append(data.result[i][0] + ': ' + data.result[i][1])
                bird.append('<br>')
                bird.append(img)
                trending.append(bird)
            }
        }
    );
}

function displayHotspots() {
    var hotspots;
    $.get(
        'track/_hotspots',
        {
            'family': family,
            'name'  : name
        },
        function(data) {
            hotspots = data.result;
            for (i = 0; i < hotspots.length; i++) {
                var midLat = hotspots[i][0];
                var midLng = hotspots[i][1];
                var SW = new google.maps.LatLng(midLat-.0009, midLng-.0011);
                var NE = new google.maps.LatLng(midLat+.0009, midLng+.0011);
                var newRect = new google.maps.LatLngBounds(SW, NE);
                rects[i].setBounds(newRect);
            }
        }
    );
}