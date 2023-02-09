import mapboxgl from '!mapbox-gl'; // eslint-disable-line import/no-webpack-loader-syntax
import {lineString, featureCollection, point} from '@turf/helpers'

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
                'line-color': '#fa0feb',
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

    updateFilters(filters) {
        if (this.map.getLayer('tiles')) {
            const arr = ['all'];
            filters.forEach(f => arr.push(f));
            this.map.setFilter('tiles', arr);
            this.map.setPaintProperty('tiles', 'line-color', '#f60808');

            const arr2 = ['any'];
            filters.forEach(f => arr2.push(f));
            this.map.setFilter('tiles_zeroes', ['!', arr2]);
        }
    }

    removeTiles() {
        if (this.map.getLayer('tiles')) this.map.removeLayer('tiles');
        if (this.map.getLayer('tiles_zeroes')) this.map.removeLayer('tiles_zeroes');
    }

    updateTiles(value) {
        if (!this.map.getLayer('tiles')) {
            this.addTiles(value);
        } else {
            this.map.setPaintProperty('tiles', 'line-color', paint(value));
            this.map.setFilter('tiles', ['>', ['get', value], 0]);
            this.map.setFilter('tiles_zeroes', ['==', ['get', value], 0]);
        }
    }

    addTiles(value='score') {
        if (!this.map.getSource('tiles')) {
            this.map.addSource('tiles', {
                type: 'vector',
                url: 'mapbox://ale-delmastro.green'
            });
        }

        if (!this.map.getLayer('tiles')) {
            this.map.addLayer({
                'id': 'tiles',
                'type': 'line',
                'source': 'tiles',
                'source-layer': 'street',
                'layout': {
                    'line-join': 'round',
                    'line-cap': 'round',
                },
                'paint': {
                    'line-color': paint(value),
                    'line-opacity': 0.4,
                    'line-width': 6,
                },
                filter: ['>', ['get', value], 0]
            });
        }

        if (!this.map.getLayer('tiles_zeroes')) {
            this.map.addLayer({
                'id': "tiles_zeroes",
                'type': 'line',
                'source': "tiles",
                'source-layer': 'street',
                'paint': {
                    'line-opacity': 0,
                    'line-width': 6,
                },
                filter: ['==', ['get', value], 0]
            });
        }

        return 'tiles';
    }

    getFirstLast(arr) {
        return [arr[0], arr[arr.length -1]];
    }

    addGeoJson (geojson, score='score') {
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
                    ['<', ['get', 'score'], 0.5], '#ff0000',
                    '#0048ff'
                ],
                'line-opacity': 0.4,
                'line-width': 8,
            },
            minzoom: 14/*,
            maxzoom: 20,
            filter: ['!=', ['get', 'score'], 0]*/
        });

        const boundaries = geojson['features'].map(f => this.getFirstLast(f['geometry']['coordinates'])).reduce((v1, v2) => v1.concat(v2), []).map(p => point(p));
        this.addGeoJsonSource("boundaries");
        this.map.getSource("boundaries").setData(featureCollection(boundaries));

        this.map.addLayer({
            "id": "pointsss",
            "type": "circle",
            "source": "boundaries",
            minzoom: 14,
            /*maxzoom: 20,*/
            /*"layout": {
                "circle-color": '#dc1010'
            }*//*,
            filter: ['!=', ['get', 'score'], 0]*/
        });

        const tmp = GREEN_LAYER+"_2";
        this.addGeoJsonSource(tmp);
        this.map.getSource(tmp).setData(geojson);

        this.map.addLayer({
            "id": "symbols",
            "type": "symbol",
            "source": tmp,
            minzoom: 14,
           /* maxzoom: 20,*/
            "layout": {
                "symbol-placement": "line-center",
                "text-font": ["Open Sans Regular"],
                "text-field": ["get", "osm_id"],
                "text-size": 16
            }/*,
            filter: ['!=', ['get', 'score'], 0]*/
        });
    }


}

function paint(attribute) {
    return [
        'case',
        ['>=', ['get', attribute], 0.5], '#ff0000',
        ['>=', ['get', attribute], 0.3], '#ff8c00',
        ['>=', ['get', attribute], 0.1], '#ffea00',
        '#f2ff7b'
    ]
}

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
