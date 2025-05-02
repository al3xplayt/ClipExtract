import os
from pathlib import Path
import yt_dlp
import ffmpeg

TEMP_DIR = 'Backend/data/temp_audio'
os.makedirs(TEMP_DIR, exist_ok=True)

def download_video(url, formato):
    try:
        # Definir opciones para yt-dlp según el formato
        if formato == 'mp3':
            ydl_opts = {
                'format': 'bestaudio/best',
                'outtmpl': os.path.join(TEMP_DIR, '%(title)s.%(ext)s'),
                'noplaylist': True,
            }
        else:  # Descargar como MP4
            ydl_opts = {
                'format': 'bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]',
                'outtmpl': os.path.join(TEMP_DIR, '%(title)s.%(ext)s'),
                'noplaylist': True,
            }
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])

        # Buscar el archivo descargado
        filename = None
        for file in os.listdir(TEMP_DIR):
            if formato == 'mp3' and file.endswith(('.mp4', '.webm', '.m4a', '.flv')):
                filename = os.path.join(TEMP_DIR, file)
                break
            elif formato == 'mp4' and file.endswith('.mp4'):
                filename = os.path.join(TEMP_DIR, file)
                break

        if not filename:
            raise Exception("No se encontró un archivo descargado.")

        # Si es MP3, convertirlo
        if formato == 'mp3':
            mp3_file = os.path.join(TEMP_DIR, f"{os.path.splitext(os.path.basename(filename))[0]}.mp3")
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

    except Exception as e:
        raise e
