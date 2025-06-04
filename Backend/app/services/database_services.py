from sqlalchemy.orm import Session
from sqlalchemy import or_
from app.models.models import *
from sqlalchemy.exc import SQLAlchemyError
from app.config import UPLOAD_FOLDER
import json

def subir_descarga(db: Session, video_url: str, filename: str, user: str, formato: str):
    try:
        email = "example@domain.com"
        usuario = db.query(Usuario).filter(
            or_(Usuario.email == email, Usuario.user == user)
        ).first()
        usuarios = db.query(Usuario).all()
        for u in usuarios:
            print(f"Usuario en DB → id: {u.id}, user: {u.user}")

        if not usuario:
            print("-"*20)
            print(type(usuario))
            print("-"*20)
            return {"success":False,"error": "Usuario no encontrado."}  # Usuario no existe
        
        nueva_descarga = DownloadHistory(usuario_id=usuario.id, video_url=video_url, filename=filename, formato=formato)
        db.add(nueva_descarga)
        db.commit()
        db.refresh(nueva_descarga)
        
        if not nueva_descarga:
            return {"success":False,"error": "Error al subir descarga."}
        return {"success":True,"message": "Descarga subida correctamente."} 
    
    except SQLAlchemyError as e:
        print(f"Error al subir descarga: {str(e)}")
        return {"success": False,"error": "Error en la base de datos al subir descarga."}  # Error general de base de datos
    
def registrar_subida(db: Session, data: json):
    user_id = data.get("user_id")
    filename = data.get("filename")
    path = UPLOAD_FOLDER + filename
    status = data.get("status", "pending")  
    try:
        # Verificar si el usuario existe
        usuario = db.query(Usuario).filter(Usuario.id == user_id).first()
        if not usuario:
            print(f"Usuario con ID {user_id} no encontrado.")
            return None  # Usuario no encontrado
        nuevo_video = Video(filename=filename, path=path, status=status, user_id=user_id)
        db.add(nuevo_video)
        db.commit()
        db.refresh(nuevo_video)
        return nuevo_video
    except SQLAlchemyError as e:
        db.rollback()  # Hacer rollback en caso de error en la base de datos
        print(f"Error al registrar subida: {str(e)}")
        return None  # Error general de base de datos
    
def registrar_clip(db: Session, data: json):
    try:
        video_id = data.get("video_id")
        video = db.query(Video).filter(Video.id == video_id).first()
        if not video:
            print(f"Video con ID {video_id} no encontrado.")
            return None  # Video no encontrado
        start_time = data.get("start_time")
        end_time = data.get("end_time")
        clip_path = data.get("clip_path")
        user_id = data.get("user_id")
        
        #Registrar el clip
        nuevo_clip = Clip(video_id=video_id, start_time=start_time, end_time=end_time, clip_path=clip_path, user_id=user_id)
        db.add(nuevo_clip)
        db.commit()
        db.refresh(nuevo_clip)
        return nuevo_clip
    except SQLAlchemyError as e:
        db.rollback()  # Hacer rollback en caso de error en la base de datos
        print(f"Error al registrar clip: {str(e)}")
        return None  # Error general de base de datos