import os, threading
from pathlib import Path
import yt_dlp
import ffmpeg
import re
from app.config import TEMP_FILES_DIR


def sanitize_filename(filename):
    filename = filename.replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u')
    filename = filename.replace('Á', 'A').replace('É', 'E').replace('Í', 'I').replace('Ó', 'O').replace('Ú', 'U')

    filename = re.sub(r'[^\x00-\x7F]+', '', filename) 

    filename = filename.replace(' ', '_')

    return filename

def download_video(url, formato):
    try:
        # Definir opciones para yt-dlp según el formato
        print(formato)
        if formato == 'mp3':
            ydl_opts = {
                'format': 'bestaudio/best',
                'outtmpl': os.path.join(TEMP_FILES_DIR, '%(title)s.%(ext)s'),
                'noplaylist': True,
            }
        else:  # Descargar como MP4
            ydl_opts = {
                'format': 'bestvideo+bestaudio/best',
                'outtmpl': os.path.join(TEMP_FILES_DIR, '%(title)s.%(ext)s'),
                'noplaylist': True,
            }
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])
        # Buscar el archivo descargado
        filename = None
        for file in os.listdir(TEMP_FILES_DIR):
            if formato == 'mp3' and file.endswith(('.mp4', '.webm', '.m4a', '.flv')):
                filename = os.path.join(TEMP_FILES_DIR, file)
                break
            elif formato == 'mp4' and file.endswith('.mp4'):
                filename = os.path.join(TEMP_FILES_DIR, file)
                break
        if not filename:
            raise Exception("No se encontró un archivo descargado.")

        # Si es MP3, convertirlo
        if formato == 'mp3':
            mp3_file = os.path.join(TEMP_FILES_DIR, f"{os.path.splitext(os.path.basename(filename))[0]}.mp3")
            try:
                ffmpeg.input(filename).output(mp3_file, audio_bitrate='192k').run(overwrite_output=True)
                os.remove(filename)
            except Exception as e:
                print(f"Error al convertir a MP3: {e}")
                return f"Error al convertir a MP3: {e}"
            return mp3_file
        else:
            return filename  # Retornar el archivo MP4 directamente

    except Exception as e:
        raise e


def schedule_delete(file_name):
    file_path = os.path.join(TEMP_FILES_DIR, file_name)
    print(f"Programando eliminación de: {file_path}")
    if os.path.exists(file_path):
        threading.Timer(1, os.remove, args=[file_path]).start()
        return True
    return False
