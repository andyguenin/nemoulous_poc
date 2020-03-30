from enum import Enum

class CandleDataType(Enum):
    OPEN = 1
    HIGH = 2
    LOW = 3
    CLOSE = 4
    VOL_BID = 5
    VOL_ASK = 6
    VOL = 7
    TCOUNT = 8

class Candle:
    def __init__(self,
                 open: float,
                 high: float,
                 low: float,
                 close: float,
                 vol_bid: int,
                 vol_ask: int,
                 vol: int,
                 tcount: int):
        self.open = open
        self.high = high
        self.low = low
        self.close = close
        self.vol_bid = vol_bid
        self.vol_ask = vol_ask
        self.vol = vol
        self.tcount = tcount

    def getPriceByType(self, type: CandleDataType) -> float:
        if type == CandleDataType.OPEN:
            return self.open
        if type == CandleDataType.HIGH:
            return self.high
        if type == CandleDataType.LOW:
            return self.low
        if type == CandleDataType.CLOSE:
            return self.close
        if type == CandleDataType.VOL_BID:
            return self.vol_bid
        if type == CandleDataType.VOL_ASK:
            return self.vol_ask
        if type == CandleDataType.VOL:
            return self.vol
        if type == CandleDataType.TCOUNT:
            return self.tcount
        raise Exception("Unexpected candle data type: " + str(type))