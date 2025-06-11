# main.py
from flask import Blueprint, request, jsonify
from src.database import SessionLocal
from src.services.database_services import *

database_bp = Blueprint('database', __name__)

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
    db.close()
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
    db.close()
    return jsonify({"success": True, "message": "Historial eliminado"}), 200

@database_bp.route("/upload/register", methods=["POST"])
def register_upload():
    db = SessionLocal()
    data = request.get_json()
    if not data:
        return jsonify({"success": False, "message": "Datos JSON requeridos"}), 400

    username = data.get("username")
    if not username:
        return jsonify({"success": False, "message": "user_id es obligatorio"}), 400

    usuer_id = db.query(Usuario).filter(Usuario.user == username).first()
    data["user_id"] = usuer_id.id if usuer_id else None
    nuevo_video = None
    try:
        # Aquí reutilizamos el servicio
        nuevo_video = registrar_subida(db, data)
        if not nuevo_video:
            return jsonify({"success": False, "message": "Usuario no encontrado o error al registrar subida"}), 404
        
        return jsonify({
            "success": True,
            "video": {
                "id": nuevo_video.id,
                "filename": nuevo_video.filename,
                "path": nuevo_video.path,
                "status": nuevo_video.status,
                "user_id": nuevo_video.user_id
            }
        }), 201
    except SQLAlchemyError as e:
        db.rollback()
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        db.close()

@database_bp.route("/clip/register", methods=["POST"])
def register_clip():
    db = SessionLocal()
    data = request.get_json()
    if not data:
        return jsonify({"success": False, "message": "Datos JSON requeridos"}), 401

    video_filename = data.get("filename")
    if not video_filename:
        return jsonify({"success": False, "message": "filename es obligatorio"}), 402

    try:
        nuevo_clip = registrar_clip(db, data)
        if not nuevo_clip:
            return jsonify({"success": False, "message": "Video no encontrado o error al registrar clip"}), 404
        
        return jsonify({
            "success": True,
            "clip": {
                "id": nuevo_clip.id,
                "video_id": nuevo_clip.video_id,
                "start_time": nuevo_clip.start_time,
                "end_time": nuevo_clip.end_time,
                "clip_path": nuevo_clip.clip_path,
                "user_id": nuevo_clip.user_id
            }
        }), 201
    except SQLAlchemyError as e:
        db.rollback()
        return jsonify({"success": False, "message": str(e)}), 500
    finally:
        db.close()

