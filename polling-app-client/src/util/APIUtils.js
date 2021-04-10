import { API_BASE_URL, POLL_LIST_SIZE, ACCESS_TOKEN } from '../constants';
import axios from 'axios';
const request = (options) => {
    const headers = new Headers({
        'Content-Type': 'application/json',
    })
    
    if(localStorage.getItem(ACCESS_TOKEN)) {
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(ACCESS_TOKEN))
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);

    return fetch(options.url, options)
    .then(response => 
        response.json().then(json => {
            if(!response.ok) {
                return Promise.reject(json);
            }
            return json;
        })
    );
};
const request2 = (options) => {
    const headers = new Headers({
        'Accept': '*/*',
    })
    
    if(localStorage.getItem(ACCESS_TOKEN)) {
        headers.append('Authorization', 'Bearer ' + localStorage.getItem(ACCESS_TOKEN))
    }

    const defaults = {headers: headers};
    options = Object.assign({}, defaults, options);

    return fetch(options.url, options)
    .then(response => 
        response.json().then(json => {
            if(!response.ok) {
                return Promise.reject(json);
            }
            return json;
        })
    );
};
export function getAllPolls(page, size) {
    page = page || 0;
    size = size || POLL_LIST_SIZE;

    return request({
        url: API_BASE_URL + "/polls?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function createPoll(pollData) {
    return request({
        url: API_BASE_URL + "/polls",
        method: 'POST',
        body: JSON.stringify(pollData)         
    });
}

export function castVote(voteData) {
    return request({
        url: API_BASE_URL + "/polls/" + voteData.pollId + "/votes",
        method: 'POST',
        body: JSON.stringify(voteData)
    });
}

export function deleteVote(deleteData) {
    return request({
        url: API_BASE_URL + "/polls/" + deleteData.pollId + "/deletevotes",
        method: 'POST',
        body: JSON.stringify(deleteData)
    });
}
export function deleteChoice(deleteData) {
    return request({
        url: API_BASE_URL + "/polls/" + deleteData.pollId + "/deletechoice",
        method: 'POST',
        body: JSON.stringify(deleteData)
    });
}
export function addChoice(addData) {
    return request({
        url: API_BASE_URL + "/polls/" + addData.pollId + "/addchoice",
        method: 'POST',
        body: JSON.stringify(addData)
    });
}
export function login(loginRequest) {
    return request({
        url: API_BASE_URL + "/auth/signin",
        method: 'POST',
        body: JSON.stringify(loginRequest)
    });
}

export function signup(signupRequest) {
    return request({
        url: API_BASE_URL + "/auth/signup",
        method: 'POST',
        body: JSON.stringify(signupRequest)
    });
}
export function changeAvatar(imageData) {
// var singleFileUploadError = document.querySelector('#singleFileUploadError');
// var singleFileUploadSuccess = document.querySelector('#singleFileUploadSuccess');
//     var xhr = new XMLHttpRequest();
//     xhr.open("POST", API_BASE_URL + "/file/useravatar");

//     xhr.onload = function() {
//         console.log(xhr.responseText);
//         var response = JSON.parse(xhr.responseText);
//         if(xhr.status == 200) {
//             return response.fileDownloadUri;
//         } else {
//             return "fail";
//         }
//     }
//     xhr.send(imageData);
    //return axios.post(API_BASE_URL + "/useravatar", imageData);
    return request2({
        url: API_BASE_URL + "/file/useravatar",
        method: 'POST',
        body: imageData
    });
}
export function getImage(filename) {
    return request({
        url: API_BASE_URL + "/file/getImage/" + filename,
        method: 'GET'
    });
}

export function checkUsernameAvailability(username) {
    return request({
        url: API_BASE_URL + "/user/checkUsernameAvailability?username=" + username,
        method: 'GET'
    });
}

export function checkEmailAvailability(email) {
    return request({
        url: API_BASE_URL + "/user/checkEmailAvailability?email=" + email,
        method: 'GET'
    });
}


export function getCurrentUser() {
    if(!localStorage.getItem(ACCESS_TOKEN)) {
        return Promise.reject("No access token set.");
    }

    return request({
        url: API_BASE_URL + "/user/me",
        method: 'GET'
    });
}

export function getUserProfile(username) {
    return request({
        url: API_BASE_URL + "/users/" + username,
        method: 'GET'
    });
}

export function getUserCreatedPolls(username, page, size) {
    page = page || 0;
    size = size || POLL_LIST_SIZE;

    return request({
        url: API_BASE_URL + "/users/" + username + "/polls?page=" + page + "&size=" + size,
        method: 'GET'
    });
}

export function getUserVotedPolls(username, page, size) {
    page = page || 0;
    size = size || POLL_LIST_SIZE;

    return request({
        url: API_BASE_URL + "/users/" + username + "/votes?page=" + page + "&size=" + size,
        method: 'GET'
    });
}