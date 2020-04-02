# Nemoulous Proof of Concept

This project contains three individual projects, one of which is not currently implemented. They are as follows:

1. NemoulousDataLoader: This python application converts the raw candle data into a schema and stores that schema in a sqlite db. It also will read from that database and put the raw data into a Kafka consumer.
2. NemoulousWeb: Not yet implemented. This would be a visualization for the strategy data from the strategy manager.
3. StrategyManager: This is a proof of concept method of managing strategies that I outlined for you earlier. There is one strategy present that currently just emits orders based on the momentum of spreads.


