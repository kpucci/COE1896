import os
import datetime
import re
from flask import Flask, request, abort, url_for, redirect, session, render_template, flash
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import exc

app = Flask(__name__)
app.static_folder = 'static'

app.config.update(dict(
	DEBUG=True,
	SECRET_KEY='development key',
	USERNAME='owner',
	PASSWORD='pass',

	SQLALCHEMY_DATABASE_URI = 'sqlite:///' + os.path.join(app.root_path, 'hockey.db')
))

db.init_app(app)

#--------------------------------------------------------------------------------------------

# Initialize database
@app.cli.command('initdb')
def initdb_command():
	"""Creates the database tables."""
	db.drop_all()
	db.create_all()
	owner = Staff(firstname="Katie", lastname="Pucci", username="owner", password="pass", email="katie@kkatering.com", admin=True)
	db.session.add(owner)
	db.session.commit()
	print('Initialized the database.')

#--------------------------------------------------------------------------------------------

#--------------------------------------------------------------------------------------------

# Test database
@app.cli.command('testdb')
def testdb_command():
	"""Creates the database tables."""
	db.drop_all()
	db.create_all()

	# Add owner
	owner = Staff(firstname="Katie", lastname="Pucci", username="owner", password="pass", email="katie@kkatering.com", admin=True)
	db.session.add(owner)
	print("Added owner")

	# Create a customer
	customer_1 = Customer(username="Customer1", password="password1", email="Customer@me.com")
	print("Created customer")
	db.session.add(customer_1)
	print("Added customer")

	start_1 = datetime.date(2017,10,5)
	end_1 = datetime.date(2017,10,6)

	# Create an event
	event_1 = Event(name="Event 1", start=start_1, end=end_1)
	print("Created event")

	# Append the event to customer and add event
	customer_1.events.append(event_1)
	print("Appended event to customer")
	db.session.add(event_1)
	print("Added event")

	# Set event customer id
	event_1.customer = customer_1
	print("Set event customer id")
	print("Customer name: " + event_1.customer.username)

	# Create a staff member
	staff_1 = Staff(firstname="Jane",
			lastname="Doe", username="janedoe1",
			password="janedoe1",
			email="jane.doe@me.com",
			admin=False
		)
	print("Created staff member 1")
	db.session.add(staff_1)
	print("Added staff member 1")

	# Create another staff member
	staff_2 = Staff(firstname="John",
			lastname="Deer", username="johndeer1",
			password="johndeer1",
			email="john.deer@me.com",
			admin=False
		)
	print("Created staff member 2")
	db.session.add(staff_2)
	print("Added staff member 2")

	# Append event to staff_1's schedule
	staff_1.schedule.append(event_1)

	# Append staff member to event
	# event_1.workers.append(staff_1)

	# Append event to staff_1's schedule
	staff_2.schedule.append(event_1)

	# Append staff member to event
	# event_1.workers.append(staff_2)

	start_2 = datetime.date(2017,11,5)
	end_2 = datetime.date(2017,11,6)

	# Create another event
	event_2 = Event(name="Event 2", start=start_2, end=end_2)
	print("Created event 2")

	# Append the event to customer and add event
	customer_1.events.append(event_2)
	print("Appended event 2 to customer")
	db.session.add(event_2)
	print("Added event 2")

	# Set event customer id
	event_2.customer = customer_1
	print("Set event customer id")

	# Append event to staff_1's schedule
	# staff_1.schedule.append(event_2)

	# Append staff member to event
	# event_2.workers.append(staff_1)

	db.session.commit()
	print('Initialized the database.')

	staff1 = Staff.query.filter_by(username="janedoe1").first()
	staff1_events = staff_1.schedule
	for e in staff1_events:
		print("Event:  ", e.name)

	event1 = Event.query.filter_by(name="Event 1").first()
	event1_staff = event1.workers
	for w in event1_staff:
		print("Staff: ", w.username)


#--------------------------------------------------------------------------------------------

# Start page:
# GET - Visit home page
# POST -
@app.route("/", methods=['GET','POST'])
def start():
    # TODO: Check cache for login status and redirect accordingly
    return render_template("start.html")

#--------------------------------------------------------------------------------------------

# Login page:
@app.route("/login", methods=['GET'])
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
@app.route("/register", methods=['GET'])
def register():
    return render_template("register.html")


#--------------------------------------------------------------------------------------------

# Player profile page:
@app.route("/player/<username>", methods=['GET'])
def player_profile(username=None):
    return render_template("player_profile.html")

#--------------------------------------------------------------------------------------------

# Coach profile page:
@app.route("/coach/<username>", methods=['GET'])
def coach_profile(username=None):
    return render_template("coach_profile.html")

#--------------------------------------------------------------------------------------------

# Parent profile page:
@app.route("/parent/<username>", methods=['GET'])
def parent_profile(username=None):
    return render_template("parent_profile.html")

#--------------------------------------------------------------------------------------------

# Drill catalog page:
@app.route("/catalog", methods=['GET'])
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
