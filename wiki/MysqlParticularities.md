
```
// we force the value to int cause mysql AUTO_GENERATED always returns Long instead of Int
val id = m.int(JobPositionEntity.id)
```

  * Serial columns are returned as java.math.BigInteger. Again the implicit convertor or `m.int` will convert those to int.
