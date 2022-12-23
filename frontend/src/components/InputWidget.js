import {requestItinerary} from "../calls/calls";
import * as PropTypes from "prop-types";
import LabelledInput from "./LabelledInput";

async function submitRequest(event, props) {
    event.preventDefault();

    props.submit();
    // TODO verificare formato correto
    const res = await requestItinerary(props.from, props.to);
    props.setRes(res);
}

function InputWidget(props) {
    const {setFrom, setTo, from, to} = props;

    return (
        <div id="widgetContainer">
            <form onSubmit={event => submitRequest(event, props)}>
                <LabelledInput name={"from"} label={"Partenza"} onChange={setFrom} value={from} />
                <LabelledInput name={"to"} label={"Arrivo"} onChange={setTo} value={to} />
                <button type="submit">
                    Cerca
                </button>
            </form>
        </div>

    );
}

InputWidget.propTypes = {
    setFrom: PropTypes.func.isRequired,
    setTo: PropTypes.func.isRequired,
    from: PropTypes.string.isRequired,
    to: PropTypes.string.isRequired,
    submit: PropTypes.func.isRequired,
    setRes: PropTypes.func.isRequired
}

export default InputWidget;
