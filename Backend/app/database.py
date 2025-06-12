# app/database.py
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv
import os
load_dotenv()
db_url = os.getenv("DATABASE_URL", "postgresql://postgres:admin@192.168.1.15:30081/clipextract_db")

# Db url kubernetes for svc nodeport cluster:
# db_url = "postgresql://postgres:admin@192.168.1.15:30081/clipextract_db"

# Db url for local cluster:
# "postgresql://postgres:admin@svc-postgresql.clipextract-backend.svc.cluster.local:5432/clipextract_db"
engine = create_engine(db_url)

# Crear sesión local
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Clase base para modelos
Base = declarative_base()
