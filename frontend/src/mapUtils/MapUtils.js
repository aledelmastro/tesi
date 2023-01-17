import mapboxgl from '!mapbox-gl'; // eslint-disable-line import/no-webpack-loader-syntax
import {lineString, featureCollection} from '@turf/helpers'

const GREEN_LAYER = "GREEN";

class MapUtils {
    static #isInternalConstructing = false;

    static build(map) {
        this.#isInternalConstructing = true;
        const mu = new MapUtils();
        this.#isInternalConstructing = false;
        mu.map = map;

        return mu;
    }

    constructor() {
        if (!MapUtils.#isInternalConstructing) {
            throw new TypeError("MapUtils is not constructable");
        }
        this.map = null;
    }

    ids = [];

    addGeoJsonSource (name) {
        if (!this.map.getSource(name)) {
            this.map.addSource(name, {
                type: 'geojson',
                data: featureCollection([])
            });
            this.ids.push(name)
        }

        return this.map.getSource(name);
    }

    appendSourceData(source, data) {
        const features = source._data.features;

        features.push(data);
        source.setData(source._data);
    }

    addLayer (sourceName, type, style={}) {
        if (this.map.getLayer(sourceName)) this.map.removeLayer(sourceName);

        this.map.addLayer({
            id: sourceName,
            type: type,
            source: sourceName,
            layout: {
                'line-join': 'round',
                'line-cap': 'round'
            },
            paint: {
                'line-color': '#52B780',
                'line-opacity': 0.4,
                'line-width': 8
            }
        });
    }

    addLineLayer (sourceName) {
        this.addLayer(sourceName, 'line')
    }

    addItinerary(points, name) {
        const source = this.addGeoJsonSource(name);
        this.addLineLayer(name);
        source.setData(lineString(points));

        this.fitBounds(points.map(p => p[0]),points.map(p => p[1]));
    }

    removeItinerary(name) {
        if (this.map.getLayer(name)) this.map.removeLayer(name);
        if (this.map.getSource(name)) this.map.removeSource(name);
        this.ids = this.ids.filter(value => value !== name);
    }

    clear() {
        this.ids.forEach(id => {
            this.map.removeLayer(id);
            this.map.removeSource(id);
        });
        this.ids = [];
    }

    fitBounds(lngs, lats) {
        let coeff = 0.0001;
        const minLng = Math.min(...lngs);
        const minLat = Math.min(...lngs);
        const maxLng = Math.min(...lats);
        const maxLat = Math.max(...lats);

        coeff = coeff*Math.hypot(maxLng - minLng, maxLat - minLat);
        const sw = [Math.min(...lngs)-coeff, Math.min(...lats)-coeff]
        const ne = [Math.max(...lngs)+coeff, Math.max(...lats)+coeff]

        this.map.fitBounds([sw, ne]);
    }

    addTiles() {
        const sName = 'tiles';
        this.map.addSource(sName, {
            type: 'vector',
            url: 'mapbox://ale-delmastro.green_v3'
        });

        this.map.addLayer({
            'id': sName,
            'type': 'line',
            'source': sName,
            'source-layer': 'street',
            'layout': {
                'line-join': 'round',
                'line-cap': 'round',
            },
            'paint': {
                'line-color': paint('score'),
                'line-opacity': 0.4,
                'line-width': 8,
            },
        });

        return sName;
    }

    addGeoJson (geojson) {
        const features = geojson['features'];
        this.addGeoJsonSource(GREEN_LAYER);
        //this.addLineLayer(GREEN_LAYER);
        this.map.getSource(GREEN_LAYER).setData(geojson);

        if (this.map.getLayer(GREEN_LAYER)) return;

        this.map.addLayer({
            id: GREEN_LAYER,
            type: 'line',
            source: GREEN_LAYER,
            layout: {
                'line-join': 'round',
                'line-cap': 'round'
            },
            'paint': {
                'line-color': [
                    'case',
                    ['!', ['has', 'score']], '#F6B829',
                    /*['==', ['get', 'score'], 0], '#efefef',*/
                    ['>=', ['get', 'score'], 0.5], '#43ff64',
                    ['<', ['get', 'score'], 0.5], '#ff0000'
                ],
                'line-opacity': 0.4,
                'line-width': 8,
            },
            minzoom: 14,
            maxzoom: 16,
            filter: ['!=', ['get', 'score'], 0]
        });

        this.map.addLayer({
            "id": "symbols",
            "type": "symbol",
            "source": GREEN_LAYER,
            minzoom: 14,
            maxzoom: 16,
            "layout": {
                "symbol-placement": "line",
                "text-font": ["Open Sans Regular"],
                "text-field": '{score}',
                "text-size": 16
            },
            filter: ['!=', ['get', 'score'], 0]
        });
    }


}

function paint(attribute) {
    return [
        'case',
        ['!', ['has', attribute]], '#F6B829',
        /*['==', ['get', 'score'], 0], '#efefef',*/
        ['>=', ['get', attribute], 0.5], '#FE1EF0',
        '#fefefe'
    ]
}
const paint1 = {
    'line-color': [
        'case',
        ['!', ['has', 'score']], '#F6B829',
        /*['==', ['get', 'score'], 0], '#efefef',*/
        ['>=', ['get', 'score'], 20], '#43ff64',
        ['<=', ['get', 'score'], 5], '#00efff',
        /* other */ '#ff0000'
    ],
    'line-opacity': 0.4,
    'line-width': 8,
}

const paint2 = [
            'case',
            /*['==', ['get', 'score'], 0], '#efefef',*/
            ['<=', ['get', 'surrounded_other_green'], 0.5], '#43ff64',
            ['>', ['get', 'surrounded_other_green'], 0.5], '#00efff',
            /* other */ '#ff0000'
        ];

function decodePolyline(polyline) {

    let currentPosition = 0;

    let currentLat = 0;
    let currentLng = 0;

    const dataLength = polyline.length;

    const polylineLatLngs = [];

    while (currentPosition < dataLength) {

        let shift = 0;
        let result = 0;

        let byte;

        do {
            byte = polyline.charCodeAt(currentPosition++) - 63;
            result |= (byte & 0x1f) << shift;
            shift += 5;
        } while (byte >= 0x20);

        var deltaLat = ((result & 1) ? ~(result >> 1) : (result >> 1));
        currentLat += deltaLat;

        shift = 0;
        result = 0;

        do {
            byte = polyline.charCodeAt(currentPosition++) - 63;
            result |= (byte & 0x1f) << shift;
            shift += 5;
        } while (byte >= 0x20);

        var deltLng = ((result & 1) ? ~(result >> 1) : (result >> 1));

        currentLng += deltLng;

        polylineLatLngs.push({lat: currentLat * 0.00001, lng: currentLng * 0.00001});
    }

    return polylineLatLngs;
}

export {MapUtils, decodePolyline, paint};






/*function cutData(data, bounds) {
    const box = bboxPolygon([bounds.getWest().toFixed(5), bounds.getNorth().toFixed(5), bounds.getEast().toFixed(5), bounds.getSouth().toFixed(5)]);
    //data["features"].forEach(f => f["geometry"].coordinates.forEach(v => [v[0].toFixed(5), v[1].toFixed(5)]));

    const t1 = new Date();
    const t = data["features"].filter(f => {
        /!*const ls = lineString(f["geometry"].coordinates.map(v => [v[0].toFixed(5), v[1].toFixed(5)]));
        const env = envelope(ls);
*!/
        //const i = intersect(env.geometry, box.geometry);
        const i = intersect(box.geometry, box.geometry);
        return i !== null;
    });
    const t2 = new Date();
    console.log((t2-t1)/1000);

    console.log(t.length);
    return featureCollection(t);
}*/
