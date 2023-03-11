import SearchInputWidget from "./SearchInputWidget";
import React, {useEffect, useRef, useState} from "react";
import ResContainer from "./ResContainer";
import * as PropTypes from "prop-types";
import {decodePolyline} from "../mapUtils/MapUtils";
import InfoBox from "./InfoBox";

class Itinerary {
    constructor(itinerary, id, formula = "", isNew = true, color = "#5d8aa8") {
        this.itinerary = itinerary;
        this.formula = formula;
        this.id = id;
        this.isNew = isNew;
        this.color = color;
    }
}

const colors = ["#2a52be", "#e32636", "#ffbf00", "#008000", "#007aa5", "#ed872d", "#00bfff", "#e4d00a", "#ff3800"];

function toColor(key) {
    return colors[key%(colors.length - 1)];
}

function toId(key) {
    return ""+key;
}

function IOTile({features, from, info, operators, plotResult, scores, setFrom, setTo, submit, to, showInfoBox, setShowInfoBox, deleteRes}) {
    const [itins, setItineraries] = useState([]);
    const [displayed, setDisplayed] = useState(new Set());
    const [pinned, setPinned] = useState(new Set());
    const colorIndex = useRef(0);

    function nextColor() {
        const color = toColor(colorIndex.current);
        colorIndex.current += 1;
        return color;
    }

    // To automatically plot the first of the new itineraries
    useEffect(() => {
        const newlyAdded = itins.filter(i => i.isNew);
        if (newlyAdded !== null && newlyAdded.length > 0) {
            newlyAdded.forEach(na => na.isNew = false);
            plotItin(newlyAdded[0], newlyAdded[0].id, newlyAdded[0].color);
        }
        if (newlyAdded[0])
            setDisplayedItin(newlyAdded[0], true);
    }, [itins]);

    function updateItineraries(newItins, formula) {
        itins.filter(i => !displayed.has(i)).forEach(i => deleteRes(i.id));

        if (newItins !== null && newItins) {
            setItineraries(prevState => {
                const itins = Array.from(prevState.filter(i => pinned.has(i)));
                itins.forEach(i => i.isNew = false);
                newItins.forEach((ni, i) => itins.push(new Itinerary(ni, toId(itins.length+i), formula, true, nextColor())));
                return itins;
            });
        }
    }

    function plotItin(itin, id, color) {
        let points = [];
        itin.itinerary.legs.forEach(leg => {
            points = points.concat(decodePolyline(leg.legGeometry.points).map(point => [point.lng, point.lat]));
        });
        plotResult(points, id, color);
    }

    function setPinnedItin(itinerary, isPinned) {
        setPinned(prevState => {
            const updatedItins = new Set(prevState.keys());
            if (prevState.has(itinerary) && !isPinned)
                updatedItins.delete(itinerary);

            if (!prevState.has(itinerary) && isPinned)
                updatedItins.add(itinerary);

            return updatedItins;
        });
    }

    function setDisplayedItin(itinerary, isDisplayed) {
        setDisplayed(prevState => {
            const updatedItins = new Set(prevState.keys());
            if (prevState.has(itinerary) && !isDisplayed) {
                updatedItins.delete(itinerary);
                deleteRes(itinerary.id);
            }

            if (!prevState.has(itinerary) && isDisplayed) {
                updatedItins.add(itinerary);
                plotItin(itinerary, itinerary.id, itinerary.color);
            }

            return updatedItins;
        });
    }

    return (
        <div id={"IoTile"}>
            <SearchInputWidget
                setRes={(newItins, formula) => updateItineraries(newItins,formula)}
                submit={submit}
                setFrom={setFrom}
                setTo={setTo}
                from={from}
                to={to}
                features={features}
                scores={scores}
                operators={operators}
            />
            {
                itins.map((itinerary, i) =>
                    <ResContainer
                        itin={itinerary.itinerary}
                        key={i}
                        pinned={pinned.has(itinerary)}
                        displayed = {displayed.has(itinerary)}
                        formula = {itinerary.formula}
                        color ={itinerary.color}
                        onPin = {(isPinned) => setPinnedItin(itinerary, isPinned)}
                        onDisplay = {(isDisplayed) => setDisplayedItin(itinerary, isDisplayed)}
                    />
                )
            }
            {
                showInfoBox && info.length > 0 ?
                    <InfoBox info={info} setShowInfoBox={setShowInfoBox} />
                    :
                    <></>
            }

        </div>
    );
}

IOTile.propTypes = {
    operators: PropTypes.arrayOf(PropTypes.string).isRequired,
    scores: PropTypes.arrayOf(PropTypes.string).isRequired,
    features: PropTypes.arrayOf(PropTypes.string).isRequired,
    setFrom: PropTypes.func.isRequired,
    setTo: PropTypes.func.isRequired,
    from: PropTypes.string.isRequired,
    to: PropTypes.string.isRequired,
    info: PropTypes.arrayOf(PropTypes.object).isRequired,
    plotResult: PropTypes.func.isRequired,
    submit: PropTypes.func.isRequired
}

export default IOTile;
