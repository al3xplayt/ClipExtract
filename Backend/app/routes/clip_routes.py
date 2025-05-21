from flask import Blueprint, request, jsonify
from app.utils.video_processing import detect_scene_changes
from app.config import UPLOAD_FOLDER
import os

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
