from sqlalchemy.orm import Session
from sqlalchemy import or_
from src.models.models import *
from werkzeug.security import generate_password_hash, check_password_hash
from sqlalchemy.exc import SQLAlchemyError
import json

def crear_usuario(db: Session, nombre: str, apellido: str, email: str, contrasena: str, user: str):
    try:
        usuario_en_uso = db.query(Usuario).filter(Usuario.user == user).first()
        correo_en_uso = db.query(Usuario).filter(Usuario.email == email).first()

        if usuario_en_uso:
            return {"success": False, "message": "El nombre de usuario ya está en uso."}
        if correo_en_uso:
            return {"success": False, "message": "El correo electrónico ya está en uso."}

        hash_pass = generate_password_hash(contrasena)
        nuevo_usuario = Usuario(nombre=nombre, apellidos=apellido, email=email, contrasena=hash_pass, user=user)
        db.add(nuevo_usuario)
        db.commit()
        db.refresh(nuevo_usuario)

        return {"success": True, "usuario": nuevo_usuario}
    except SQLAlchemyError as e:
        db.rollback()
        print(f"Error al crear usuario: {str(e)}")
        return {"success": False, "message": "Error en la base de datos al crear usuario."}


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
    
def getUserInfo(db: Session, user_id: int):
    try:
        usuario = db.query(Usuario).filter(Usuario.id == user_id).first()
        if not usuario:
            return {"success": False, "message": "Usuario no encontrado."}  # Usuario no existe
        return {"success": True, "usuario": usuario}
    except SQLAlchemyError as e:
        print(f"Error al obtener usuario por ID: {str(e)}")
        return {"success": False, "message": "Error en la base de datos al obtener usuario por ID."}  # Error general de base de datos

def updateUserInfo(db: Session, user_id: int, data: json):
    try:
        usuario = db.query(Usuario).filter(Usuario.id == user_id).first()
        if not usuario:
            return {"success": False, "message": "Usuario no encontrado."}  # Usuario no existe

        for key, value in data.items():
            setattr(usuario, key, value)

        db.commit()
        db.refresh(usuario)
        return {"success": True, "usuario": usuario}
    except SQLAlchemyError as e:
        db.rollback()  # Hacer rollback en caso de error en la base de datos
        print(f"Error al actualizar usuario: {str(e)}")
        return {"success": False, "message": "Error en la base de datos al actualizar usuario."}  # Error general de base de datos



