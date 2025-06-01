from sqlalchemy import Column, Integer, String, TIMESTAMP, text, ForeignKey, Float
from app.database import Base

class Usuario(Base):
    __tablename__ = 'usuarios'

    id = Column(Integer, primary_key=True, index=True)
    nombre = Column(String(100), nullable=False)
    email = Column(String(100), nullable=False, unique=True)
    contrasena = Column(String(255), nullable=False)
    apellidos = Column(String(100), nullable=True)
    user = Column(String(36), nullable=False, unique=True)

class DownloadHistory(Base):
    __tablename__ = 'historial_descargas'
    
    id = Column(Integer, primary_key=True, index=True)
    usuario_id = Column(Integer, ForeignKey('usuarios.id'), nullable=False)
    video_url = Column(String(252), nullable=False)
    filename = Column(String(250), nullable=False)
    formato = Column(String(3), nullable=False)
    fecha_descarga = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'), nullable=False)

class Video(Base):
    __tablename__ = 'videos'
    
    id = Column(Integer, primary_key=True, index=True)
    titulo = Column(String(255), nullable=False)
    fecha_subida = Column(TIMESTAMP, server_default=text('CURRENT_TIMESTAMP'), nullable=False)
    usuario_id = Column(Integer, ForeignKey('usuarios.id'), nullable=False)
    duracion = Column(Float, nullable=False)  # Duración en segundos
    ruta = Column(String(255), nullable=False)  # Ruta del archivo de video

class Clip(Base):
    __tablename__ = 'historial_clips'
    id = Column(Integer, primary_key=True, index=True)
    usuario_id = Column(Integer, ForeignKey('usuarios.id'), nullable=False)
    video_id = Column(Integer, ForeignKey('videos.id'), nullable=False)