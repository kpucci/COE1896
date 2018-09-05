from flask_sqlalchemy import SQLAlchemy

# Instantiate a database object
db = SQLAlchemy()

class Player(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    email = db.Column(db.String(80), unique = True)
    password = db.Column(db.String(100))

    first_name = db.Column(db.String(80))
    last_name = db.Column(db.String(80))

    hockey_level = db.Column(db.Integer)
    skill_level = db.Column(db.Integer)
    age = db.Column(db.Integer)
    hand = db.Column(db.Boolean)

    def _repr_(self):
        return "<Player {}>".format(repr(self.email))

class Coach(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    email = db.Column(db.String(80), unique = True)
    password = db.Column(db.String(100))

    first_name = db.Column(db.String(80))
    last_name = db.Column(db.String(80))

    def _repr_(self):
        return "<Coach {}>".format(repr(self.email))

class Parent(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    email = db.Column(db.String(80), unique = True)
    password = db.Column(db.String(100))

    first_name = db.Column(db.String(80))
    last_name = db.Column(db.String(80))

    # TODO: Add child reference

    def _repr_(self):
        return "<Parent {}>".format(repr(self.email))
