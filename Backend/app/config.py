import os

# Configuración general del backend
BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), os.path.pardir))

TEMP_AUDIO_DIR = os.path.join(BASE_DIR, r'data\temp_audio')
TEMP_FILES_DIR = os.path.join(BASE_DIR, r'data\temp_files')
UPLOAD_FOLDER = os.path.join(BASE_DIR, r'data\uploads')

def configure_app(app):
    # Crear carpetas necesarias si no existen
    for path in [TEMP_AUDIO_DIR, TEMP_FILES_DIR, UPLOAD_FOLDER]:
        os.makedirs(path, exist_ok=True)
