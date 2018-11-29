/**
 * Get JWT access token by sending email and password
 */
function getJWT()
{
    var email = document.getElementById("email-input").value;
    var password = document.getElementById("password-input").value;

    var data = '{"username":"' + email + '", "password":"' + password + '"}';

    makeLoginReq("POST", "/auth", 200, storeJWT, data);
}

/**
 * Store the access token in local storage and redirect to user's profile
 * @param  {String} responseText - HTTP response text
 */
function storeJWT(responseText)
{
    var token = JSON.parse(responseText)['access_token'];
    if (typeof(Storage) !== "undefined")
    {
        localStorage.setItem("access_token", token);
        console.log(localStorage.getItem("access_token"));
    }

    // makeTokenReq("GET", "/profile/", 200, goToProfile, null, token);
}

function goToProfile(responseText)
{

}

/**
* Makes an HTTP request
* @param {Function} method - HTTP method (GET, POST, etc.)
* @param {String} target - Resource URL
* @param {Number} retCode - Expected return code
* @param {Function} callback - Callback method to execute once request has completed
* @param {String} data - Data to send with request
*/
function makeLoginReq(method, target, retCode, callback, data)
{
    // Create new request
    var httpRequest = new XMLHttpRequest();

	if (!httpRequest){
		alert('Giving up :( Cannot create an XMLHTTP instance');
		return false;
	}

    // On state change of request, makeHandler
	httpRequest.onreadystatechange = makeLoginHandler(httpRequest, retCode, callback);

    // Open the request
	httpRequest.open(method, target);

    // If the request contains data
	if (data){
		httpRequest.setRequestHeader('Content-Type', 'application/json');
		httpRequest.send(data);
	}else{
		httpRequest.send();
	}
}

/**
* Function to handle the reqeust
* @param {XMLHttpRequest} httpRequest
* @param {Number} retCode - Expected return code
* @param {Function} callback - Callback method
*/
function makeLoginHandler(httpRequest, retCode, callback)
{
	function handler() {
        // If request has been completed
		if (httpRequest.readyState === XMLHttpRequest.DONE){
            // If return code is as expected
            if (httpRequest.status === retCode){
				console.log("received response text:  " + httpRequest.responseText);
				callback(httpRequest.responseText);     // Call callback method on response text
			}else if(httpRequest.status == 0){
				console.log("received response text:  " + httpRequest.responseText);
				callback(httpRequest.responseText);
            }else if(httpRequest.status == 401){
                alert("Incorrect email or password. Please try again.");
				window.location.href = "/";
			}else{
				alert("There was a problem with the request.  You'll need to refresh the page!");
				alert(httpRequest.status);
			}
		}
	}
	return handler;
}
