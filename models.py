from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash

# Instantiate a database object
db = SQLAlchemy()

# Player has many teams
# Team has many players --> Many-to-many
teams1 = db.Table('teams1',
    db.Column('player_id', db.Integer, db.ForeignKey('player.id')),
    db.Column('team_id', db.Integer, db.ForeignKey('team.id'))
)

players = db.Table('players',
    db.Column('team_id', db.Integer, db.ForeignKey('team.id')),
    db.Column('player_id', db.Integer, db.ForeignKey('player.id'))
)

# Coach has many teams
# Team has many coaches --> Many-to-many
coaches = db.Table('coaches',
    db.Column('team_id', db.Integer, db.ForeignKey('team.id')),
    db.Column('coach_id', db.Integer, db.ForeignKey('coach.id'))
)

teams2 = db.Table('teams2',
    db.Column('coach_id', db.Integer, db.ForeignKey('coach.id')),
    db.Column('team_id', db.Integer, db.ForeignKey('team.id'))
)

# Playlist has many drills
# Drill has many playlists --> Many-to-many
drills = db.Table('drills',
    db.Column('playlist_id', db.Integer, db.ForeignKey('playlist.id')),
    db.Column('drill_id', db.Integer, db.ForeignKey('drill.id'))
)

playlists = db.Table('playlists',
    db.Column('drill_id', db.Integer, db.ForeignKey('drill.id')),
    db.Column('playlist_id', db.Integer, db.ForeignKey('playlist.id'))
)


class Player(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    email = db.Column(db.String(80), unique = True, nullable = False)
    password = db.Column(db.String(100), nullable = False)

    first_name = db.Column(db.String(80), nullable = False)
    last_name = db.Column(db.String(80), nullable = False)

    hockey_level = db.Column(db.Integer)
    skill_level = db.Column(db.Integer)
    hand = db.Column(db.Boolean)

    # Playlist has one player
    # Player has one playlist --> One-to-one
    playlist_id = db.Column(db.Integer, db.ForeignKey('playlist.id'))
    playlist = db.relationship("Playlist", backref=db.backref("player", uselist=False))

    # Many-to-many
    teams1 = db.relationship('Team', secondary='teams1',lazy='dynamic', backref='player')

    # Practice has one drill
    # Drill has many practices --> Many-to-one
    parent_id = db.Column(db.Integer, db.ForeignKey('parent.id'))
    parent = db.relationship("Parent", backref="players")

    # NOTE: Backref to practices

    def _repr_(self):
        return "<Player {}>".format(repr(self.id))

    def __init__(self, id, email, password, first_name, last_name, hockey_level, skill_level, hand):
        self.id = id
        self.email = email
        self.set_password(password)
        self.first_name = first_name
        self.last_name = last_name
        self.hockey_level = hockey_level
        self.skill_level = skill_level
        self.hand = hand

    def set_password(self, password):
        self.password = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password, password)

class Coach(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    email = db.Column(db.String(80), unique = True, nullable = False)
    password = db.Column(db.String(100), nullable = False)

    first_name = db.Column(db.String(80), nullable = False)
    last_name = db.Column(db.String(80), nullable = False)

    # Many-to-many
    teams2 = db.relationship('Team', secondary='teams2',lazy='dynamic', backref='coach')

    def _repr_(self):
        return "<Coach {}>".format(repr(self.id))

    def __init__(self, id, email, password, first_name, last_name):
        self.id = id
        self.email = email
        self.set_password(password)
        self.first_name = first_name
        self.last_name = last_name

    def set_password(self, password):
        self.password = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password, password)


class Parent(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    email = db.Column(db.String(80), unique = True, nullable = False)
    password = db.Column(db.String(100), nullable = False)

    first_name = db.Column(db.String(80), nullable = False)
    last_name = db.Column(db.String(80), nullable = False)

    # NOTE: Backref to players

    def _repr_(self):
        return "<Parent {}>".format(repr(self.id))

    def __init__(self, id, email, password, first_name, last_name):
        self.id = id
        self.email = email
        self.set_password(password)
        self.first_name = first_name
        self.last_name = last_name

    def set_password(self, password):
        self.password = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password, password)

class Drill(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    name = db.Column(db.String(80), nullable = False)
    description = db.Column(db.String(200))
    image = db.Column(db.String(100))

    # Many-to-many
    playlists = db.relationship('Playlist', secondary='playlists',lazy='dynamic', backref='drill')

    def _repr_(self):
        return "<Drill {}>".format(repr(self.id))

class Playlist(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    # NOTE: Backref to player

    # Many-to-many
    drills = db.relationship('Drill', secondary='drills',lazy='dynamic',backref='playlist')

    def _repr_(self):
        return "<Playlist {}>".format(repr(self.id))

class Practice(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)

    # Practice has one player
    # Player has many practices --> Many-to-one
    player_id = db.Column(db.Integer, db.ForeignKey('player.id'))
    player = db.relationship("Player", backref="practices")

    # Practice has one drill
    # Drill has many practices --> Many-to-one
    drill_id = db.Column(db.Integer, db.ForeignKey('drill.id'))
    drill = db.relationship("Drill")

    score = db.Column(db.Integer)
    timestamp = db.Column(db.DateTime)
    duration = db.Column(db.Integer)    # In seconds

    def _repr_(self):
        return "<Practice {}>".format(repr(self.id))

class Team(db.Model):
    id = db.Column(db.Integer, unique=True, primary_key=True)
    name = db.Column(db.String(80), nullable = False)
    league = db.Column(db.String(80), nullable = False)
    division = db.Column(db.String(80), nullable = False)

    # Many-to-many
    coaches = db.relationship('Coach', secondary='coaches',lazy='dynamic',backref='team')

    # Many-to-many
    players = db.relationship('Player', secondary='players',lazy='dynamic',backref='team')

    def _repr_(self):
        return "<Team {}>".format(repr(self.id))
