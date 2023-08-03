select 'search'                                                                   source,
       DATE_FORMAT(TUMBLE_START(rt, INTERVAL '10' SECOND), 'yyyy-MM-dd HH:mm:ss') stt,
       DATE_FORMAT(TUMBLE_END(rt, INTERVAL '10' SECOND), 'yyyy-MM-dd HH:mm:ss')   edt,
       word                                                                       keyword,
       count(*)                                                                   keyword_count,
       UNIX_TIMESTAMP * 1000                                                      ts
from split_table
group by word, TUMBLE(rt, INTERVAL '10' SECOND)

