# main.py
from flask import Blueprint, request, jsonify
from app.database import SessionLocal
from app.services.database_services import crear_usuario, verificar_usuario

database_bp = Blueprint('database', __name__)
@database_bp.route("/registro", methods=["POST"])
def registro():
    data = request.json
    db = SessionLocal()
    usuario = crear_usuario(db, data["nombre"], data["email"], data["contrasena"])
    db.close()
    if not usuario:
        return jsonify({"mensaje": "Usuario ya registrado"}), 409
    return jsonify({"mensaje": "Usuario creado"}), 201

# Endpoint para login
@database_bp.route("/login", methods=["POST"])
def login():
    data = request.json
    db = SessionLocal()
    usuario = verificar_usuario(db, data["email"], data["contrasena"])
    db.close()
    if not usuario:
        return jsonify({"mensaje": "Credenciales incorrectas"}), 401
    return jsonify({"mensaje": "Inicio de sesión exitoso", "usuario": usuario.email}), 200