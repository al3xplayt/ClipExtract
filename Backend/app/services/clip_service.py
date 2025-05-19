# App/services/clip_service.py

import os
from app.utils.video_procesing import detect_scene_changes

UPLOAD_FOLDER = os.path.join("App", "data", "uploads")

def process_uploaded_video(file_storage):
    filename = file_storage.filename
    path = os.path.join(UPLOAD_FOLDER, filename)
    file_storage.save(path)

    clips = detect_scene_changes(path)
    return clips
