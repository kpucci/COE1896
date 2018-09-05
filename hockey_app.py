import os
import datetime
import re
from flask import Flask, request, abort, url_for, redirect, session, render_template, flash
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import exc
from resources import (
    PlayerResource,
    PlayerListResource,
    CoachResource,
    CoachListResource,
    ParentResource,
    ParentListResource
)
from flask_restful import (
    reqparse,
    abort,
    Api,
    Resource
)

from models import (
    db,
    Player,
    Coach,
    Parent
)

app = Flask(__name__)
app.static_folder = 'static'
api = Api(app)

app.config.update(dict(
    DEBUG=True,
    SECRET_KEY='development key',
    USERNAME='owner',
    PASSWORD='pass',

    SQLALCHEMY_DATABASE_URI = 'sqlite:///' + os.path.join(app.root_path, 'hockey.db')
))

db.init_app(app)

#---------------------------------------------------------
# Resources:

api.add_resource(PlayerListResource, '/players')
api.add_resource(PlayerResource, '/players/<int:id>')
api.add_resource(CoachListResource, '/coaches')
api.add_resource(CoachResource, '/coaches/<int:id>')
api.add_resource(ParentListResource, '/parents')
api.add_resource(ParentResource, '/parents/<int:id>')

#--------------------------------------------------------------------------------------------

# Initialize database
@app.cli.command('initdb')
def initdb_command():
    """Creates the database tables."""
    db.drop_all()
    db.create_all()
    print('Initialized the database.')

#--------------------------------------------------------------------------------------------
#--------------------------------------------------------------------------------------------

# Login page:
@app.route("/", methods=['GET','POST'])
def login():
    # TODO: Add authentication
    # TODO: Remember me functionality

    if request.method == "GET":
        if "logged_in" in session:
            return render_template("login.html", loggedIn=True)
        else:
            return render_template("login.html", loggedIn=False)
    elif request.method == "POST":
        player = Player.query.filter(Player.email.like(request.form["email"]), Player.password.like(request.form["password"])).scalar()
        coach = Coach.query.filter(Coach.email.like(request.form["email"]), Coach.password.like(request.form["password"])).scalar()
        parent = Parent.query.filter(Parent.email.like(request.form["email"]), Parent.password.like(request.form["password"])).scalar()

        if player is not None:
            session['logged_in'] = request.form["email"]
            return redirect(url_for("player_profile", id=player.id))
        elif coach is not None:
            session['logged_in'] = request.form["email"]
            return redirect(url_for("coach_profile", id=coach.id))
        elif parent is not None:
            session['logged_in'] = request.form["email"]
            return redirect(url_for("parent_profile", id=parent.id))
        else:
            flash('Incorrect email or password')
            return render_template("login.html", loggedIn=False)
    else:
        return render_template("login.html", loggedIn=False)

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
def catalog():
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
