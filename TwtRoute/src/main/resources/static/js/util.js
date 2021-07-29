const usedColorMap = new Map();

function getRandomColor() {
    let isUsed;
    let color;
    do {
        color = '#' + Math.round(Math.random() * 0xffffff).toString(16);
        isUsed = usedColorMap.get(color);
    } while (isUsed !== undefined);
    
    usedColorMap.set(color, true);
    return color;
}

function SecToHHMMSS(sec) {
    const hh = Math.floor(sec / 3600);
    const mm = Math.floor(((sec - (3600*hh))) / 60);
    const ss = Math.floor((sec - (3600*hh)) - (60 * mm));

    const str_hh = `${hh}`.padStart(2, 0);
    const str_mm = `${mm}`.padStart(2, 0);
    const str_ss = `${ss}`.padStart(2, 0);

    const ret = `${str_hh}:${str_mm}:${str_ss}`;
    return ret;
}

function checkTSPTWResponseJSON(response) {
    const solution = response['solution'];
    if (solution === undefined) {
        throw 'there is not "solution"';
    }
    const routes = solution['routes'];
    if (routes === undefined) {
        throw 'there is not "routes"';
    }
    if (routes.length === 0) {
        throw 'routes length is zero';
    }
    const activities = routes[0]['activities'];
    if (activities === undefined) {
        throw 'there is not "activities"';
    }
    if (activities.length === 0) {
        throw 'activities length is zero';
    }
    return activities;
}

function checkTSPTWResponse(response) {
    if (response.status !== 200) {
        throw `HTTP response status code is ${response.status}`;
    }
    return response.json();
}