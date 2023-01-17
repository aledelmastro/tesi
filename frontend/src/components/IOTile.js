import SearchInputWidget from "./SearchInputWidget";
import React, {useState} from "react";
import ResContainer from "./ResContainer";

function IOTile(props) {
    const [res, setRes] = useState([]);
    return (
        <div style={style}>
            <SearchInputWidget
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

const style = {
    padding: 20
}

export default IOTile;
