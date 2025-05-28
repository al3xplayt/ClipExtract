from sqlalchemy.orm import Session
from sqlalchemy import or_
from app.models.models import *
from werkzeug.security import generate_password_hash, check_password_hash
from sqlalchemy.exc import SQLAlchemyError

def crear_usuario(db: Session, nombre: str, apellido: str, email: str, contrasena: str, user: str):
    try:
        # Verifica si ya existe el usuario
        usuario_existente = db.query(Usuario).filter(
            or_(Usuario.email == email, Usuario.user == user)
        ).first()
        if usuario_existente:
            return {"error": "Ya existe un usuario con ese email o nombre de usuario."} 

        hash_pass = generate_password_hash(contrasena)

        nuevo_usuario = Usuario(nombre=nombre,apellidos=apellido, email=email, contrasena=hash_pass, user=user)
        db.add(nuevo_usuario)
        db.commit()
        db.refresh(nuevo_usuario)

        return nuevo_usuario
    except SQLAlchemyError as e:
        db.rollback()  # Hacer rollback en caso de error en la base de datos
        print(f"Error al crear usuario: {str(e)}")
        return {"error": "Error en la base de datos al crear usuario."}  # Error general de base de datos


def verificar_usuario(db: Session, email: str, contrasena: str, user: str):
    try:
        # Consultar el usuario
        usuario = db.query(Usuario).filter(
            or_(Usuario.email == email, Usuario.user == user)
        ).first()
        if not usuario:
            return {"success": False, "message": "Usuario no encontrado.", "usuario" : usuario}  # Usuario no existe

        # Verificar la contraseña
        if not check_password_hash(usuario.contrasena, contrasena):
            return {"success": False, "message": "Contraseña incorrecta.", "usuario" : usuario}  # Contraseña incorrecta

        return {"success": True, "message": f"Bienvenido {usuario.user}", "usuario": usuario}
    except SQLAlchemyError as e:
        print(f"Error al verificar usuario: {str(e)}")
        return {"success":False,"message": "Error en la base de datos al verificar usuario."}  # Error general de base de datos

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
    
def registrar_clip(db: Session, video_url: str, filename: str, user: str, formato: str):
    try:
        usuario = db.query(Usuario).filter(Usuario.user == user).first()
        if not usuario:
            return {"success": False, "error": "Usuario no encontrado."}  # Usuario no existe
        
        nuevo_clip = ClipHistory(usuario_id=usuario.id, video_url=video_url, filename=filename, formato=formato)
        db.add(nuevo_clip)
        db.commit()
        db.refresh(nuevo_clip)
        
        return {"success": True, "message": "Clip registrado correctamente."}
    
    except SQLAlchemyError as e:
        print(f"Error al registrar clip: {str(e)}")
        return {"success": False, "error": "Error en la base de datos al registrar clip."}  # Error general de base de datos