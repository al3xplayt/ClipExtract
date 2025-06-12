# 🎬 ClipExtract
**ClipExtract** es una aplicación que permite **descargar vídeos y audios de YouTube**, así como **extraer clips destacados automáticamente** mediante técnicas de inteligencia artificial. Está compuesta por una app Android y un backend en Python (Flask) con PostgreSQL y FFmpeg.

---

## 🚀 Instalación

### Requisitos

Asegúrate de tener instalados los siguientes elementos:

* [Python 3.13.2](https://www.python.org/downloads/)
* [PostgreSQL](https://www.postgresql.org/download/)
* [FFmpeg](https://ffmpeg.org/download.html)
* Git (opcional pero recomendado)

### Clonar el repositorio

```bash
git clone https://github.com/tu_usuario/trabajofinal_AG.git
cd trabajofinal_AG
```

### Crear y activar entorno virtual

```bash
python -m venv venv
source venv/bin/activate  # En Windows: .\venv\Scripts\activate
```

### Instalar dependencias del backend

```bash
pip install -r requirements.txt
```

### Configurar variables de entorno

Crea un archivo `.env` en la raíz del proyecto con el siguiente contenido (modifica según tu configuración):

```env
FLASK_APP=main.py
FLASK_ENV=development
DATABASE_URL=postgresql://usuario:contraseña@localhost:5432/clipextract
```

### Ejecutar el backend

```bash
python ./Backend/main.py
```

---

## 📱 Instalación de la App Android

1. Abre el proyecto Android ubicado en `./AndroidApp` desde Android Studio.
2. Asegúrate de que el servidor Flask esté en ejecución.
3. Configura la IP del backend en los archivos correspondientes (`RetrofitClient.java` o `ApiService.java`).
4. Conecta un dispositivo o emulador y ejecuta la app.

---

## 🧠 Funcionalidades

* ✅ Descargar vídeos de YouTube (formato MP4)
* ✅ Descargar audios de YouTube (formato MP3)
* 🧪 Extraer automáticamente clips destacados (mediante IA)
* 📜 Historial de descargas local (Room)
* 📤 Subida de vídeos locales para análisis

