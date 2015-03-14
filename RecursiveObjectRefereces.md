If class A refers to class B and B to A, then they have a recursive reference. Assuming A and B are immutable, there is no way to
create instances of A & B without having control of the source code of A and B. MapperDao, when instantiating B has to provide
a reference to A. But A instance is not ready yet. MapperDao uses a mock object of A:

```

val mockA=...create A with the available data of A at this time (B is not available)

...

val b=new B(mockA,...)

...

val a=new A(b,...)


```

B is not available during the creation of mockA. Hence after a call to `mapperDao.select`, some entities down the graph might not have all fields populated if the same entity exists closer to the root of the graph.
