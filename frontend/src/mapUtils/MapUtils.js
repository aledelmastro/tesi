import mapboxgl from '!mapbox-gl'; // eslint-disable-line import/no-webpack-loader-syntax

class MapUtils {
    static #isInternalConstructing = false;

    static build(map) {
        this.#isInternalConstructing = true;
        const mu = new MapUtils();
        this.#isInternalConstructing = false;

        mu.map = map;
        mu.addGeoJsonSource("STREET");
        mu.addLayer("STREET");

        return mu;
    }

    constructor() {
        if (!MapUtils.#isInternalConstructing) {
            throw new TypeError("MapUtils is not constructable");
        }
        this.map = null;
        /*this.startMarker = new mapboxgl.Marker({draggable: true});
        this.endMarker = new mapboxgl.Marker({draggable: true});*/
    }

    findFeature(features, id) {
        return features.filter(f => f.id === id).length > 0;
    }

    setStartMarker(lng, lat) {
        this.startMarker.setLngLat([lng, lat])
    }

    clearStreet() {
        const source = this.map.getSource("STREET");

        source._data.features = [];
        source.setData(source._data);
    }

    addPath(points, id) {
        const source = this.map.getSource("STREET");
        const features = source._data.features;

        if (this.findFeature(features, id)) return;

        features.push(
            {
                'id': id,
                'type': 'Feature',
                'properties': {},
                'geometry': {
                    'type': 'LineString',
                    'coordinates': points
                }
            }
        );
        source.setData(source._data);

        this.fitBounds(points.map(p => p[0]),points.map(p => p[1]));


        return id;
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

    deletePath(id) {
        const source = this.map.getSource("STREET");

        source._data.features = source._data.features.filter(p => p.id !== id);
        source.setData(source._data);
    }

    addPoint(map, lon, lat) {
        const marker1 = new mapboxgl.Marker()
            .setLngLat([lon, lat])
            .addTo(map);
    }

    addGeoPoint(map, lon, lat) {
        this.map.addSource('route', {
            'type': 'geojson',
            'data': {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [lon, lat]
                },
                "properties": {
                    "title": "Mapbox DC",
                    "marker-symbol": "monument"
                }
            }
        });
    }

    addGeoJson (geojson) {
        console.log(geojson['features']);
        if (this.map.getLayer('geo')) this.map.removeLayer('geo');
        if (this.map.getLayer('symbols')) this.map.removeLayer('symbols');
        if (this.map.getSource('geo')) this.map.removeSource("geo");
        this.map.addSource("geo", {
            'type': 'geojson',
            'data': geojson
        });
/*
        this.addLayer("geo");
*/

        this.map.addLayer({
            'id': 'geo',
            'type': 'line',
            'source': 'geo',
            'layout': {
                'line-join': 'round',
                'line-cap': 'round'
            },
            'paint': {
                'line-color': [
                    'case',
                    ['!', ['has', 'score']], '#F6B829',
                    ['==', ['get', 'score'], 0], '#efefef',
                    ['>=', ['get', 'score'], 20], '#43ff64',
                    ['<=', ['get', 'score'], 5], '#00efff',
                    /* other */ '#ff0000'
                ],
                'line-opacity': 0.4,
                'line-width': 8
            }
        });

        this.map.addLayer({
            "id": "symbols",
            "type": "symbol",
            "source": "geo",
            "layout": {
                "symbol-placement": "line",
                "text-font": ["Open Sans Regular"],
                "text-field": '{score}',
                "text-size": 16
            }
        });
    }

    addGeoJsonSource (name) {
        this.map.addSource(name, {
            'type': 'geojson',
            'data': {
                "type": "FeatureCollection",
                "features": []
            }
        });
    }

    addLayer (sourceName) {
        this.map.addLayer({
            'id': sourceName,
            'type': 'line',
            'source': sourceName,
            'layout': {
                'line-join': 'round',
                'line-cap': 'round'
            },
            'paint': {
                'line-color': '#52B780',
                'line-opacity': 0.4,
                'line-width': 8
            }
        });
    }
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

export {MapUtils, decodePolyline};
