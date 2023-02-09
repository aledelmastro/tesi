import './App.css';
import React, {useRef, useEffect, useState} from 'react';
import mapboxgl from '!mapbox-gl'; // eslint-disable-line import/no-webpack-loader-syntax
import IOTile from "./components/IOTile";
import {MapUtils} from "./mapUtils/MapUtils";
import ContextMenu from "./components/ContextMenu";
import {requestInspectorModes} from "./calls/calls";
import FiltersPanel from "./components/FiltersPanel";
import {options} from "axios";

mapboxgl.accessToken = 'pk.eyJ1IjoiYWxlLWRlbG1hc3RybyIsImEiOiJjbGFzOWZnOWoyMGY3M3BxdjE1d29lcnV4In0.0X1pCcxD7RtDiAOc_XFjwQ';

function App() {
    const mapContainer = useRef(null);
    const map = useRef(null);
    const mapUtils = useRef(null);

    const [popup, setPopup] = useState({x: 10, y: 10, lng: 7.681642, lat: 45.0728662, visible: false, xInfo: 0, yInfo: 0, showInfo: false});
    const [lng, setLng] = useState(7.681642);
    const [lat, setLat] = useState(45.0728662);
    const [zoom, setZoom] = useState(14);
    const fromMarker = useRef(null);
    const toMarker = useRef(null);

    const [from, setFrom] = useState('');
    const [to, setTo] = useState('');

    const [info, setInfo] = useState([]);

    const itinsDisplayed = useRef([]);
    const [scoresAndFeatures, setScoresAndFeatures] = useState({scores: [], features: []});
    const [layer, setLayer] = useState("");
    const [selectorDisabled, setSelectorDisabled] = useState(true);

    function setMarker(marker, lngLat) {
        if (marker.current === null)
            marker.current = new mapboxgl.Marker({color: 'red'});
        else
            marker.current.remove();

        marker.current.setLngLat(lngLat)
            .addTo(map.current);
    }

    function updateFromTo(from) {
        const lngLat = [popup.lng, popup.lat];
        const val = popup.lat.toFixed(4) + ',' + popup.lng.toFixed(4);
        if (from) {
            setMarker(fromMarker, lngLat);
            setFrom(val);
        } else {
            setMarker(toMarker, lngLat);
            setTo(val);
        }
        clearStreet();
        hideContextMenu(false);
    }

    function clear() {
        clearStreet();
        if (fromMarker.current !== null)
            fromMarker.current.remove();
        if (toMarker.current !== null)
            toMarker.current.remove();
    }

    function clearStreet() {
        if (!mapUtils.current) return;
        mapUtils.current.clear();
    }

    function plotResult(points, id, collapsed) {
        if (!mapUtils.current) return;
        mapUtils.current.addItinerary(points, "itin");
        itinsDisplayed.current.push("itin");
        setMarker(fromMarker, points[0]);
        setMarker(toMarker, points[points.length - 1]);
    }

    useEffect(() => {
        if (map.current) return; // initialize map only once
        map.current = new mapboxgl.Map({
            container: mapContainer.current,
            style: 'mapbox://styles/mapbox/streets-v12',
            /*style: 'mapbox://styles/mapbox/satellite-v9',*/
            center: [lng, lat],
            zoom: zoom,
            dragRotate: false
        });

        requestInspectorModes().then(res => {
            setScoresAndFeatures({
                scores: res.scores,
                features: res.features
            });
        })
            .catch(e => console.error("NON FAREMO GLI STESSI ERRORI DELLA TELEFUNKEN"));
    });


    useEffect(() => {
        if (!map.current) return; // wait for map to initialize

        const onLoad = () => {
            mapUtils.current = MapUtils.build(map.current);
            /*mapUtils.current.addTiles();*/
                  /*const l = map.current.getSource("tiles");*/

            setSelectorDisabled(false);
        }

        const onContext = (e) => {
            e.preventDefault();
            setPopup({
                // position referred to the top-right corner of the screen
                x: e.originalEvent.x,
                y: e.originalEvent.y,
                // position referred to the top-right corner of the map
                xInfo: e.point.x,
                yInfo: e.point.y,
                // coordinates
                lng: e.lngLat.lng,
                lat: e.lngLat.lat,
                showInfo: map.current.getZoom() > 17,
                visible: true
            });
        };

        const onClick = () => {
            hideContextMenu();
        }

        const onDrag = () => {
            hideContextMenu();
        }

        map.current.on('load', onLoad);
        map.current.on('contextmenu', onContext);
        map.current.on('click', onClick);
        map.current.on('drag', onDrag);

        return () => {
            if (map.current !== null) {
                map.current.off('contextmenu', onContext);
                map.current.off('click', onClick);
                map.current.off('load', onLoad);
                map.current.off('drag', onDrag);
            }
        }
    });

    const hideContextMenu = () => {
        if (popup.visible)
            setPopup(prevState => {
                return {...prevState, ...{visible: false}};
            });
    }

    const handleMenuInfo = () => {
        const bbox = [
            [popup.xInfo - 2, popup.yInfo - 2],
            [popup.xInfo + 2, popup.yInfo + 2]
        ];
        if (map.current.getLayer('tiles')) {
            const features = map.current.queryRenderedFeatures(bbox, {layers:['tiles', 'tiles_zeroes']});
            if (features.length > 1) {
                features.splice(1, features.length-1);
            }
            setInfo(features.map(f=> f['properties']));
        }
        hideContextMenu();
    }

    const handleLayerChange = (e, v) => {
            if (v.value)
                mapUtils.current.updateTiles(v.value);
            else
                mapUtils.current.removeTiles(v.value);
    };

    const handleLayerClick = (filters) => {
        if (filters)
            mapUtils.current.updateFilters(filters);
    };

    return (
        <div id="container">
            <IOTile
                submit={clearStreet}
                plotResult={plotResult}
                from={from}
                to={to}
                setFrom={setFrom}
                setTo={setTo}
                info={info}
            />
            <ContextMenu
                setTo={() => updateFromTo(false)}
                setFrom={() => updateFromTo(true)}
                getInfo={() => handleMenuInfo()}
                visible={popup.visible}
                left={popup.x}
                top={popup.y}
                showInfo={popup.showInfo}
            />

            <FiltersPanel scores={scoresAndFeatures.scores}
                          features={scoresAndFeatures.features}
                          disabled={selectorDisabled}
                          onChange={handleLayerChange}
                          onClick={handleLayerClick}
            />
            <div ref={mapContainer} className="map-container"/>

        </div>
    );
}

export default App;
