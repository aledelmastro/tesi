import './App.css';
import React, { useRef, useEffect, useState } from 'react';
import mapboxgl from '!mapbox-gl'; // eslint-disable-line import/no-webpack-loader-syntax
import IOTile from "./components/IOTile";
import {MapUtils} from "./mapUtils/MapUtils";
import ContextMenu from "./components/ContextMenu";

mapboxgl.accessToken = 'pk.eyJ1IjoiYWxlLWRlbG1hc3RybyIsImEiOiJjbGFzOWZnOWoyMGY3M3BxdjE1d29lcnV4In0.0X1pCcxD7RtDiAOc_XFjwQ ';

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
    const val = popup.lat.toFixed(4)+','+popup.lng.toFixed(4);
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
    mapUtils.current.clearStreet();
  }

  function plotResult(points, id, collapsed) {
    if (!mapUtils.current) return;
    mapUtils.current.addPath(points, id);
    setMarker(fromMarker, points[0]);
    setMarker(toMarker, points[points.length-1]);
  }

  useEffect(() => {
    if (map.current) return; // initialize map only once
    map.current = new mapboxgl.Map({
      container: mapContainer.current,
      style: 'mapbox://styles/mapbox/streets-v12',
      center: [lng, lat],
      zoom: zoom,
      dragRotate: false
    });
    map.current.addControl(new mapboxgl.NavigationControl());
    console.log("Init");
  });


  useEffect(() => {
    if (!map.current) return; // wait for map to initialize

    const onLoad = () => {
      mapUtils.current = MapUtils.build(map.current);
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

  return (
      <div id="container">
        <IOTile
            submit={clearStreet}
            plotResult={plotResult}
            from = {from}
            to = {to}
            setFrom ={setFrom}
            setTo ={setTo}
        />
        <ContextMenu
            setTo={() => updateFromTo(false)}
            setFrom={() => updateFromTo(true)}
            visible={popup.visible}
            left={popup.x}
            top={popup.y}
        />
        <div ref={mapContainer} className="map-container" />
      </div>
  );
}

export default App;
