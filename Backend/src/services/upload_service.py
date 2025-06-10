import subprocess

def normalize_video(input_path, output_path):
    """
    Normaliza un video usando ffmpeg:
    - Reconvierte a H.264 (video)
    - AAC (audio)
    - Corrige rotación (metadata)
    - Escala a 720p como ejemplo
    """
    command = [
        'ffmpeg',
        '-y',  # Sobrescribir sin preguntar
        '-i', input_path,
        '-vf', 'scale=1280:720,transpose=0',  # Ajusta según necesidad (transpose=0 es no rotar)
        '-c:v', 'libx264',
        '-preset', 'fast',
        '-crf', '23',
        '-c:a', 'aac',
        '-b:a', '128k',
        '-movflags', '+faststart',
        output_path
    ]
    try:
        subprocess.run(command, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        return True
    except subprocess.CalledProcessError as e:
        print("Error en normalización:", e.stderr.decode())
        return False
