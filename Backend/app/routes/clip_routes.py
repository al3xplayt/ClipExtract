# App/routes/clip_routes.py

from flask import Blueprint, request, jsonify
from app.services.clip_service import process_uploaded_video

clip_bp = Blueprint("clip_bp", __name__)

@clip_bp.route('/extract_clips', methods=['POST'])
def extract_clips():
    if 'video' not in request.files:
        return jsonify({'error': 'No video file provided'}), 400

    file = request.files['video']
    clips = process_uploaded_video(file)
    return jsonify({'clips': clips})
