import argparse
import logging
from os import listdir
from os.path import isfile, join
import sqlite3

import pandas as pd

from nemoulous.loader.loader import Loader

'''
This file will load candle data into the database
'''

logger = logging.getLogger(__name__)

class FileCandleLoader(Loader):
    def __init__(self, file: str, label: str):
        super().__init__()
        self.file = file
        self.label = label

    def load(self) -> pd.DataFrame:
        logger.debug(f'Reading {self.label} data from file')
        raw = pd.read_csv(self.file)
        raw['label'] = self.label
        return raw


def loadAllFiles(dataDir: str):
    table_exist = '''SELECT name FROM sqlite_master WHERE type='table' AND name='commodity_futures';'''


    conn = sqlite3.connect('nemoulous.db')

    c = conn.cursor()
    c.execute(table_exist)
    if c.fetchone() is None:
        c.execute('''create table commodity_futures (
        time timestamp not null,
        open float not null,
        high float not null,
        low float not null,
        close float not null,
        vol_bid int not null, 
        vol_ask int not null,
        vol int not null,
        tcount int not null,
        contract varchar(32) not null
         )
        ''')

    files = [f for f in listdir(dataDir) if isfile(join(dataDir, f)) and f.endswith('.csv')]
    for file in files:
        contract = file[:-4]
        logger.debug(f'Delete {contract} from database.')
        c.execute('delete from commodity_futures where contract = ?', (contract,))
        fcl = FileCandleLoader(dataDir + '/' + contract + '.csv', contract).load()
        inserts = [(i[0], i[1], i[2], i[3], i[4], i[5], i[6], i[7], i[8], i[9]) for i in fcl.values.tolist()]
        logger.debug(f'Inserting {contract} into database.')
        c.executemany('insert into commodity_futures values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)', inserts)
        logger.debug(f'Finished inserting {contract} into database')
    conn.commit()
    c.close()

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Load candle data from a file and store it into the database')
    parser.add_argument('--data_dir', '-d', nargs=1, required=True)
    args = parser.parse_args()
    loadAllFiles(args.data_dir[0])
