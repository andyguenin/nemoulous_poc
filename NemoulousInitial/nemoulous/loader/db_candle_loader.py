import pandas as pd
from numpy import NaN

import math
from nemoulous.securities.futures.monthcodes import MonthCodes
from nemoulous.loader.loader import Loader
import sqlite3
from typing import Tuple
from datetime import datetime, timedelta
from nemoulous.pricing.candle import CandleDataType, Candle

from nemoulous.utils.date_format import yyyymmdd_hhmm_format

import logging

logger = logging.getLogger(__name__)


class DbCandleLoader(Loader):

    def __init__(self, database='nemoulous.db'):
        super().__init__()
        self.db = database

    def load(self,
             dateBegin=None,
             dateEnd=None,
             contracts=[]
             ) -> pd.DataFrame:
        beginFilter = '? is not null'
        if dateBegin is not None:
            beginFilter = 'time >= ?'
        endFilter = '? is not null'
        if dateEnd is not None:
            endFilter = 'time <= ?'
        contract_filter = '1=1'
        if len(contracts) != 0:
            contract_filter = 'contract in (' + (', '.join(len(contracts) * ['?'])) + ')'

        values = (1 if dateBegin is None else dateBegin, 1 if dateEnd is None else dateEnd, *tuple(contracts))
        sql = '''
        select time, open, high, low, close, vol_bid, vol_ask, vol, tcount, contract
        from commodity_futures where %s and %s and %s''' % (beginFilter, endFilter, contract_filter)

        conn = sqlite3.connect(self.db)
        c = conn.cursor()
        c.execute(sql, values)

        df = pd.DataFrame(c.fetchall())

        column_names = ['time', 'open', 'high', 'low', 'close', 'vol_bid', 'vol_ask', 'vol', 'tcount', 'contract']
        if df.empty:
            df = pd.DataFrame(columns=column_names)
        else:
            df.columns = column_names

        return df

    def loadTimeSeries(self,
                       priceToUse: Tuple[CandleDataType],
                       dateBegin=None,
                       dateEnd=None,
                       contracts=[]):
        logger.info('begin loading')
        if not dateBegin is None:
            logger.info(f'start date: {dateBegin.strftime(yyyymmdd_hhmm_format)}')
        if not dateEnd is None:
            logger.info(f'start end:  {dateEnd.strftime(yyyymmdd_hhmm_format)}')
        df = self.load(dateBegin, dateEnd, contracts)

        pivoted = ()

        if not df.empty:
            df = df.groupby(['time', 'contract']).aggregate(
                {'open': 'mean', 'high': 'mean', 'low': 'mean', 'close': 'mean', 'vol_ask': 'sum', 'vol_bid': 'sum',
                 'vol': 'sum', 'tcount': 'sum'}).reset_index()
            df['time'] = df['time'].apply(lambda x: datetime.datetime.strptime(x, yyyymmdd_hhmm_format))
            logger.info('creating pivot')
            for candle_type in priceToUse:
                df['price'] = df.apply(
                    lambda x: Candle(x[2], x[3], x[4], x[5], x[6], x[7], x[8], x[9]).getPriceByType(candle_type), axis=1)
                pivot = df.pivot(index='time', columns='contract', values='price')
                pivoted = (*pivoted, pivot.reset_index())

        else:
            for candle_type in priceToUse:
                pivoted = (*pivoted, pd.DataFrame())

        return pivoted


    def storeTermStructure(self,
                           data: pd.DataFrame,
                           data_type: CandleDataType,
                           tag: str
                           ):

        conn = sqlite3.connect(self.db)
        c = conn.cursor()
        table_exist = '''SELECT name FROM sqlite_master WHERE type='table' AND name='commodity_future_term_structure';'''
        c.execute(table_exist)
        if c.fetchone() is None:
            create_table = '''
            create table commodity_future_term_structure (
                product varchar(32),
                type varchar(32),
                tag varchar(32),
                time timestamp,                
                m0 float,
                m1 float,
                m2 float,
                m3 float,
                m4 float,
                m5 float,
                m6 float,
                m7 float,
                m8 float,
                m9 float,
                m10 float,
                m11 float,
                primary key (product, type, tag, time)
            )
            '''
            c.execute(create_table)

        # for simplicity, I'm going to auto-roll the contracts at 6 calendar days before the end of month. I know that this
        # should happen at 3 bd before, but I am not loading a bd calendar in yet
        columns = [(datetime.datetime(int('20' + year), MonthCodes().month_by_code(code), 1) - timedelta(days=6), 'NG' + code + year) for year in ['17', '18', '19'] for code in MonthCodes().future_codes_order]

        min_date = data['time'].min()
        max_date = data['time'].max()

        inserts = []



        cols_to_add = {c[1]: NaN for c in columns if c[1] not in data.columns}
        data = data.assign(**cols_to_add)

        for i in range(int(divmod((max_date - min_date).total_seconds(), 60)[0]) + 1):
            time = min_date + timedelta(minutes=i)
            l = data[data['time'] == time].copy()
            cols = [elem[1] for elem in columns if elem[0] >= time and elem[1] in l.columns][:12]
            insert = l[['time', *cols]]
            new_column_names = ['time', *['m' + str(i) for i in range(len(cols))]]
            insert.columns = new_column_names
            # add_column_names = {'m' + str(i + len(cols)): NaN for i in range(12 - len(cols))}
            # insert = insert.assign(**add_column_names).reset_index().to_dict()
            if len(insert['time']) > 0:
                insert = [None if type(insert[key][0]) == float and math.isnan(insert[key][0]) else insert[key][0] for key in insert][1:]
                insert[0] = insert[0].strftime('%Y-%m-%d %H:%M')
                inserts.append(tuple(['NG', data_type.name, tag, *insert]))

        try:
            c.executemany('insert into commodity_future_term_structure (product, type, tag, time, m0, m1, m2, m3, m4, m5, m6, m7, m8, m9, m10, m11) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)', inserts)
            conn.commit()
        except:
            logger.info("cannot insert data")

        conn.close()


if __name__ == '__main__':
    import datetime

    datetime_start = datetime.datetime(2017, 1, 1)
    datetime_end = datetime.datetime(2019, 1, 3)
    for i in range((datetime_end - datetime_start).days + 1):
        (df,) = DbCandleLoader().loadTimeSeries((CandleDataType.CLOSE,), dateBegin=datetime_start + timedelta(days=i), dateEnd=datetime_start + timedelta(days=i + 1))
        if not vol.empty:
            DbCandleLoader().storeTermStructure(df, CandleDataType.CLOSEu, "MIN")


