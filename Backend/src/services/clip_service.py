# App/services/clip_service.py

import os
from src.utils.video_processing import detect_scene_changes
from src.config import UPLOAD_FOLDER

def process_uploaded_video(file_storage):
    filename = file_storage.filename
    path = os.path.join(UPLOAD_FOLDER, filename)
    file_storage.save(path)

    clips = detect_scene_changes(path)
    return clips
