from nemoulous.loader.loader import Loader
import pandas as pd
import sqlite3

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
        return df



if __name__ == '__main__':
    print(DbTermStructureLoader('../../nemoulous.db').load())