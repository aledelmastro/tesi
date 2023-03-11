import React from "react";
import * as PropTypes from "prop-types";
import {Button, Table} from "semantic-ui-react";

function InfoBox(props) {
    const {info, setShowInfoBox} = props;

    return (
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
    )
}

InfoBox.propTypes = {
    setShowInfoBox: PropTypes.func.isRequired,
    info: PropTypes.object.isRequired
}

export default InfoBox;
