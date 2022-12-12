# FRUIT : An Object Mapper Library 

```toc
```

[toc]

## Motivation 

### Introduction of the Problem 

### Industry Standards 

### Tenets - Design Principles 

### Naming 

`FRUIT` stands for **F**unctional **R**untime **U**niform **I**nterface for **T**ransformation.
It describes the basic ideas :
1. It is functional in nature ( as well as declarative )
2. It happens in runtime, there are no static options 
3. Uniform - basic structures are unfied 
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

Basic mapping is done by defining a mapper - somehere as a an `yaml` document with something like this:

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
public static Transformation<?> transformation(String mapFile, String transformName){

  Object tm = ZTypes.yaml(mapFile,true);
  Assert.assertTrue( tm instanceof Map );
  Map<String,Object> transforms = (Map<String, Object>)tm;
  Object val = transforms.get(transformName);
  Assert.assertNotNull(val);
  Map.Entry<String,Object> entry = Map.entry(transformName, val);
  return MapBasedTransform.fromEntry(entry);
}

public void someFunc(){
  // and in the code...
  Object o = load( "input.json");
  Transformation<?> tr = transformation( "mapping.yaml", "hello_x");
  Object r = tr.apply(o); // apply the transform
}
```

#### Analysis

So, whats happening in the mapper file? Mappings or transforms are defined as `map` in `yaml` - with `keys` not having  `_`  defines the structure of the output object.

As we can see  in our `hello` transform, there are two fields, one `maps` the `rating/primary/value` and another `rating/quality/value` into respective fields.

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

The current object context being mapped - is defined by the variable `$`.  As we progress down the sub objects, context gets replaced. 

This is equivalent to the `this` pointer.


### Transform Types 


#### Basic 

#### Identity 

#### Body Mapping 

#### List 

#### Object 

#### Map Object 

#### Group 

### Scripting 

#### XPath 

#### ZoomBA 



## References

1.  Question / Motivaton - https://stackoverflow.com/questions/1618038/xslt-equivalent-for-json 
2. Other works 
	1. https://github.com/bazaarvoice/jolt
	2. https://github.com/schibsted/jslt/blob/master/tutorial.md
3. Building Blocks
	1. https://github.com/apache/commons-jxpath
	2. https://gitlab.com/non.est.sacra/zoomba/ 



