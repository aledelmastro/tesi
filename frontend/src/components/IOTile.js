import SearchInputWidget from "./SearchInputWidget";
import React, {useState} from "react";
import ResContainer from "./ResContainer";
import {Table} from "semantic-ui-react";

function IOTile(props) {
    const [res, setRes] = useState([]);
    return (
        <div id={"IoTile"}>
            <SearchInputWidget
                setRes={setRes}
                submit={props.submit}
                setFrom={props.setFrom}
                setTo={props.setTo}
                from={props.from}
                to={props.to}
            />
            {res.map((itin, i) => <ResContainer itin={itin} key={i} id={i} plotResult={props.plotResult}/>)}
            <div id={"InfoBox"}>
                {props.info.map((v, i) => {
                    return(
                    <Table compact basic key = {i}>
                        <Table.Header>
                            <Table.Row>
                                <Table.HeaderCell colSpan='2'>Dettagli</Table.HeaderCell>
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
        </div>
    );
}

export default IOTile;
