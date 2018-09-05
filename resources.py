from flask_restful import Resource, fields, reqparse, marshal_with, inputs
from flask import request, abort, flash, jsonify, json
from models import db, Player, Coach, Parent

player_fields = {
    'id': fields.Integer,
	'email': fields.String,
    'password': fields.String,
    'first_name': fields.String,
    'last_name': fields.String,
    'hockey_level': fields.Integer,
    'skill_level': fields.Integer,
    'hand': fields.Boolean
}

coach_fields = {
    'id': fields.Integer,
	'email': fields.String,
    'password': fields.String,
    'first_name': fields.String,
    'last_name': fields.String
}

parent_fields = {
    'id': fields.Integer,
    'email': fields.String,
    'password': fields.String,
    'first_name': fields.String,
    'last_name': fields.String,
    'child_name': fields.String
}

player_parser = reqparse.RequestParser(bundle_errors=True)
player_parser.add_argument('id', type=int, location='json')
player_parser.add_argument('email', type=str, required=True, location='json')
player_parser.add_argument('password', type=str, required=True, location='json')
player_parser.add_argument('first_name', type=str, required=True, location='json')
player_parser.add_argument('last_name', type=str, required=True, location='json')
player_parser.add_argument('hockey_level', type=int, required=True, location='json')
player_parser.add_argument('skill_level', type=int, required=True, location='json')
player_parser.add_argument('hand', type=inputs.boolean, required=True, location='json')

coach_parser = reqparse.RequestParser(bundle_errors=True)
coach_parser.add_argument('id', type=int, location='json')
coach_parser.add_argument('email', type=str, required=True, location='json')
coach_parser.add_argument('password', type=str, required=True, location='json')
coach_parser.add_argument('first_name', type=str, required=True, location='json')
coach_parser.add_argument('last_name', type=str, required=True, location='json')

parent_parser = reqparse.RequestParser(bundle_errors=True)
parent_parser.add_argument('id', type=int, location='json')
parent_parser.add_argument('email', type=str, required=True, location='json')
parent_parser.add_argument('password', type=str, required=True, location='json')
parent_parser.add_argument('first_name', type=str, required=True, location='json')
parent_parser.add_argument('last_name', type=str, required=True, location='json')
parent_parser.add_argument('child_name', type=str, location='json')

class PlayerResource(Resource):
    @marshal_with(player_fields)
    def get(self, id):
        player = Player.query.filter_by(id=id).first()

        if not player:
            abort(404, "Player %d: not found." % id)

        return player

class PlayerListResource(Resource):
    @marshal_with(player_fields)
    def get(self):
        return Player.query.all()

    @marshal_with(player_fields)
    def post(self):
        # Get arguments from request
        args = player_parser.parse_args()

        if 'id' not in args:
            highest = Player.query.order_by(Player.id).last()
            player_id = highest + 1
        else:
            player_id = args['id']

        player = Player(id=player_id, email=args['email'], password=args['password'],
            first_name=args['first_name'], last_name=args['last_name'], hockey_level=args['hockey_level'],
            skill_level=args['skill_level'], hand=args['hand'])

        db.session.add(player)
        db.session.commit()

        return player, 201

class CoachResource(Resource):
    @marshal_with(coach_fields)
    def get(self, id):
        coach = Coach.query.filter_by(id=id).first()

        if not coach:
            abort(404, "Coach %s: not found." % id)

        return coach

class CoachListResource(Resource):
    @marshal_with(coach_fields)
    def get(self):
        return Coach.query.all()

    @marshal_with(coach_fields)
    def post(self):
        args = coach_parser.parse_args()

        if 'id' not in args:
            highest = Coach.query.order_by(Coach.id).last()
            coach_id = highest + 1
        else:
            coach_id = args['id']

        coach = Coach(id=coach_id, email=args['email'], password=args['password'],
            first_name=args['first_name'], last_name=args['last_name'])

        db.session.add(coach)
        db.session.commit()

        return coach, 201

class ParentResource(Resource):
    @marshal_with(parent_fields)
    def get(self, id):
        parent = Parent.query.filter_by(id=id).first()

        if not parent:
            abort(404, "Parent %s: not found." % id)

        return parent

class ParentListResource(Resource):
    @marshal_with(parent_fields)
    def get(self):
        return Parent.query.all()

    @marshal_with(parent_fields)
    def post(self):
        args = parent_parser.parse_args()

        if 'id' not in args:
            highest = Parent.query.order_by(Parent.id).last()
            parent_id = highest + 1
        else:
            parent_id = args['id']

        parent = Parent(id=parent_id, email=args['email'], password=args['password'],
            first_name=args['first_name'], last_name=args['last_name'])

        # TODO: Search for child's name

        db.session.add(parent)
        db.session.commit()

        return parent, 201
