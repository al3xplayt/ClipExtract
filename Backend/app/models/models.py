from sqlalchemy import Column, Integer, String, TIMESTAMP, text, ForeignKey
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
