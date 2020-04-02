class MonthCodes:
    future_codes_order = ['F', 'G', 'H', 'J', 'K', 'M', 'N', 'Q', 'U', 'V', 'X', 'Z']

    def __init__(self):
        pass

    def code_by_month(self, month: int) -> str:
        '''Returns the code of the future by month of year, with January = 1, Febrary = 2, etc'''
        if month > 12 or month < 1:
            raise Exception('month is out of bounds for future code lookup')
        return self.future_codes_order[month - 1]

    def month_by_code(self, code: str) -> int:
        '''Returns the month of year by future code, with January = 1, Febrary = 2, etc'''
        if code not in self.future_codes_order:
            raise Exception('code is not valid')
        return self.future_codes_order.index(code) + 1
