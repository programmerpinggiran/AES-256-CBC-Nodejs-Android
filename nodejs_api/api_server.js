const crypto = require('crypto');
const bodyParser = require('body-parser');
var express = require('express');
var app = express();
var randomstring = require("randomstring");
var mymd5 = require('md5');
const ENCRYPTION_KEY = mymd5('YOUR_SECRET_KEY'); // Must be 256 bits (32 characters)

const IV_LENGTH = 16; // For AES, this is always 16
let algorithm = 'aes-256-cbc';
const port = 4000;

app.use(bodyParser.json({
    limit: '50mb'
}));
app.use(bodyParser.urlencoded({
    limit: '50mb',
    extended: true
}));

var http = require('http').Server(app);
http.listen(port, function () {
    console.log('App server listening on port ' + port);
});

var MSG_SUCCESS = 'Success';


function encrypt(data) {
    var randomiv = randomstring.generate({
        length: IV_LENGTH,
        charset: 'alphanumeric',
    });
    var cipher = crypto.createCipheriv(algorithm, ENCRYPTION_KEY, randomiv)
    var crypted = cipher.update(data, 'utf8', 'base64')
    crypted += cipher.final('base64');
    //you can random and hide your iv here, example randomstring+randomiv+crypted
    return randomiv + crypted;
}

function decrypt(text) {
    let iv = text.substring(0, 16);
    let data = new Buffer(text.substring(16, text.length), "base64")
    let decipher = crypto.createDecipheriv(algorithm, ENCRYPTION_KEY, iv);
    let decrypted = decipher.update(data);
    decrypted = Buffer.concat([decrypted, decipher.final()]);
    return decrypted.toString('utf8');
}

app.get('/getdata', function (request, res) {
    let origindata = {
        message: MSG_SUCCESS,
        status: "1",
        data: "your data here",
    };

    var encrypt_result = encrypt(JSON.stringify(origindata));
    res.json(encrypt_result);
});

app.post('/senddata', function (request, res) {
    if (!request.body.data) {
        let origindata = {
            success: false,
            status: "0",
            data: "Maaf sesi kakak telah habis, silahkan login kembali. code 103",
        };
        var encrypt_result = encrypt(JSON.stringify(origindata));
        res.json(encrypt_result);
        return;
    } else {
        console.log("retreive post data " + request.body.data);
        let postdata = JSON.parse(decrypt(request.body.data));
        console.log("post decrypt result " + postdata);

        let origindata = {
            message: MSG_SUCCESS,
            status: "1",
            data: "your data : " + postdata,
        };
        var encrypt_result = encrypt(JSON.stringify(origindata));
        console.log("user request sent" + encrypt_result);
        res.json(encrypt_result);
    }


});