import SearchInputWidget from "./SearchInputWidget";
import React, {useRef, useState} from "react";
import ResContainer from "./ResContainer";
import {Button, Table} from "semantic-ui-react";
import * as PropTypes from "prop-types";

function IOTile({features, from, info, operators, plotResult, scores, setFrom, setTo, submit, to, showInfoBox, setShowInfoBox}) {
    const [res, setRes] = useState([]);
    const [itins, setItineraries] = useState({
        res: [],
        pinned: []
    });

    console.log(itins.pinned);

    return (
        <div id={"IoTile"}>
            <SearchInputWidget
                setRes={(itins) => setItineraries(prevState => {return{...prevState, ...{res: itins}}})}
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
                itins.pinned.filter(i => !itins.res.includes(i)).map((itin, i) =>
                    <ResContainer itin={itin} key={i} id={i} plotResult={plotResult} pinned={true}
                                  onClick={(itinerary) => {
                                      setItineraries(prevState => {
                                          /*let pinnedItineraries = Array.from(prevState.pinned);*/
                                          const pinnedItineraries = prevState.pinned.filter(i => i !== itinerary);

                                          return {
                                              res: prevState.res,
                                              pinned: pinnedItineraries
                                          }
                                      })
                                  }}
                    />
                )
            }
            {
                itins.res.map((itin, i) =>
                    <ResContainer itin={itin} key={i} id={i} plotResult={plotResult} pinned={false}
                                  onClick={(itinerary) => {
                                      setItineraries(prevState => {
                                          let pinnedItineraries = Array.from(prevState.pinned);
                                          let itineraries = Array.from(prevState.res);
                                          pinnedItineraries.push(itinerary);
                                          itineraries = itineraries.filter(i => i !== itinerary)

                                          return {
                                              res: itineraries,
                                              pinned: pinnedItineraries
                                          }
                                      })
                                  }}
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
