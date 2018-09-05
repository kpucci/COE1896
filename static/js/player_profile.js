function setup()
{
    loadStats();
    var id = document.getElementById("player-id").value;
    makeReq("GET", "/players/" + id, 200, populateProfile);
}

function populateProfile(responseText)
{
    console.log("-----Populating Player Profile-----");
    console.log(responseText);
    var player = JSON.parse(responseText);

    var first_name = player['first_name'];
    var last_name = player['last_name'];

    document.getElementById("player-name").innerHTML = first_name + " " + last_name;
}

function logout()
{

}

function goToLogin(responseText)
{
    window.location.href = "/";
}

function loadStats()
{
    document.getElementById("stats-link").classList.add("active");
    document.getElementById("drills-link").classList.remove("active");
    document.getElementById("stats").style.display = "block";
    document.getElementById("drills").style.display = "none";
}

function loadDrills()
{
    document.getElementById("drills-link").classList.add("active");
    document.getElementById("stats-link").classList.remove("active");
    document.getElementById("drills").style.display = "block";
    document.getElementById("stats").style.display = "none";
}

function loadCatalog()
{

}

function loadSettings()
{

}

// setup load event
window.addEventListener("load", setup, true);
