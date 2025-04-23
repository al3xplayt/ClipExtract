"""
Author: Alejandro
Version: 1.0
Date: February 2025
"""

from flask import Flask, render_template, request, send_file, after_this_request, jsonify
from werkzeug.utils import secure_filename
import os, yt_dlp, ffmpeg, threading, time
from pathlib import Path
from flask_cors import CORS

# Configuración de Flask
app = Flask(__name__)
CORS(app)  # Permitir CORS para solicitudes desde otros dominios
app.config['MAX_CONTENT_LENGTH'] = 50 * 1024 * 1024  # Límite de 50MB por archivo

# Directorios de archivos
TEMP_DIR = 'Backend/temp_audio'
os.makedirs(TEMP_DIR, exist_ok=True)

TEMP_DIR_FILES = 'Backend/temp_files'
os.makedirs(TEMP_DIR_FILES, exist_ok=True)

UPLOAD_FOLDER = 'Backend/uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

ALLOWED_EXTENSIONS = {'mp4', 'mp3', 'wav', 'avi', 'mov'}

def is_file_in_use(file_path):
    try:
        # Intentamos renombrar el archivo para ver si está en uso
        os.rename(file_path, file_path)
        return False
    except OSError:
        return True

def allowed_file(filename):
    """Verificar si el archivo tiene una extensión permitida"""
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def download_video(url, formato):
    try:
        # Definir opciones para yt-dlp según el formato
        if formato == 'mp3':
            ydl_opts = {
                'format': 'bestaudio/best',
                'outtmpl': os.path.join(TEMP_DIR, '%(title)s.%(ext)s'),
                'noplaylist': True,
            }
        else:  # Descargar como MP4
            ydl_opts = {
                'format': 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]',
                'outtmpl': os.path.join(TEMP_DIR, '%(title)s.%(ext)s'),
                'noplaylist': True,
            }
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])

        # Buscar el archivo descargado
        filename = None
        for file in os.listdir(TEMP_DIR):
            if formato == 'mp3' and file.endswith(('.mp4', '.webm', '.m4a', '.flv')):
                filename = os.path.join(TEMP_DIR, file)
                break
            elif formato == 'mp4' and file.endswith('.mp4'):
                filename = os.path.join(TEMP_DIR, file)
                break

        if not filename:
            raise Exception("No se encontró un archivo descargado.")

        # Si es MP3, convertirlo
        if formato == 'mp3':
            mp3_file = os.path.join(TEMP_DIR, f"{os.path.splitext(os.path.basename(filename))[0]}.mp3")
            try:
                ffmpeg.input(filename).output(mp3_file, audio_bitrate='192k').run(overwrite_output=True)
                os.remove(filename)
            except Exception as e:
                print(f"Error al convertir a MP3: {e}")
                return f"Error al convertir a MP3: {e}"
            return mp3_file
        else:
            return filename  # Retornar el archivo MP4 directamente

    except Exception as e:
        raise e

def remove_file(file_path):
    try:
        os.remove(file_path)
        print(f"Archivo eliminado: {file_path}")
    except Exception as e:
        print(f"Error al eliminar archivo: {e}")

@app.route('/', methods=['GET', 'POST'])
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
            if file_path and os.path.exists(file_path):
                file = Path("temp_audio") / os.path.basename(file_path)
                filename = os.path.basename(file_path)  # Nombre del archivo descargado

                # Enviar el archivo al cliente
                return send_file(file, as_attachment=True, download_name=filename, mimetype='audio/mpeg')
            else:
                return "No se pudo procesar el archivo.", 500
        except Exception as e:
            return str(e), 500

    return render_template('download_page.html')

@app.route('/delete_file', methods=['POST'])
def delete_file():
    data = request.get_json()
    file_name = data.get('file_name')

    if file_name:
        file_path = os.path.join(TEMP_DIR, file_name)
        if os.path.exists(file_path):
            threading.Timer(1, remove_file, args=[file_path]).start()
            return jsonify({"message": "Archivo marcado para eliminación."}), 200
        else:
            return jsonify({"message": "Archivo no encontrado."}), 404
    return jsonify({"message": "Nombre de archivo no proporcionado."}), 400

@app.route('/upload', methods=['POST'])
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
    file.save(file_path)

    # Aquí podrías procesar el archivo (FFmpeg, etc.)
    print(f"Archivo recibido y guardado en: {file_path}")
    return jsonify({"message": "Archivo subido exitosamente", "filename": filename}), 200

if __name__ == '__main__':
    try:
        app.run(debug=True, host='0.0.0.0', port=5010)
    except Exception as e:
        print(f"Error al iniciar la aplicación: {e}")
