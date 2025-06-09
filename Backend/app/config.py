import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent

TEMP_FILES_DIR = BASE_DIR / 'data' / 'temp_files'
UPLOAD_FOLDER = BASE_DIR / 'data' / 'uploads'
TEMP_CLIPS_DIR = BASE_DIR / 'data' / 'temp_clips'
DATABASE_URL = os.getenv("DATABASE_URL", "postgresql+psycopg2://admin:admin@localhost:5432/ClipExtract")

def configure_app(app):
    for path in [TEMP_FILES_DIR, UPLOAD_FOLDER, TEMP_CLIPS_DIR]:
        os.makedirs(path, exist_ok=True)
    app.config['SQLALCHEMY_DATABASE_URI'] = DATABASE_URL
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

