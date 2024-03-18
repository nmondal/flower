# FRUIT : An Object Mapper Library 

```toc
```

[toc]

## Motivation 

### Introduction of the Problem 

One of the key problem of `Back-End Engineering` is transforming one object to another form. This is pretty prominent in the case of the pattern `BFF - backend for front-ent`  - but other areas are not far behind.

What does this mean? It means transformation of `schema`.
Input object will be of type $T_I$  while the output object will be of type $T_O$.  This is known to be the `Object Mapping` problem.

A most trivial object mapping is just selecting some specified fields of an object. 
A most generic Object mapper involves aggregating multiple objects and then selecting some computed fields to create an object.

Thus most generic object mapper is defined as such.
Let $O_i$ be the $N$ different input objects. 
Let $F_j$ be the $M$ different  functions which produce the $M$ fields of the output object - most generally taking the form:

$F_j := F_j(O_1,O_2,...,O_N)$

Thus the output object $O_O$ is given by :

$O_O := < F_1, F_2,..., F_M > ( O_1,O_2,...,O_N )$

This  is generic enough to include aggregation. However, we can extend even a single object to aggregation by imagining an `AND` Object - which is a tuple $<O_I>$ . Using this virtual tuple object any aggregation is possible. 

### Industry Standards 

#### SQL 
Surprisingly, the first generic model for such a system is `Relational Algebra`. The standard $\Pi$  - `project` and $\sigma$  `select` operations naturally showcase object mapping.
`SQL` is not Turing Complete.

#### XSLT
Another useful tool is `xslt`  - a standard that use `xpath` to transform `xml` documents. `xslt` is Turing Complete.

#### Code 
By far, the most rampant standard is `just code it`.  This is what industry loves, not because it is effective, because simply speaking it let people add more and more developers and thus - jobs are never out of fashion.

The trouble, of course is lack of declaratives structure in `code`. While `SQL` and `xslt` both are `code` - they are declarative, and thus defines the `structure`.

Given the lack of proper standards for `Object Transform` - or rather as it is nowadays being called `JSON Transform` - there are various alternatives available. References section deals with it.


### Tenets - Design Principles 

#### Simple 

After looking into the alternatives we figured out they are not `simple`.  The forming structure is not inherent in many of the design.

#### Declarative 

The system must be declarative, else one can simply `code it`.  System must be configuration driven. 
See more:
https://stackoverflow.com/questions/129628/what-is-declarative-programming

#### Provable 

Configurations must be provable by `inspection`.  Once the `engine` is well tested, it must produce precisely what is being asked for - much like `SQL`.

#### Re-Usable 

Transforms must be able to reference each other - such as to be able to re-use existing transforms.

#### Fault Tolerant 

In case of malformed objects - things should not fail unless they are intended. This is in progress. 

#### Debuggable 

In case of failures, error checking should be a breeze. This is in progress. 

### Naming 

`FRUIT` stands for **F**unctional **R**untime **U**niform **I**nterface for **T**ransformation.
It describes the basic ideas :
1. It is functional in nature ( as well as declarative )
2. It happens in runtime, there are no static options 
3. Uniform - basic structures are unified 
4. Interface based - one can create to implement  ones own implementation 
5. Transformation - obvious 

## Manual 

### Basic Usage 

Suppose there is this `JSON` here:

```json
// input.json 
{
  "rating": {
    "primary": {
    "value": 3
    },
    "quality": {
    "value": 3
    }
  }
}
```

And suppose we want to create an object extracting the primary and the quality by creating a structure as this:

```json
// this is what we seek...
{
  "primary": 3,
  "quality": 3
}
```

Mapping is done by defining a mapper - somehere as a an `yaml` document with something like this:

```yaml
# this file is mapping.yaml
hello_x:
  rating: "#rating/primary/value"
  quality: "#rating/quality/value"  

hello_z:
  rating: "$.rating.primary.value"
  quality: "$.rating.quality.value"  

```

And then you run it from the code as something like this:

```java
public void someFunc(){
  // and in the code...
  Object o = load( "input.json");
  Transformation<?> tr = MapBasedTransform.MANAGER.transformation( "mapping.yaml", "hello_x");
  Object r = tr.apply(o); // apply the transform
}
```

#### Analysis

So, whats happening in the mapper file? Mappings or transforms are defined as `map` in `yaml` - with `keys` not having  `_`  in the begining defines the structure of the output object.

As we can see  in our `hello` transform, there are two `path`s, one `maps` the `rating/primary/value` and another `rating/quality/value` into respective fields.

This is called path mapping. `JXPath` is used to map paths. The directive `#` before the path defines the mapping as `xpath` while other mappings are possible ( `Xelement` , `ZoomBA` ). 

The `hello_z` transform shows how in the `ZoomBA` scripting one can achieve the same.

This in short, how the `Transformation<?>` API works. 

#### Understanding Path Mapping 

One thing is to understand that how paths are mapped. There are 3 ways:

##### Xpath Value 

Any path starting with `#` is using xpath value. The result is always a JVM proper object - or error, if there is nothing exists in the path.

##### XElement Value 

Any path starting with `@` is using element value. The result is always a `NodePointer` object - which can very well be empty. Read more about it here:

https://commons.apache.org/proper/commons-jxpath/apidocs/org/apache/commons/jxpath/ri/model/NodePointer.html


##### ZoomBA Scripting Object 

The current object context being mapped - is defined by the variable `$`.  As we progress down the sub objects, context gets replaced by the sub-context. More of these will be clear with examples. 

This is equivalent of `$` is  `this` pointer.


### Transform Types 


#### Basic 

A basic transformation was already used in `hello` transform - let's go over it:

```yaml
hello:
  x : "#a.b"
  y : "#c.d"
  z : 42
  t : "'constant string'"
```

These are some `basic` field mapping which tentatively stating that the object must have at least this structure:

```json
{
  "a" : { "b" : X },
  "c" : { "d" : Y },
}
```

we are not sure of `X` and `Y` which are unknown. As one can see, one can put constant values too. In case of string, one must  quote it `"'const'"` .


Any object with identity transform transforms the object into the same object. It does not modify, it pass the input object as is as output.

#### Function Mapping 

Applying result of an expression as the object itself. We are not talking about fields, but the object itself:

```yaml
function_map:
  "_" : "f($)"
```

Function map literally takes the object as input, applies the function `f()` onto that object and uses the return value as object mapper.

These examples shows different functions:

```yaml
const_string:
  "*" : '"me"'
other_field:
  "*" : str($) # string from the context 
```


##### Identity 

A special case of function mapping is the identity function. Sometimes it is necessary to map the input object as is to the output. This is done using the identity mapper as follows:

```yaml
identity:
  "*" : "*"
```

This is identically same as doing:

```yaml
identity:
  "*" : "$"
```

Just that it saves the `expression evaluation` time. 

#### List 

This is the transform we use to create a list out of something that is iterable. As an example - say from this:

```json
{
  "clientsActive": true,
  "clients": {
    "Acme": {
      "clientId": "Acme",
      "index": 1
    },
    "Axe": {
      "clientId": "AXE",
      "index": 0
    }
  },
  "data": {
    "bookId": null,
    "bookName": "Enchiridion"
  }
}
```

We want to have this:

```json
{
   "clientIds" : [ "Acme", "AXE" ]
}
```

This is doable by simply creating a `List` transformer as follows:

```yaml
jolt_9x:
  clientIds :
    _each: "#//clientId"
    "*" : "*"
```

Note the usage of the `identity` transform. It is saying use the extracted object ( string ) as is, w/o any modification.  W/O `identity`, it would be impossible to do so.

The same can be achieved using ( with waste of compute ) as :

```yaml
jolt_9x:
  clientIds :
    _each: "#//clientId"
    "*" : "$"
```

in effect, this is the same as above. But it would now evaluate the expression.

##### each

The directive `_each` produces a `Stream` of objects from the data source that is given. In the above example, it is using `xpath` to find out values of all `clientId` nodes.

##### when

Suppose we want to find all `cliendId` having `X` in it. How do we do it? This is precisely why the `when` clause exists in `List` transform:

```yaml
jolt_9x:
  clientIds :
    _each: "#//clientId"
    _when: "'X' @ $"
    "*" : "*"
```

The `when` clause is defining `'X' @ $` that is, if `$` string `contains` `X`. This is `ZoomBA` syntax.  Only `AXE` will now be selected.

NOTE: In the example there is a container `clientIds` which contains the list. In case one wants a raw list, one can simple start at the top removing any fields:

```yaml
jolt_9x:
  _each: "#//clientId"
  _when: "'X' @ $"
  "*" : "*"
```

This will produce a raw list.

#### Object 

By definition, every mapping starts with `object` mapping. The `hello` mapping is an example.
The idea of this is to define fields and define transformers for each field. 

```yaml
jolt_1:
  Rating : "#rating/primary/value"
  SecondaryRatings :
    quality:
      Id : "'quality'"
      Value: "#rating/quality/value"
      Range: 5
  Range: 5
```

This defines an object mapping. One can see the `const` maps as well as the path mapping. 

#### Map Object 

Sometimes it is needed to iterate over a collection and create an object out of it when conditions match. This is done by the map object transformer.

Suppose we are given this :

```json
{
  "Rating": 1,
  "SecondaryRatings": {
    "Design": 4,
    "Price": 2,
    "RatingDimension3": 1
  }
}
```

And we are to `flatten` the nested structure into something like this:

```json
{
  "rating-primary" : 1,
  "rating-Design" : 4,
  "rating-Price" : 2,
  "rating-RatingDimension3" : 1
}
```

This can be done by Map Object mapper as follows:

```yaml
jolt_2:
  _each : "@//node()[not(node())]"
  _key  : |
    x = str( 'rating-%s' , $.name.name )
    x.replaceAll('.*Rating$', 'rating-primary')

  _value : $.value
```

Note the two important directives are `_key` which defines the key function, and the `_value` which defines the value of the key in the created map.

We also note the use of `@` to define `xelem` which let us figure out the name of the `leaf` node in question. 

Suppose now we take the opposite problem - from the output producing the complex structure back again - this is done again by the same type of mapper:

```yaml
jolt_3x:
  Rating : "#rating-primary"
  SecondaryRatings:
    _each: "@//node()"
    _when: "'primary' !@ $.name.name" 
    _key: "$.name.name.replace('rating-','')"
    _value: $.value
```

See the use of the `_when` condition.  This prevents the primary rating from being mapped.

#### Group 

Another type of transformation is `group by key`. Suppose we are given something as this:

```json
{
  "entities": [
    {
      "type": "alpha",
      "data": "foo"
    },
    {
      "type": "beta",
      "data": "bar"
    },
    {
      "type": "alpha",
      "data": "zoo"
    }
  ]
}
```

And we want to group by the `type`. This is easily accomplished by:

```yaml
jolt_12z:
  _each: "$.entities"
  _group : "$.type"
  "*" : "*"
```

Notice the use of the `_group` directive which acts like the `_key`. Engine aggregates all values within the same key in a list. 

#### Redirect 

While it is easy to write down a nested mapper - that is, a mapper inside another mapper - sometimes it is better to take out the mapper and `reuse` it. This is given by the   `redirect` directive as follows:

```yaml
base_mapper:
  x : "#a"
  y : "#b"
  t : "$.a + $.b"

mapper_redirect:
  _each: "#."
  "*" : "&base_mapper"
```

This transform supposed to take in an array of `json` objects like this:

```json
[
  { "a" : 10, "b":  20 },
  { "a" : 30, "b":  40 },
  { "a" : 30, "b":  10 }
]
```

And transform them into another array.



We note that the `mapper_redirect` use another child mapper - which is used with the `&`  directive - which tells the engine to refer to another mapper `base_mapper` . This way it is reusable. As of now we only support reuse within the same file.    




### Programming Further  

ZoomBA is being used as the underlying scripting/expression engine. Intention of these transform library is to augment `flower` to support object mapping. Composition and Aggregation of multiple mappers can be done using the standard flower dependency graph model.

#### XPath 

We support XPATH 1.0 because of the JXPath. The specification can be found here:
https://www.w3.org/TR/1999/REC-xpath-19991116/

#### ZoomBA 

A manual can be found here : https://nmondal.github.io/assets/pdfs/zoomba.pdf 

## References

1.  Question / Motivation - https://stackoverflow.com/questions/1618038/xslt-equivalent-for-json 
2. Other works 
	1. https://github.com/bazaarvoice/jolt
	2. https://github.com/schibsted/jslt/blob/master/tutorial.md
3. Building Blocks
	1. https://github.com/apache/commons-jxpath
	2. https://gitlab.com/non.est.sacra/zoomba/ 
4. Declarative Paradigm 
	1. https://pling.jondgoodwin.com/post/declarative-programming/
	2. https://stackoverflow.com/questions/129628/what-is-declarative-programming
	



