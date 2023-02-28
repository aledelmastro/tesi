import SearchInputWidget from "./SearchInputWidget";
import React, {useRef, useState} from "react";
import ResContainer from "./ResContainer";
import {Button, Table} from "semantic-ui-react";
import * as PropTypes from "prop-types";
import {decodePolyline} from "../mapUtils/MapUtils";

class Itinerary {
    constructor(itinerary, pinned, displayed) {
        this.itinerary = itinerary;
        this.pinned = pinned;
        this.displayed = displayed;
    }
}

const colors = ["#5d8aa8", "#e32636", "#ffbf00", "#008000", "#ff2052"];

function IOTile({features, from, info, operators, plotResult, scores, setFrom, setTo, submit, to, showInfoBox, setShowInfoBox, deleteRes}) {
    const [itins, setItineraries] = useState([]);

    function updateItineraries(newItins) {
        setItineraries(prevState => {
            const itins = Array.from(prevState.filter(i => i.pinned));
            newItins.forEach(ni => itins.push(new Itinerary(ni, false, false)));
            return itins;
        });
    }

    function plotItin(itin, id, color) {
        let points = [];
        itin.itinerary.legs.forEach(leg => {
            points = points.concat(decodePolyline(leg.legGeometry.points).map(point => [point.lng, point.lat]));
        });
        plotResult(points, id, color);
    }

    return (
        <div id={"IoTile"}>
            <SearchInputWidget
                setRes={(newItins) => updateItineraries(newItins)}
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
                itins.map((itin, key) =>
                    <ResContainer itin={itin.itinerary} key={key} pinned={itin.pinned}
                            color ={colors[key%itins.length]}
                            displayed = {itin.displayed}
                            onPin = {() => {
                              setItineraries(prevState => {
                                  const updatedItins = [];
                                  prevState.forEach(i => {
                                      if (i === itin) {
                                          updatedItins.push(new Itinerary(i.itinerary, !i.pinned, i.displayed));
                                      } else {
                                          updatedItins.push(i);
                                      }
                                  });
                                  console.log(updatedItins);
                                  return updatedItins;
                              });
                            }}
                            onDisplay = { () => {
                                setItineraries(prevState => {
                                    const updatedItins = [];
                                    prevState.forEach(i => {
                                        if (i === itin) {
                                            updatedItins.push(new Itinerary(i.itinerary, i.pinned, !i.displayed));
                                            if (!i.displayed) {
                                                plotItin(itin, "itin"+key, colors[key%itins.length]);
                                            } else {
                                                deleteRes("itin"+key);
                                            }
                                        } else {
                                            updatedItins.push(i);
                                        }
                                    });
                                    console.log(updatedItins);
                                    return updatedItins;
                                });
                            }}
                            plotResult = {() => plotItin(itin, key, )} //TODO reivedere
                    />
                )
            }
            {
                showInfoBox && info.length > 0 ?
                    <div id={"InfoBox"}>
                        {info.map((v, i) => {
                            return(
                                <Table compact basic key = {i}>
                                    <Table.Header>
                                        <Table.Row>
                                            <Table.HeaderCell>Dettagli</Table.HeaderCell>
                                            <Button icon={"close"} onClick={() => setShowInfoBox(false)} />
                                        </Table.Row>
                                    </Table.Header>
                                    <Table.Body>{
                                        Object.keys(v).map(k =>
                                            <Table.Row key={k}>
                                                <Table.Cell>{k}</Table.Cell>
                                                <Table.Cell>{v[k]}</Table.Cell>
                                            </Table.Row>
                                        )}</Table.Body>
                                </Table>
                            )

                        })}
                    </div>
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
