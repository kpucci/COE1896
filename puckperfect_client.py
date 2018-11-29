import os
import datetime
import re
from flask import Flask, request, abort, url_for, redirect, session, render_template, flash, Response

app = Flask(__name__)
# app.static_folder = 'project_files/puckperfect/static'
# app.template_folder = 'project_files/puckperfect/templates'

app.config.update(dict(
    DEBUG=True,
    SECRET_KEY='development key',
    USERNAME='owner',
    PASSWORD='pass'
))

#--------------------------------------------------------------------------------------------
#--------------------------------------------------------------------------------------------

# Login page:
@app.route("/", methods=['GET','POST'])
def login():
    return render_template("login.html")

#--------------------------------------------------------------------------------------------

# Logout
@app.route("/logout/")
def logout():
	session.pop("logged_in", None)
	flash("You've been signed out.")
	return redirect(url_for('default'))

#--------------------------------------------------------------------------------------------

# Registration page:
@app.route("/register/", methods=['GET','POST'])
def register():
    return render_template("register.html")

#--------------------------------------------------------------------------------------------

# Player profile page:
@app.route("/player/<id>", methods=['GET'])
def player_profile(id=None):
    if "access_token" in session:
        
    return render_template("player_profile.html", id=id)

#--------------------------------------------------------------------------------------------

# Coach profile page:
@app.route("/coach/<id>", methods=['GET'])
def coach_profile(id=None):
    return render_template("coach_profile.html")

#--------------------------------------------------------------------------------------------

# Parent profile page:
@app.route("/parent/<id>", methods=['GET'])
def parent_profile(id=None):
    return render_template("parent_profile.html")

#--------------------------------------------------------------------------------------------

# Drill catalog page:
@app.route("/catalog/", methods=['GET'])
def catalog(id=None):
    # Get player
    # Get player's playlist
    # Get all drills
    # Filter out drills that are already in the playlist
    return render_template("catalog.html")


#--------------------------------------------------------------------------------------------

# Drill page:
@app.route("/drill/<id>", methods=['GET'])
def drill(id=None):
    return render_template("drill.html")

#--------------------------------------------------------------------------------------------

# Practice page:
@app.route("/practice/<id>", methods=['GET'])
def practice(id=None):
    return render_template("practice.html")

#--------------------------------------------------------------------------------------------

# Practice plan page:
@app.route("/practice_plan/", methods=['GET'])
def practice_plan(id=None):
    return render_template("practice_plan.html")


if __name__ == "__main__":
	app.run()
