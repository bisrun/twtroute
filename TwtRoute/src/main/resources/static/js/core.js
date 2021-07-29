"use strict";

const INIT_COORD = 'init_coord';
const INIT_ZOOM = 'init_zoom';
const LAST_RESPONSE_DATA = 'last_response_data';

const map_div = document.querySelector('#map_div');
const fileInputTSPTW = document.querySelector('#fileInputTSPTW');
const btnClear = document.querySelector('#btnClear');
const txtCoords = document.querySelector('#txtCoords');

function clearMarker(markers) {
    for (let idx = 0; idx < markers.length; idx++) {
        let marker = markers[idx];
        marker.erase();
    }
    markers = [];
}

function clearPolyline(polylines) {
    for (let idx = 0; idx < polylines.length; idx++) {
        let polyline = polylines[idx];
        polyline.erase();
    }
    polylines = [];
}

function saveCurMapOption(atlanMap) {
    const curCenter = atlanMap.getCenter();
    localStorage.setItem(INIT_COORD, JSON.stringify(curCenter));
    const curZoom = atlanMap.getZoom();
    localStorage.setItem(INIT_ZOOM, curZoom);
}

function eventOnDragEnd(event, atlanMap) {
    saveCurMapOption(atlanMap);
}

function eventOnZoomChanged(event, atlanMap) {
    saveCurMapOption(atlanMap);
}

function initAtlanMap() {
    let map_options = {
        // default option
        // 서울 시청
		center : new atlan.maps.UTMK(953933.75, 1952050.75),
		zoom : 8
	};

    const coord = localStorage.getItem(INIT_COORD);
    if (coord != null) {
        const lonlat = JSON.parse(coord);
        map_options.center = new atlan.maps.UTMK(lonlat.x, lonlat.y);
    }
    const zoom = localStorage.getItem(INIT_ZOOM);
    if (zoom != null) {
        map_options.zoom = zoom;
    }

    const map = new atlan.maps.Map(map_div, map_options);
    map.onEvent('dragend', (e => eventOnDragEnd(e, map)));
    map.onEvent('zoom_changed', (e => eventOnZoomChanged(e, map)));

    return map;
}

function tsptwResponse(response) {
    // 예외 처리 필요.
    if (response.status !== 200) {
        throw response.status;
    }
    return response.json();
}

function drawingMarker(coord, poi, other, map) {
    let marker = new atlan.maps.overlay.Marker({
		position : new atlan.maps.LatLng(coord.y, coord.x),
        content: {
            text : other,
            color: '#000000',
            size : 16
        },
		/*icon : {
		    //url : 'http://map.ecn.cdn.ofs.kr/images_20160226/img/marker.png',
		    size : new atlan.maps.Size(28, 40)
		},*/
		map : map,
		title : poi
	});
    return marker;
}

function writeInfoToMarker(marker, infos, map) {
    const poi = infos.loc_name;
    const arrival_time = infos.arrival_time;
    const end_time = infos.end_time;
    const distance = infos.distance;
    const duration = infos.duration;
    const taskOrder = infos.task_order;
    const taskId = infos.task_id;
    //...

    let infoContent = `<h3>Visit order: ${taskOrder} - ${poi}</h3>`
        + `<h3>Input task id: ${taskId}</h3>`
		+ '<dl class="basic-box">'
		+   `<dt class="addr-road">`
        +       `<span class="ad-label">이전 위치에서 출발 시간</span> <span>${SecToHHMMSS(arrival_time)}</span>`
        +       `<br>`
        +       `<span class="ad-label">현재 위치에 도착 한 시간</span> <span>${SecToHHMMSS(end_time)}</span>`
        +   `</dt>`
		+   `<dd class="addr">`
        //+       `<span class="ad-label">출발 시간</span> <span>${SecToHHMMSS(arrival_time)}</span>`
        +   `</dd>`
		+ '</dl>'
		+ '<div class="addr-footer">'
		//+ '<p class="addr-tel"><span>02) 120</span></p>'
		//+ '<p class="btn-set"><a class="btn-style move-start" href="#" data-adcr="to_start">출발</a><a class="btn-style move-end" href="#" data-adcr="to_start">도착</a></p>'
		+ '</div>'
		;

    const infoWindow = new atlan.maps.overlay.InfoWindow({
        content: infoContent
    });

    marker.onEvent('click', function(){
        infoWindow.open(map, marker);
    });
}

function drawingPolyline(coords, map, color) {
    let latLngArray = [];

    for (let idx = 0; idx < coords.length; idx++) {
        const coord = coords[idx];
        const x = coord[0];
        const y = coord[1];
        let p = new atlan.maps.LatLng(y, x);

        latLngArray.push(p);
    }

    const newPath = new atlan.maps.Path(latLngArray);

    let line = new atlan.maps.vector.Polyline({
        map: map,
        path: newPath,
        strokeColor: color, //getRandomColor(), // css 색상
        strokeWeight: 5,
        strokeLinecap: 'square',
        strokeOpacity: 0.8
    });

    line.onEvent('click', function() {
        //console.log("CLICK CLICK");
    })
    return line;
}

function drawingTSPTWResponseJSON(response, atlanMap, markers, polylines) {
    let activities = null;
    let panToCoord;
    try {
        activities = checkTSPTWResponseJSON(response);
    } catch (err) {
        throw err;
    }

    localStorage.setItem(LAST_RESPONSE_DATA, JSON.stringify(response));
    for (let idx = 0; idx < activities.length; idx++) {
        const activity = activities[idx];
        const coord = activity['loc_coord'];

        panToCoord = {x: coord[0], y: coord[1]};

        const marker = drawingMarker(panToCoord, activity['loc_name'], idx, atlanMap);
        markers.push(marker);
        writeInfoToMarker(markers[idx], activity, atlanMap);

        const geometry = activity['geometry'];
        if (geometry === undefined) {
            continue
        }

        const polyline = drawingPolyline(geometry['coordinates'], atlanMap, getRandomColor());
        polylines.push(polyline);
    }
    atlanMap.panTo(new atlan.maps.LatLng(panToCoord.y, panToCoord.x));
}

function finishedTSPTWInputJson(event, atlanMap, markers, polylines) {
    const IP = '192.168.6.45';
    const Port = 8088;
    const auth_id = '00x0000x0000';
    const device_id = 'dev001001';

    // http://${IP}:${Port}
    const PostUrl = `/route/v1/tsptw/request?auth_id=${auth_id}&device_id=${device_id}`;
    const data = event.target.result;

    fetch(PostUrl, {
        method: 'POST',
        body: data
    })
    .then(response => checkTSPTWResponse(response))
    .then(result => drawingTSPTWResponseJSON(result, atlanMap, markers, polylines))
    .catch(err => alert(`Fail to request by [${err}]\nCheck your console log`));
}

function changeTSPTWInputFileEvent(event, atlanMap, markers, polylines) {
    event.preventDefault();

    clearMarker(markers);
    clearPolyline(polylines);

    const file = document.querySelector(`#${event.srcElement.id}`);
    console.info(`TSPTW input file is ${file.files[0].name}`);
    console.info(`File size: ${file.files[0].size}`);

    const reader = new FileReader();
    reader.onload = (e => finishedTSPTWInputJson(e, atlanMap, markers, polylines));
    reader.readAsText(file.files[0]);
    // reset
    file.value = '';
}

function clearBtnEvent(event, markers, polylines) {
    if (markers.length === 0) {
        alert("No marker");
    } else if (polylines.length === 0) {
        alert("No line");
    } else {
        clearMarker(markers);
        clearPolyline(polylines);
    }
}

function coordsTxtPressed(event, atlanMap, polylines) {
    if (event.keyCode == 13) {
        console.log(txtCoords.value);
        clearPolyline(polylines);

        const url1 = `http://192.168.6.45:20000/route/v1/driving/${txtCoords.value}?geometries=geojson&overview=full`;
        console.log(url1);
        fetch(url1)
        .then(response => response.json())
        .then(response => {
            const coords = response['routes'][0]['geometry']['coordinates'];
            const polyline = drawingPolyline(coords, atlanMap, 'blue');
            polylines.push(polyline);
        })
        .catch(err => console.log(err))

        const url2 = `http://192.168.6.45:5400/route/v1/driving/${txtCoords.value}`;
        console.log(url2);
        fetch(url2)
        .then(response => response.json())
        .then(response => {
            const coords = response['route_summary'][0]['geometry']['coordinates'];
            const polyline = drawingPolyline(coords, atlanMap, 'red');
            polylines.push(polyline);
        })
        .catch(err => console.log(err))
    }
}

document.addEventListener("DOMContentLoaded", function(){
    let markers = [];
    let polylines = [];
    const atlanMap = initAtlanMap();
    fileInputTSPTW.addEventListener('change', event => changeTSPTWInputFileEvent(event, atlanMap, markers, polylines));
    btnClear.addEventListener('click', event => clearBtnEvent(event, markers, polylines));
    txtCoords.addEventListener('keydown', event => coordsTxtPressed(event, atlanMap, polylines));
})
