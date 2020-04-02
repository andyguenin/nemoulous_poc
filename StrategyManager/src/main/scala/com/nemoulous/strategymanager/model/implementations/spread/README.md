# Spread models


## Diverging Spread

_file: DivergingSpread.scala_

The Diverging Spread model takes in one parameter: the number of consecutive periods that the spread must be increasing. 
Once the model observes this many periods of increasing spread, it will put a trade on, counting on the spread to
continue increasing. The trade will then last for 2 times the duration of the increasing spread. 