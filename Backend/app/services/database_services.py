# app/auth.py
from sqlalchemy.orm import Session
from app.models import Usuario
from werkzeug.security import generate_password_hash, check_password_hash

def crear_usuario(db: Session, nombre: str, email: str, contrasena: str):
    # Verifica si ya existe el usuario
    usuario_existente = db.query(Usuario).filter(Usuario.email == email).first()
    if usuario_existente:
        return None  # ya existe

    hash_pass = generate_password_hash(contrasena)
    nuevo_usuario = Usuario(nombre=nombre, email=email, contrasena=hash_pass)
    db.add(nuevo_usuario)
    db.commit()
    db.refresh(nuevo_usuario)
    return nuevo_usuario

def verificar_usuario(db: Session, email: str, contrasena: str):
    usuario = db.query(Usuario).filter(Usuario.email == email).first()
    if not usuario:
        return None

    if not check_password_hash(usuario.contrasena, contrasena):
        return None

    return usuario  # autenticado correctamente
