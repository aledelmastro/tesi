import axios from "axios";

const baseUrl = 'https://otp-sustain.5t.torino.it/otp/routers/default/plan';
//const baseUrl = 'http://localhost:8080/otp/routers/default/plan';

function formatCoordinates(lat, lon) {
    return lat+','+lon;
}

async function requestItinerary(from, to, time, date, mode, locale, callback) {

    const res = await axios.get(baseUrl,{
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

export {requestItinerary}
