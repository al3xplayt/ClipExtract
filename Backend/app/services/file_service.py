import os, threading

TEMP_DIR = 'Backend/data/temp_audio'

def schedule_delete(file_name):
    file_path = os.path.join(TEMP_DIR, file_name)
    if os.path.exists(file_path):
        threading.Timer(1, os.remove, args=[file_path]).start()
        return True
    return False
