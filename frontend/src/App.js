import './App.css';
import React, {useRef, useEffect, useState} from 'react';
import mapboxgl from '!mapbox-gl'; // eslint-disable-line import/no-webpack-loader-syntax
import IOTile from "./components/IOTile";
import {MapUtils} from "./mapUtils/MapUtils";
import ContextMenu from "./components/ContextMenu";
import {requestInspector, requestInspectorAll, requestInspectorModes} from "./calls/calls";
import {Button, Form, FormField, Radio, Card, CardContent, CardHeader} from 'semantic-ui-react'
import {featureCollection, lineString, polygon} from "@turf/helpers";
import envelope from "@turf/envelope";
import bboxPolygon from "@turf/bbox-polygon";
import intersect from "@turf/intersect";
import {paint} from "./mapUtils/MapUtils";
import LayerSelector from "./components/LayerSelector";

mapboxgl.accessToken = 'pk.eyJ1IjoiYWxlLWRlbG1hc3RybyIsImEiOiJjbGFzOWZnOWoyMGY3M3BxdjE1d29lcnV4In0.0X1pCcxD7RtDiAOc_XFjwQ';



function App() {
    const mapContainer = useRef(null);
    const map = useRef(null);
    const mapUtils = useRef(null);

    const [popup, setPopup] = useState({x: 10, y: 10, lng: 7.681642, lat: 45.0728662, visible: false});
    const [lng, setLng] = useState(7.681642);
    const [lat, setLat] = useState(45.0728662);
    const [zoom, setZoom] = useState(14);
    const fromMarker = useRef(null);
    const toMarker = useRef(null);

    const [from, setFrom] = useState('');
    const [to, setTo] = useState('');

    const itinsDisplayed = useRef([]);
    const debugLayers = useRef({});
    const [debugLayersName, setDebugLayersName] = useState([]);
    const [layer, setLayer] = useState("");
    const [radioDisabled, setRadioDisabled] = useState(true);

    function setMarker(marker, lngLat) {
        if (marker.current === null)
            marker.current = new mapboxgl.Marker();
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
            /*
                  style: 'mapbox://styles/ale-delmastro/clcuf444c00aj14mqqxqb5wsn',
            */
            center: [lng, lat],
            zoom: zoom,
            dragRotate: false
        });
        /*
            map.current.addControl(new mapboxgl.NavigationControl());
        */


        requestInspectorModes().then(res => {
            console.log(res);
            debugLayers.current = res;
            setDebugLayersName(res);
        })
            .catch(e => console.error("NON FAREMO GLI STESSI ERRORI DELLA TELEFUNKEN"));
    });


    useEffect(() => {
        if (!map.current) return; // wait for map to initialize

        const onLoad = () => {
            mapUtils.current = MapUtils.build(map.current);
            mapUtils.current.addTiles();
            /*
                  const l = map.current.getSource("tiles");
            */
            setRadioDisabled(false);
        }

        const onContext = (e) => {
            e.preventDefault();
            setPopup({
                x: e.originalEvent.x,
                y: e.originalEvent.y,
                lng: e.lngLat.lng,
                lat: e.lngLat.lat,
                visible: true
            });
        };

        const onClick = (e) => {
            setPopup(prevState => {
                return {...prevState, ...{visible: false}};
            });
            console.log(map.current.getZoom());
        }

        map.current.on('load', onLoad);
        map.current.on('contextmenu', onContext);
        map.current.on('click', onClick);

        return () => {
            if (map.current !== null) {
                map.current.off('contextmenu', onContext);
                map.current.off('click', onClick);
                map.current.off('load', onLoad);
            }
        }
    });

    function handleChange() {
        return (e, v) => {
            setLayer(v.value);
            map.current.setPaintProperty('tiles', 'line-color', paint(v.value));
            map.current.setFilter('tiles', ['>=', ['get', v.value], 0.5]);
        };
    }

    return (
        <div id="container">
            <IOTile
                submit={clearStreet}
                plotResult={plotResult}
                from={from}
                to={to}
                setFrom={setFrom}
                setTo={setTo}
            />
            <ContextMenu
                setTo={() => updateFromTo(false)}
                setFrom={() => updateFromTo(true)}
                visible={popup.visible}
                left={popup.x}
                top={popup.y}
            />
            {/*        <div style={{
          position: "absolute",
          right: 10,
          top: 10,
          backgroundColor: "green",
          zIndex: 10
        }}
       id={"inspector"}
       onClick={async () => {
           map.current.setPaintProperty('tilesasdasd', 'line-color', paint('score'));
       }}>
          {"Green"}
        </div>
        <div
            style={{
              position: "absolute",
              right: 50,
              top: 10,
              backgroundColor: "blue",
              zIndex: 10
            }}
        onClick={() => {
            map.current.setPaintProperty('tilesasdasd', 'line-color', paint('inbetween_other_green'));
      }}>
        {"Mostra"}
      </div>*/}

            <LayerSelector debugLayersName={debugLayersName} disabled={radioDisabled} active={layer}
                           onChange={handleChange(setLayer)}/>
            <div ref={mapContainer} className="map-container"/>
        </div>
    );
}

export default App;
