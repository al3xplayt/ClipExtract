from flask import Blueprint, request, jsonify, send_file, stream_with_context, Response
from src.utils.video_processing import detect_scene_changes
from src.config import UPLOAD_FOLDER, TEMP_CLIPS_DIR as TEM  
import os, subprocess
from src.database import SessionLocal
from src.models.models import Video
clip_bp = Blueprint("clip_bp", __name__)


@clip_bp.route('/extract_clips', methods=['GET'])
def extract_clips():
    db = SessionLocal()
    filename = request.args.get("filename")
    if not filename:
        return jsonify({'message': 'Falta el nombre del archio'}), 400
    video_path = os.path.join(UPLOAD_FOLDER, filename)
    print(f"Ruta del video: {video_path}")
    if not os.path.exists(video_path):
        print(f"Archivo no encontrado: {video_path}")
        return jsonify({'message': 'Archivo no encontrado'}), 404
    clips = detect_scene_changes(video_path)
    video = db.query(Video).filter_by(filename=filename).first()
    db.close()
    print("Video id: ",video.id)
    for i, clip in enumerate(clips):
        start = clip['start']
        end = clip['end']
        duration = end - start
        clips[i]['duration'] = duration
    if not clips:
        return jsonify({'message': 'No se encontraron clips'}), 200
    return jsonify({'filename': filename, 'video_id': video.id,'clips': clips}), 200

@clip_bp.route('/download_clip', methods=['POST'])
def download_clip():
    data = request.get_json()
    filename = data.get('filename')
    start = data.get('start')
    end = data.get('end')

    if not filename or start is None or end is None:
        return jsonify({'message': 'Faltan parámetros necesarios'}), 400

    video_path = os.path.join(UPLOAD_FOLDER, filename)
    if not os.path.exists(video_path):
        return jsonify({'message': 'Archivo no encontrado'}), 404

    # Archivo temporal para el clip recortado
    clip_filename = f"clip_{os.path.splitext(filename)[0]}_{int(start)}_{int(end)}.mp4"
    clip_path = os.path.join(TEM, clip_filename)

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
        return jsonify({'message': 'Error al procesar el video', 'details': str(e)}), 500

    if not os.path.exists(clip_path):
        return jsonify({'message': 'No se pudo generar el clip'}), 500

    # Devolver el archivo recortado para descarga
    return send_file(clip_path, as_attachment=True, download_name=clip_filename, mimetype='video/mp4')

@clip_bp.route('/preview_clip/<filename>' , methods=['GET'])
def serve_video(filename):
    path = os.path.join(UPLOAD_FOLDER, filename)
    if os.path.exists(path):
        return send_file(path, mimetype="video/mp4")
    return jsonify({"message": "Archivo no encontrado"}), 404

@clip_bp.route('/preview_clip_stream', methods=['GET'])
def preview_clip_stream():
    filename = request.args.get("filename")
    start = request.args.get("start")
    end = request.args.get("end")

    if not filename or not start or not end:
        return jsonify({"message": "Faltan parámetros: filename, start, end"}), 400

    video_path = os.path.join(UPLOAD_FOLDER, filename)
    if not os.path.exists(video_path):
        return jsonify({"message": "Archivo no encontrado"}), 404

    try:
        start = float(start)
        end = float(end)
    except ValueError:
        return jsonify({"message": "Parámetros start y end deben ser números"}), 400

    duration = end - start
    if duration <= 0:
        return jsonify({"message": "Duración inválida"}), 400

    # ffmpeg streaming
    command = [
        "ffmpeg",
        "-ss", str(start),
        "-i", video_path,
        "-t", str(duration),
        "-c:v", "copy",  # Sin recodificación
        "-movflags", "frag_keyframe+empty_moov",
        "-f", "mp4",
        "pipe:1"
    ]


    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    def generate():
        while True:
            chunk = process.stdout.read(1024)
            if not chunk:
                break
            yield chunk

    return Response(
        stream_with_context(generate()),
        mimetype="video/mp4",
        headers={"Accept-Ranges": "bytes"}
    )

