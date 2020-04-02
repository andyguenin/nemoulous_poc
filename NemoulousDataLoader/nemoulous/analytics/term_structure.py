
from typing import Dict
import datetime

class TermStructure:

    def __init__(self, prices: Dict[datetime, float]):
        self.prices = prices

    def getSpreads(self) -> Dict[(datetime, datetime), float]:
        pass
