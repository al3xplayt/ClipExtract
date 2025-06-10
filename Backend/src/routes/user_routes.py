from flask import Blueprint, request, jsonify
from app.database import SessionLocal
from app.services.user_service import *
from app.utils.query_utils import get_user
import json
user_bp = Blueprint('user', __name__)


@user_bp.route("/registro", methods=["POST"])
def registro():
    data = request.json
    db = SessionLocal()
    usuario = crear_usuario(db, data["nombre"], data["surname"], data["email"], data["contrasena"], data["username"])
    db.close()
    
    if usuario.get("message"):
        print(f"Error al crear usuario: {usuario['message']}")
        return jsonify({"success": False}), 409  # Conflicto, ya registrado

    return jsonify({"success": True}), 201  # Creado correctamente

@user_bp.route("/login", methods=["POST"])
def login():
    data = request.json
    db = SessionLocal()
    resultado = verificar_usuario(db, data["email"], data["contrasena"], data["username"])
    db.close()

    if not resultado["success"]:
        print(resultado["message"])
        return jsonify({"success": False, "message": resultado["message"]}), 401

    return jsonify({
        "success": True,
        "message": resultado["message"],
        "user_name": resultado["usuario"].user
    }), 200

@user_bp.route('/user/profile', methods=['GET'])
def get_user_profile():
    username = request.args.get('username')
    if not username:
        return jsonify({'msg': 'Falta username'}), 400
    db = SessionLocal()
    user = get_user(db, username)
    db.close()
    if not user:
        return jsonify({'msg': 'Usuario no encontrado'}), 404
    return jsonify({
        'user_name': user.user,
        'email': user.email,
    })

@user_bp.route('/user/profile', methods=['PUT'])
def update_user_profile():
    data = request.get_json()
    if not data or 'username' not in data:
        return jsonify({'msg': 'Falta username en la petición'}), 400

    db = SessionLocal()
    user = get_user(db, data.get('username'))
    print(f"Usuario encontrado: {user.user}")
    if not user:
        db.close()
        return jsonify({'msg': 'Usuario no encontrado'}), 404

    if 'user_name' in data:
        user.user = data['user_name']
    if 'email' in data:
        user.email = data['email']

    # Aquí validar campos, lógica extra si quieres

    try:
        db.commit()
    except Exception as e:
        db.rollback()
        db.close()
        return jsonify({'msg': 'Error al guardar', 'error': str(e)}), 500

    db.close()
    return jsonify({'msg': 'Perfil actualizado correctamente'})
