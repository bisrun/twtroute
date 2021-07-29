
const shpCreateCheckBox = document.querySelector('#shpCreateCheckBox');
const shpAutoSaveCheckBox = document.querySelector('#shpAutoSaveCheckBox');
const fileInputText = document.querySelector('#fileInputText');
const btnDwnShp = document.querySelector('#btnDwnShp')

function saveCheckOption(checkBox) {
    const key = checkBox.name;
    const value = checkBox.checked;
    localStorage.setItem(key, value)
}

function loadCheckOption(checkBox) {
    const key = checkBox.name;
    const value = localStorage.getItem(key);

    if ((value !== null) &&(value !== undefined)) {
        checkBox.checked = JSON.parse(value);
    }
}

function clickATagByAuto(resultOfAny) {
    if (resultOfAny['result'] !== 'success') {
        console.log(resultOfAny['urlPath']);
        return;
    }

    const urlPath = resultOfAny['urlPath'];
    const downloadName = resultOfAny['downloadName'];
    const linkEle = document.createElement('a');

    linkEle.href = urlPath;
    linkEle.download = downloadName;
    linkEle.click();
}

function finishedInputText(event, name) {
    const data = event.target.result;
    const PostUrl = `/develop/convert/${name}`;

    fetch(PostUrl, {
        method: 'POST',
        body: data,
        headers: {
            'Content-Type': 'text/plain',
        }
    })
    .then(response => response.json())
    .then(result => {
        clickATagByAuto(result);
        /*const urlPath = result['urlPath'];
        const downloadName = result['downloadName'];

        const linkEle = document.createElement('a');
        linkEle.href = urlPath;
        linkEle.download = downloadName;
        linkEle.click();*/
    })
    .catch(err => alert(err));
}

function getRequestJSON(event) {
    event.preventDefault();
    const file = document.querySelector(`#${event.srcElement.id}`);
    const reader = new FileReader();
    const fileName = file.files[0].name;
    
    reader.onload = (e => finishedInputText(e, fileName));
    reader.readAsText(file.files[0]);
    // reset
    file.value = '';
}

function getCurrentShape(event) {
    event.preventDefault();
    const PostUrl = `/develop/shape`;
    const data = localStorage.getItem(LAST_RESPONSE_DATA);
    
    if (data === null) {
        alert('No data');
        return;
    }
    
    fetch(PostUrl, {
        method: 'POST',
        body: data,
    })
    .then(response => response.json())
    .then(results => {
        results.forEach(result => {
            clickATagByAuto(result);
        })
    })
    .catch(err => alert(err));
}

// check box in memory
shpCreateCheckBox.addEventListener('click', function(event){
    saveCheckOption(shpCreateCheckBox);
});
shpAutoSaveCheckBox.addEventListener('click', function(event){
    saveCheckOption(shpAutoSaveCheckBox);
});
fileInputText.addEventListener('change', event => getRequestJSON(event));
btnDwnShp.addEventListener('click', event => getCurrentShape(event));

// load check box
loadCheckOption(shpCreateCheckBox);
loadCheckOption(shpAutoSaveCheckBox);
//localStorage.removeItem(LAST_RESPONSE_DATA);



