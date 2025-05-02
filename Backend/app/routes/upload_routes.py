from flask import Blueprint, request, jsonify
from werkzeug.utils import secure_filename
import os

# Crear un Blueprint para las rutas de carga de archivos
upload_bp = Blueprint('upload_bp', __name__)

# Configuración de la carpeta de subida (esto puede ser movido a un archivo de configuración)
UPLOAD_FOLDER = 'Backend/uploads'
ALLOWED_EXTENSIONS = {'mp4', 'mp3', 'wav', 'avi', 'mov'}
MAX_CONTENT_LENGTH = 50 * 1024 * 1024  # Límite de 50MB por archivo

# Configurar el Blueprint
upload_bp.config = {
    'UPLOAD_FOLDER': UPLOAD_FOLDER,
    'MAX_CONTENT_LENGTH': MAX_CONTENT_LENGTH
}

# Asegurarse de que la carpeta de subida existe
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def allowed_file(filename):
    """Verificar si el archivo tiene una extensión permitida"""
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@upload_bp.route('/upload', methods=['POST'])
def upload_file():
    """Ruta para subir un archivo"""
    if 'file' not in request.files:
        return jsonify({"error": "No se envió ningún archivo"}), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({"error": "Nombre de archivo vacío"}), 400

    if not allowed_file(file.filename):
        return jsonify({"error": "Archivo no permitido"}), 400

    filename = secure_filename(file.filename)
    file_path = os.path.join(UPLOAD_FOLDER, filename)
    file.save(file_path)

    # Aquí puedes agregar lógica adicional para procesar el archivo si es necesario
    print(f"Archivo recibido y guardado en: {file_path}")

    return jsonify({"message": "Archivo subido exitosamente", "filename": filename}), 200
