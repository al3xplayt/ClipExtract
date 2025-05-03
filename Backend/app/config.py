import os
from flask_cors import CORS
from flask import Flask

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir))


TEMP_FILES_DIR = os.path.join(BASE_DIR, r'data\temp_files')
UPLOAD_FOLDER = os.path.join(BASE_DIR, r'data\uploads')
DATABASE_URL = "postgresql+psycopg2://admin:admin@localhost:5432/ClipExtract"

def configure_app(app):
    for path in [TEMP_FILES_DIR, UPLOAD_FOLDER]:
        os.makedirs(path, exist_ok=True)

    app.config['SQLALCHEMY_DATABASE_URI'] = DATABASE_URL
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
    app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# Y en tu main.py o __init__.py
app = Flask(__name__)
configure_app(app)
CORS(app)
