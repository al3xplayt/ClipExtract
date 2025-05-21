import cv2
import os

def detect_scene_changes(video_path, threshold=3.0, min_scene_gap=2.0):
    cap = cv2.VideoCapture(video_path)
    fps = cap.get(cv2.CAP_PROP_FPS)
    timestamps = []

    ret, prev_frame = cap.read()
    if not ret:
        cap.release()
        print("No se pudo leer el primer frame.")
        return []

    prev_gray = cv2.cvtColor(prev_frame, cv2.COLOR_BGR2GRAY)
    frame_number = 1
    last_scene_time = 0.0

    timestamps.append(0.0)

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        curr_gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        diff = cv2.absdiff(curr_gray, prev_gray)
        score = diff.mean()

        current_time = frame_number / fps

        if score > threshold and (current_time - last_scene_time >= min_scene_gap):
            timestamps.append(current_time)
            last_scene_time = current_time
            print(f"*** Cambio de escena detectado en segundo {current_time:.2f} (score={score:.2f})")

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

    print(f"Total de clips detectados: {len(clips)}")
    return clips
