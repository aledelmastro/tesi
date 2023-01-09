import axios from "axios";

//const baseUrl = 'https://otp-sustain.5t.torino.it/otp/routers/default';
const baseUrl = 'http://localhost:8080/otp/routers/default';

function formatCoordinates(lat, lon) {
    return lat+','+lon;
}

async function requestItinerary(from, to, time, date, mode, locale, callback) {
    const url = baseUrl + "/plan";

    const res = await axios.get(url,{
        params:{
            //fromPlace: '45.06591,7.66738',
            fromPlace: from,
            //toPlace: '45.06779,7.69394',
            toPlace: to,
            time: '9:37am',
            date: '11-11-2022',
            mode: 'WALK',
            locale: 'it',
        }
    });

    if (res.data.error !== undefined)
        throw new Error(res.data.error.id + " " + res.data.error.message);

    return res.data.plan.itineraries;
}

async function requestInspector(latBr, lngBr, latTl, lngTl, green) {
    const mode = green ? "green" : "other";
    const url = baseUrl + "/inspector/json/" + mode;

    const res = await axios.get(url,{
        params:{
            latTl: latTl,
            lngTl: lngTl,
            latBr: latBr,
            lngBr: lngBr
        }
    });

    if (res.data.error !== undefined)
        throw new Error(res.data.error.id + " " + res.data.error.message);

    return res.data;
}

export {requestItinerary, requestInspector}
