import threading
import os
from app.config import TEMP_FILES_DIR, UPLOAD_FOLDER

def remove_file(file_path):
    """Remove a file if it exists."""
    try:
        if os.path.exists(file_path):
            os.remove(file_path)
            print(f"Archivo eliminado: {file_path}")
        else:
            print(f"Archivo no encontrado: {file_path}")
    except Exception as e:
        print(f"Error al eliminar el archivo {file_path}: {e}")

def schedule_delete(file_name, delay=360):
    """Schedule a file for deletion after a delay."""
    file_path = os.path.join(TEMP_FILES_DIR, file_name)
    if not os.path.exists(file_path):
        file_path = os.path.join(UPLOAD_FOLDER, file_name)

    if not os.path.exists(file_path):
        print(f"Archivo no encontrado para programar eliminación: {file_name}")
        return

    timer = threading.Timer(delay, remove_file, args=(file_path,))
    timer.start()
    print(f"Archivo {file_name} programado para eliminación en {delay} segundos.")

# Mirar las rutas y si hay un