from .download_routes import download_bp
from .upload_routes import upload_bp
from .database_route import database_bp
from .clip_routes import clip_bp
def register_routes(app):
    app.register_blueprint(download_bp)
    app.register_blueprint(upload_bp)
    app.register_blueprint(database_bp)
    app.register_blueprint(clip_bp)
