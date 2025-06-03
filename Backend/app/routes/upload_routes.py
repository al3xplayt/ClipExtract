from flask import Blueprint, request, jsonify
from werkzeug.utils import secure_filename
from app.config import UPLOAD_FOLDER
import os

upload_bp = Blueprint('upload_bp', __name__)

ALLOWED_EXTENSIONS = {'mp4'}
MAX_CONTENT_LENGTH = 50 * 1024 * 1024  # 50MB

os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@upload_bp.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return jsonify({"error": "No se envió ningún archivo"}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({"error": "Nombre de archivo vacío"}), 400

    if not allowed_file(file.filename):
        return jsonify({"error": "Archivo no permitido"}), 400

    filename = secure_filename(file.filename)
    file_path = os.path.join(UPLOAD_FOLDER, filename)
    print(f"Ruta del archivo: {file_path} con nombre: {filename}")
    file.save(file_path)

    return jsonify({
        "message": "Archivo subido exitosamente",
        "filename": filename
    }), 200
