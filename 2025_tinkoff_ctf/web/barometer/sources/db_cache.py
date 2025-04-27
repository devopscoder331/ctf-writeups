import psycopg2
import os
import redis
import zlib
import json
import logging
from datetime import datetime, timedelta
from decimal import Decimal
import threading
from psycopg2.extensions import adapt

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger('db_cache')

class DateTimeEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, datetime):
            return obj.isoformat()
        if isinstance(obj, Decimal):
            return float(obj)
        return super().default(obj)

class DateTimeDecoder(json.JSONDecoder):
    def __init__(self, *args, **kwargs):
        json.JSONDecoder.__init__(self, *args, **kwargs)
        
    def decode(self, s, *args, **kwargs):
        result = super().decode(s, *args, **kwargs)
        return self._process_datetime(result)
        
    def _process_datetime(self, obj):
        if isinstance(obj, list):
            return [self._process_datetime(item) for item in obj]
        elif isinstance(obj, tuple):
            return tuple(self._process_datetime(item) for item in obj)
        elif isinstance(obj, str):
            try:
                return datetime.fromisoformat(obj)
            except (ValueError, TypeError):
                return obj
        return obj

class DBCache:
    def __init__(self):
        self.redis = redis.from_url('redis://redis:6379/0')
        self.cache_ttl = 15  # seconds

    def get_connection(self):
        return psycopg2.connect(
            dbname=os.environ.get('DB_NAME', 'weather_db'),
            user=os.environ.get('DB_USER', 'postgres'),
            password=os.environ.get('DB_PASSWORD', 'postgres'),
            host=os.environ.get('DB_HOST', 'db'),
            port=os.environ.get('DB_PORT', '5432')
        )

    def _get_cache_key(self, query, params):
        quoted_params = tuple(adapt(p).getquoted().decode('utf-8') for p in params)
        normalized_query = query % quoted_params
        crc = zlib.crc32(normalized_query.encode('utf-8'))
        cache_key = f"cache:{crc}"
        return cache_key

    def execute(self, query, params=None):
        if params is None:
            params = ()

        is_write = query.strip().upper().startswith(('INSERT', 'UPDATE', 'DELETE'))
        
        if is_write:
            with self.get_connection() as conn:
                with conn.cursor() as cur:
                    cur.execute(query, params)
                    if query.strip().upper().startswith('INSERT') and 'RETURNING' in query.upper():
                        result = cur.fetchall()
                        conn.commit()
                        return result
                    conn.commit()
                    return None

        cache_key = self._get_cache_key(query, params)
        
        cached_result = self.redis.get(cache_key)
        if cached_result:
            return json.loads(cached_result, cls=DateTimeDecoder)
        
        with self.get_connection() as conn:
            with conn.cursor() as cur:
                cur.execute(query, params)
                result = cur.fetchall()
                
                if result:
                    self.redis.setex(
                        cache_key,
                        self.cache_ttl,
                        json.dumps(result, cls=DateTimeEncoder)
                    )
                
                return result

    def execute_one(self, query, params=None):
        results = self.execute(query, params)
        return results[0] if results else None

db_cache = DBCache()
