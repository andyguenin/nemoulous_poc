# Nemoulous Data Loader

## Initial Load

run `nemoulous/loader/file_candle_loader.py --data_dir data`

## Convert to Term Structure

This run takes a while (over an hour on my machine), but only needs to be done once. There are probably more efficient ways to handle the
transformation of this data, but I didn't spend the time researching it.

run `nemoulous/loader/db_candle_loader.py`

## Dump to a Kafka Producer

run `nemoulous/loader/db_term_structure_loader.py`