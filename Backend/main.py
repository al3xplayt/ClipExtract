from flask import Flask
from flask_cors import CORS
from app.routes import register_routes
from app.config import configure_app

app = Flask(__name__)
CORS(app)
configure_app(app)  # aplica configuraciones (e.g., upload_folder)
register_routes(app)  # registra los blueprints

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=50010)
