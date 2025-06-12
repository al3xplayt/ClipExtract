from flask import Blueprint, request, jsonify, send_file
from pathlib import Path
from app.services.download_service import download_video
import sys
import os

sys.path.append(os.path.abspath(os.path.dirname(__file__)))
from app.services.download_service import schedule_delete


download_bp = Blueprint('download', __name__)

@download_bp.route('/download', methods=['POST'])
def download_page():
    if request.method == 'POST':
        data = request.get_json()  # Obtener los datos JSON
        url = data.get('url')
        formato = data.get('format')  # "mp3" o "mp4"
        formato = formato.lower()
        if not url:
            return "No se proporcionó un enlace válido", 400

        try:
            file_path = download_video(url, formato)
            print(file_path)
            if file_path and os.path.exists(file_path):
                file = Path("data/temp_files") / os.path.basename(file_path)
                filename = os.path.basename(file_path)  # Nombre del archivo descargado
                type = 'audio/mpeg' if formato == 'mp3' else 'video/mp4'
                return send_file(file, as_attachment=True, download_name=filename, mimetype=type)
            else:
                return "No se pudo procesar el archivo.", 501
        except Exception as e:
            return str(e), 505

@download_bp.route('/delete_file', methods=['POST'])
def delete():
    data = request.get_json()
    file_name = data.get('file_name')
    if not file_name:
        return jsonify({"error": "No se proporcionó nombre de archivo"}), 400
    schedule_delete(file_name)
    return jsonify({"message": f"Archivo '{file_name}' programado para eliminación"}), 200

