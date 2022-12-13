import InputWidget from "./InputWidget";
import React, {useState} from "react";
import ResContainer from "./ResContainer";

function IOTile(props) {
    const [res, setRes] = useState([]);
    return (
        <div>
            <InputWidget
                setRes={setRes}
                submit={props.submit}
                setFrom={props.setFrom}
                setTo={props.setTo}
                from={props.from}
                to={props.to}
            />
            {res.map((itin, i) => <ResContainer itin={itin} key={i} id={i} plotResult={props.plotResult}/>)}
        </div>
    );
}

export default IOTile;
