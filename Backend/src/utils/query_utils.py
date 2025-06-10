from sqlalchemy.orm import Session
from sqlalchemy import or_
from app.models.models import *
from sqlalchemy.exc import SQLAlchemyError


def get_user(db: Session, username: str):
    """Retrieve user ID by username."""
    user = db.query(Usuario).filter(Usuario.user == username).first()
    
    if not user:
        return None
    return user