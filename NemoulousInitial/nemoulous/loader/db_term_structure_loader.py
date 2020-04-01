from datetime import datetime
from time import sleep

from nemoulous.loader.loader import Loader
import pandas as pd
import json
import sqlite3
from kafka import KafkaProducer

from nemoulous.utils.date_format import yyyymmdd_hhmm_format


class DbTermStructureLoader(Loader):
    def __init__(self, file: str):
        self.db_file = file

    def load(self) -> pd.DataFrame:
        conn = sqlite3.connect(self.db_file)
        c = conn.cursor()

        query = '''
        select product, type, tag, time, m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11
        from commodity_future_term_structure 
        '''

        c.execute(query)
        df = pd.DataFrame(c.fetchall())
        df.columns = ['product', 'type', 'tag', 'time', 'm0', 'm1', 'm2', 'm3', 'm4', 'm5', 'm6', 'm7', 'm8', 'm9', 'm10', 'm11']
        df['time'] = df['time'].apply(lambda x: datetime.strptime(x, yyyymmdd_hhmm_format))
        return df



if __name__ == '__main__':

    producer = KafkaProducer(bootstrap_servers='localhost:9092', value_serializer=lambda v: json.dumps(v).encode('utf-8') )



    producer.send('fizzbuzz', {'foo': 'bar'})


    df = DbTermStructureLoader('nemoulous.db').load()
    df = df[(df['m0'].notna()) & (df['m1'].notna()) & (df['time'] >= datetime(2018, 1, 1)) & (df['time'] <= datetime(2018, 1, 5))].reset_index()
    for index, row in df.iterrows():
        if index < 20:
            obj = {
                'name': 'futurespread',
                'futureType': 'NG',
                'closeContract': str(row['m0']),
                'farContract': str(row['m1']),
                'spread': row['m1'] - row['m0']
            }

            print(obj)
            future = producer.send('nemoulous-signal', obj)
        else:
            break


    producer.flush()
    sleep(20)
