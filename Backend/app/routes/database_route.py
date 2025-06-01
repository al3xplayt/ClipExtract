# main.py
from flask import Blueprint, request, jsonify
from app.database import SessionLocal
from app.services.database_services import *

database_bp = Blueprint('database', __name__)

@database_bp.route("/registro", methods=["POST"])
def registro():
    data = request.json
    db = SessionLocal()
    usuario = crear_usuario(db, data["nombre"], data["surname"], data["email"], data["contrasena"], data["username"])
    db.close()
    
    if usuario.get("error"):
        print(f"Error al crear usuario: {usuario['error']}")
        return jsonify({"success": False}), 409  # Conflicto, ya registrado

    return jsonify({"success": True}), 201  # Creado correctamente

@database_bp.route("/login", methods=["POST"])
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

@database_bp.route("/register_download", methods=["POST"])
def upload_download():
    data = request.json
    db = SessionLocal()
    resultado = subir_descarga(db, data["url"],  data["filename"], data["username"], data["formato"])
    db.close()

    if not resultado["success"]:
        print(f"Usuario {data['username']}")
        print(data["url"])
        print(resultado["error"])
        print("_"*20)
        return jsonify({"success": False, "message": resultado["error"]}), 404

    return jsonify({"success": True, "message": resultado["message"]}), 200

@database_bp.route("/history/<username>", methods=["GET"])
def get_user_history(username):
    db = SessionLocal()
    user = db.query(Usuario).filter(Usuario.user == username).first()
    if not user:
        print(f"Usuario {username} no encontrado")
        return jsonify({"success": False, "message": "Usuario no encontrado"}), 404
    user_id = db.query(Usuario).filter(Usuario.user == username).first().id
    history = db.query(DownloadHistory).filter(DownloadHistory.usuario_id == user_id).all()
    
    result = [
        {
            "video_url": h.video_url,
            "filename": h.filename,
            "formato": h.formato,
            "fecha_descarga": h.fecha_descarga.strftime("%Y-%m-%d %H:%M:%S"),
        } for h in history
    ]
    return jsonify({"success": True, "history": result}), 200

@database_bp.route("/history/<username>/delete", methods=["DELETE"])
def delete_user_history(username):
    db = SessionLocal()
    user = db.query(Usuario).filter(Usuario.user == username).first()
    if not user:
        return jsonify({"success": False, "message": "Usuario no encontrado"}), 404
    
    user_id = db.query(Usuario).filter(Usuario.user == username).first().id
    db.query(DownloadHistory).filter(DownloadHistory.usuario_id == user_id).delete()
    db.commit()
    return jsonify({"success": True, "message": "Historial eliminado"}), 200

@database_bp.route("/regist_clips", methods=["POST"])
def regist_clips():
    data = request.json
    db = SessionLocal()
    resultado = registrar_clips(db, data["url"], data["username"], data["formato"])
    db.close()

    if not resultado["success"]:
        print(f"Usuario {data['username']}")
        print(data["url"])
        print(resultado["error"])
        print("_"*20)
        return jsonify({"success": False, "message": resultado["error"]}), 404

    return jsonify({"success": True, "message": resultado["message"]}), 200

