# FLOWER 

## Name 
Acronym for:
> [Flow][E]ngine For [R]eal Time

## Objective

### 'API' development 

From the last decade the cult of "api-development" has become synonymous with the following:

1. Exposing data over web ( CRUD )
2. Aggregating multiple other API calls or data sources 
3. Writing next to trivial logic ( apparently called *business logic* ) 
4. All of these w/o any inherent capability of parallelism, asynchrony and timeouts and retries 

In the end business and tech needs diverge - where it is like a self fulfilling circle
throttling business development. 

### Reduce Developer Footprint 

The previous paragraph suggests ( many ) wrong ideas about the nature of the Tech.
All of these has to stop, or has to be done by folks who are not even developers.
Software development must be democratized beyond developers.
If something was possible with 100 developers, then those MUST be solved with 10, if not less.
Less the merrier.
Goal of any development must starts with profit, and end with increasing margin.
This can not happen with an empire full of developers doing development for development's sake.
A crack team of 10 is more than capable of handling a top team of 100 developers.

And we should stop thinking about "long run". In the long run, the business will be more than dead.

### Stable Standard by Default  

What we are saying is anti thesis of what the so called "Industry Experts" promote.
Perhaps they know better, perhaps they mean well. That is not to say that we are willing to sacrifice stability.

Some code in some form is un-avoidable, no matter how differently one represents it.
Consider the goal of query. In 1960s it was COBOL and then SQL became a stable standard.
With the advent of *scale* people almost stopped using SQL - instead started using SQL-ish dialect which are DSLs.

Inventing DSL is key. So DSL it is.

## Design Goals

1. Business Logic outside Code 
2. Configurability with Turing Completeness 
3. Polyglot Environment 
4. Reduction of Development cost 
5. Get shit done

One paragraph suffices for each of them.

### Separation of Business from Core Engine 

Key issue of loss of dev time is logic is inside code.
This must be separated with *logic from business people* against *core logic to do anything*.
The *functional* stuff helped , a bit.

### Turing Completeness along with Configuration Based 

At most a business need must be Turing Complete. 
Also, given the separation of logic - one must build a configuration based engine.

### Everyone Wins - Polyglot

The snob attitude of one language is *better than another* must stop.
Given any problem there are sub problems and within them one language must trump others.
Massive data processing ? Perhaps you need Pandas. We do not know. 

Hence the system must support a large class of languages in which people can type in random code for business.
Hence, one does not need to find a specific set of developers - let's call them *business developers* with specified tech.

### Reduction of Development Cost

Previous paragraph reduces the development bar. *Stellar* is less than the operating word.
Imagine SQL. The people building SQL would be very less, 10 or less even.
Folks who would be using SQL to get something done would be in millions.

One does not need to reach that much margin, but a ratio of 1 core Engineer per 10~50 *business developer* is good enough.
Most of the cases they can be contractual employees.

Cost reduction immediately follows.



## Basic Idea 

### Flow 

### Engine 

### Scripting 

## Manual 


## Other Interesting Ideas

1. Rule Engines ( [https://en.wikipedia.org/wiki/Business_rules_engine](https://en.wikipedia.org/wiki/Business_rules_engine) )
2. Workflow Engines (  [https://en.wikipedia.org/wiki/Workflow_engine](https://en.wikipedia.org/wiki/Workflow_engine) ) 
3. Low Code (  [https://en.wikipedia.org/wiki/Low-code_development_platform](https://en.wikipedia.org/wiki/Low-code_development_platform) )
4. DSL ( [https://en.wikipedia.org/wiki/Domain-specific_language](https://en.wikipedia.org/wiki/Domain-specific_language)  )

Here is my own presentation on DSL :  
[https://www.slideshare.net/nogamondal/formal-methods-in-qa-automation-using-dsl](https://www.slideshare.net/nogamondal/formal-methods-in-qa-automation-using-dsl)


Unfortunately, the tech clan is dead against using any of them which does not allow them to write code, 
and clutter the whole business logic out of it.
Hence, while some of these sounds like great ideas, they are very less samples of it in production.


## License 

This work is under Apache 2.0.
Here is from where one get a copy:
[https://www.apache.org/licenses/LICENSE-2.0.txt](https://www.apache.org/licenses/LICENSE-2.0.txt)






