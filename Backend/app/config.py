import os

TEMP_FILES_DIR =  'data/temp_files'
UPLOAD_FOLDER = 'data/uploads'
TEMP_CLIPS_DIR = 'data/temp_clips'
#DATABASE_URL = "postgresql+psycopg2://admin:admin@localhost:5432/ClipExtract" For local development
DATABASE_URL = os.environ.get("DATABASE_URL")  # For Docker deployment

def configure_app(app):
    for path in [TEMP_FILES_DIR, UPLOAD_FOLDER, TEMP_CLIPS_DIR]:
        os.makedirs(path, exist_ok=True)
    print("created folders:", TEMP_CLIPS_DIR, TEMP_FILES_DIR, UPLOAD_FOLDER)
    app.config['SQLALCHEMY_DATABASE_URI'] = DATABASE_URL
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

