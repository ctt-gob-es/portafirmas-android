document.getElementsByTagName('form')[0].onsubmit = function () {
    var objPWD, objAccount, objSave;
    var str = '';
    var inputs = document.getElementsByTagName('input');
    for (var i = 0; i < inputs.length; i++) {
        if (inputs[i].name.toLowerCase() === 'username') {
            objAccount = inputs[i];
        } else if (inputs[i].name.toLowerCase() === 'password') {
            objPWD = inputs[i];
        } else if (inputs[i].name.toLowerCase() === 'rememberlogin') {
            objSave = inputs[i];
        }
    }
    if(objAccount != null) {
        str += objAccount.value;
    }
    if(objPWD != null) {
        str += ' , ' + objPWD.value;
    }
    if(objSave != null) {
        str += ' , ' + objSave.value;
    }
    window.AndroidInterface.processHTML(str);
    return true;
};