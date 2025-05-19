# App/utils/video_processing.py

import cv2
import os

def detect_scene_changes(video_path, threshold=30.0):
    cap = cv2.VideoCapture(video_path)
    fps = cap.get(cv2.CAP_PROP_FPS)
    timestamps = []

    ret, prev_frame = cap.read()
    if not ret:
        cap.release()
        return []

    prev_gray = cv2.cvtColor(prev_frame, cv2.COLOR_BGR2GRAY)
    frame_number = 1

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        curr_gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        diff = cv2.absdiff(curr_gray, prev_gray)
        score = diff.mean()

        if score > threshold:
            time_sec = frame_number / fps
            timestamps.append(time_sec)

        prev_gray = curr_gray
        frame_number += 1

    cap.release()

    clips = []
    for i in range(len(timestamps) - 1):
        start = timestamps[i]
        end = timestamps[i + 1]
        duration = end - start
        if 5 <= duration <= 30:
            clips.append({"start": round(start, 2), "end": round(end, 2)})

    return clips
