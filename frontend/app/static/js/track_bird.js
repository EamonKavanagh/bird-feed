var map;
var marker;
var circle;
var rectHi;
var rectMe;
var rectLo;
var rects;

var family = parse('family');
var name = parse('name');

function initMap() {
    var refresh = 10000;

    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 40.7829, lng: -73.9654},
        zoom: 15,
        clickableIcons: false,
        streetViewControl: false,
        mapTypeControlOptions : {mapTypeIds: []}
    });
    
    rectHi = constructRectangle('#FF0000');
    rectMe = constructRectangle('#FF9900');
    rectLo = constructRectangle('#FFCC00');
    rects = [rectHi, rectMe, rectLo];
    
    google.maps.event.addListener(map, 'click', function(event) {
        if (marker == null) {
            marker = new google.maps.Marker({
                position: event.latLng,
                map: map,
                title: 'My Location'
            });
            circle = new google.maps.Circle({
                map: map,
                strokeWeight: 0,
                fillColor: '#FF0000',
                fillOpactiy: .35,
                center: event.latLng,
                radius: 50
            });
        } else {
            moveMarker(event.latLng);
            moveCircle(event.latLng);
        }
        $.get(
            'track/_near_me',
            {
                'family'  : family,
                'location': event.latLng.lat() + ',' + event.latLng.lng(),
                'distance': circle.getRadius(),
                'name': name
                
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
    });
    
    displayHotspots();
    setInterval(function() {
        displayHotspots();
    }, refresh);
}

function moveMarker(position) {
    marker.setPosition(position);
}

function moveCircle(position) {
    circle.setCenter(position);
}

function constructRectangle(fill) {
    return new google.maps.Rectangle({
        strokeWeight: 0,
        fillColor: fill,
        fillOpacity: .35,
        clickable: false,
        map: map
    });
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
