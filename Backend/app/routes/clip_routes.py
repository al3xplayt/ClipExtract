from flask import Blueprint, request, jsonify, send_file
from app.utils.video_processing import detect_scene_changes
from app.config import UPLOAD_FOLDER, TEMP_FILES_DIR  
import os, subprocess

clip_bp = Blueprint("clip_bp", __name__)


@clip_bp.route('/extract_clips', methods=['GET'])
def extract_clips():
    filename = request.args.get("filename")
    if not filename:
        return jsonify({'error': 'Falta el parámetro filename'}), 400

    video_path = os.path.join(UPLOAD_FOLDER, filename)
    if not os.path.exists(video_path):
        return jsonify({'error': 'Archivo no encontrado'}), 404

    clips = detect_scene_changes(video_path)
    for i, clip in enumerate(clips):
        start = clip['start']
        end = clip['end']
        duration = end - start
        clips[i]['duration'] = duration
    print(clips)
    print("Algo se ha hecho")
    if not clips:
        print("No se encontraron clips")
        return jsonify({'message': 'No se encontraron clips'}), 200
    print(len(clips))
    return jsonify({'clips': clips}), 200

@clip_bp.route('/download_clip', methods=['POST'])
def download_clip():
    data = request.get_json()
    filename = data.get('filename')
    start = data.get('start')
    end = data.get('end')

    if not filename or start is None or end is None:
        return jsonify({'error': 'Faltan parámetros necesarios'}), 400

    video_path = os.path.join(UPLOAD_FOLDER, filename)
    if not os.path.exists(video_path):
        return jsonify({'error': 'Archivo no encontrado'}), 404

    # Archivo temporal para el clip recortado
    clip_filename = f"clip_{os.path.splitext(filename)[0]}_{int(start)}_{int(end)}.mp4"
    clip_path = os.path.join(TEMP_FILES_DIR, clip_filename)

    # Comando ffmpeg para recortar el video
    # -ss start, -to end indica el segmento a extraer
    command = [
        "ffmpeg",
        "-i", video_path,
        "-ss", str(start),
        "-to", str(end),
        "-c", "copy",  # copia sin recodificar para mayor rapidez
        "-y",  # sobreescribir archivo si existe
        clip_path
    ]

    try:
        subprocess.run(command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except subprocess.CalledProcessError as e:
        return jsonify({'error': 'Error al procesar el video', 'details': str(e)}), 500

    if not os.path.exists(clip_path):
        return jsonify({'error': 'No se pudo generar el clip'}), 500

    # Devolver el archivo recortado para descarga
    return send_file(clip_path, as_attachment=True, download_name=clip_filename, mimetype='video/mp4')
