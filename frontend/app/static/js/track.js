var map;
var marker;
var circle;
var rectHi;
var rectMe;
var rectLo;
var rects;

var family = parse('family');
var name = parse('name');

var refreshRate = 5000;

function initMap() {
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
        displayNearMe(event, circle);
    });
    
    displayHotspots();
    setInterval(function() {
        displayHotspots();
    }, refreshRate);
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