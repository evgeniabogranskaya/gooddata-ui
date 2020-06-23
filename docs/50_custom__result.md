---
title: Specify Result Structure
sidebar_label: Specify Result Structure
copyright: (C) 2007-2018 GoodData Corporation
id: result_specification
---

Before you start the custom execution on an Analytical Backend, you have to specify details of how the result
should be cross-tabulated - in other words, how should the backend lay out the data into dimensions and how to
sort data in the dimensions.

## Dimensions

The dimensions communicate to the Analytical Backend how to organize data into arrays. Imagine an attribute in columns vs. rows.
 
Each dimension specifies *items*. These items could be attributes' `localIdentifier`s or a special `measureGroup` identifier.
The `measureGroup` identifier tells the executor to place all measures from the execution into the dimension. 

Each dimension optionally specifies *totals* as the Analytical Backend calculates totals during the cross-tabulation,
as it lays data into the dimensions.

### Default dimensions

By default, execution will set dimensions so that all attributes are in the first dimension and the `measureGroup` is in
the second dimension. To override this behavior you can use the `execution.withDimensions()` function and specify
dimensions yourself.

### Creating dimension specification

You can create dimension specification using the `newDimension` factory function:

```javascript
newDimension(items, totals)
```

Where:

-  `items` is an array of attribute `localIdentifiers`, attributes and/or `measureGroup`
-  `totals` is an array total definitions; this is optional

The definition and behavior of totals is described more in depth in [table totals](30_tips__table_totals.md).

The execution's `withDimensions` function accepts an array of dimension specifications. You can define either *one* or
*two* dimensions. 

### Creating two dimensional specification

The GoodData.UI execution model provides a convenience function to create two dimensional specification:

```javascript
newTwoDimensional(firstDimItems, secondDimItems)
```

Where:

-  `firstDimItems` are items that you would normally send to the `newDimension` function; these items will be
   in the first dimension
-  `secondDimItems` are items that you would normally send to the `newDimension` function; these items will be
   in the second dimension

### Examples of dimensions

#### Execution with one measure and one attribute

Use case: one data point calculated from measure scoped for each attribute value

```javascript
import { MeasureGroupIdentifier, newDimension } from "@gooddata/sdk-model";
import { Ldm } from "./ldm";

execution()
    .forItems([Ldm.LocationState, Ldm.$FranchiseFees])
    .withDimensions([newDimension([Ldm.LocationState, MeasureGroupIdentifier])]);

{  
data: [ 32000, 41000, 77000 ]
}
```

#### Execution with two measures and one attribute 

Use case: simple table; row per attribute value, measures are in columns

```javascript
import { MeasureGroupIdentifier, newTwoDimensional } from "@gooddata/sdk-model";
import { Ldm } from "./ldm";

execution()
    .forItems([Ldm.LocationState, Ldm.$FranchiseFees, Ldm.$TotalSales])
    .withDimensions(newTwoDimensional([Ldm.LocationState], [MeasureGroupIdentifier]));

{
    data: [
      [ 32000, 300 ],
      [ 41000, 345 ],
      [ 77000, 590 ]
    ]
}
```

Alternative use case: simple table; row per measure, one column per attribute value

```javascript
import { MeasureGroupIdentifier, newTwoDimensional } from "@gooddata/sdk-model";
import { Ldm } from "./ldm";

execution()
    .forItems([Ldm.LocationState, Ldm.$FranchiseFees, Ldm.$TotalSales])
    .withDimensions(newTwoDimensional([MeasureGroupIdentifier], [Ldm.LocationState]));

// `executionResult` is returned with measures in the first dimension:
{
data: [
  [ 32000, 41000, 77000 ],
  [ 300, 345, 590 ]
]
}
```

#### Execution with one measure and two attributes

Use case: stacked charts, pivot tables; one attribute used to create rows; one column per measure value calculated for each value of the second attribute

```javascript
import { MeasureGroupIdentifier, newTwoDimensional } from "@gooddata/sdk-model";
import { Ldm } from "./ldm";

execution()
    .forItems([Ldm.LocationState, Ldm.LocationCity, Ldm.$FranchiseFees])
    .withDimensions(newTwoDimensional([Ldm.LocationState], [Ldm.LocationCity, MeasureGroupIdentifier]));

{
data: [
  [ 13000, 19000 ],  // state of California, one element for each city
  [ 15000, 26000 ],  // state of Florida, one element for each city
  [ 31000, 36000 ]   // state of Texas, one element for each city
]
}
```

### Totals

Optionally, you can define totals for each dimension. Totals are used to get aggregated data over several rows or columns of measure data.

See [table totals](30_tips__table_totals.md) page for more information how to define totals.

## Sorting

When you are constructing execution you can optionally specify what server-sorting to apply. This is done by calling the
`withSorting` function as you are preparing the execution.

```javascript
import { MeasureGroupIdentifier, newTwoDimensional, newAttributeSort } from "@gooddata/sdk-model";
import { Ldm } from "./ldm";

execution()
    .forItems([Ldm.LocationState, Ldm.LocationCity, Ldm.$FranchiseFees])
    .withDimensions(newTwoDimensional([Ldm.LocationState], [Ldm.LocationCity, MeasureGroupIdentifier]))
    .withSorting(newAttributeSort(Ldm.LocationState, "desc"));
```

See [execution model](02_start__execution_model.md) page for more information how to define sorts.

### Execution Result

GoodData.UI models execution as a two-phase process. When you start the execution you almost immediately obtain the
the **execution result** - or an error in case you did not specify correct execution. A valid execution result contains
descriptors of the shape and content of dimensions.

For example if you start execution for one attribute and one measure and ask for two dimensional result with rows for
attribute values and column for measure, then the execution result will contain descriptors for two dimensions. And in
each dimension descriptor will be the essential detail about the attribute and measure respectively.

Once you have a valid execution result, the Analytical Backend is already running the computation in the background. You can
use the execution result's `readAll` or `readWindow` methods to access all of the computed data or just a window (page) of the
computed data.

When the data is available, these methods will return an instance of **data view**. This contains the calculated data and
headers that describe the calculated data.

For in-depth description of the result structures, please see [analytical backend SPI documentation](https://github.com/gooddata/gooddata-ui-sdk/tree/master/libs/sdk-backend-spi/src/workspace/execution).

We do not recommend to work directly with the raw execution results. Instead, use the abstractions and convenience
methods provided by the `DataViewFacade`. 

```javascript
import { DataViewFacade } from "@gooddata/sdk-ui";

const executionResult = await execution().forItems(...).execute();
const dataView = await executionResult.readAll();

const facade = DataViewFacade.for(dataView);
``` 

## Quick Reference of Dimensions

<table>
<tbody>
<tr>
<th class="confluenceTh">Items</th>
<th class="confluenceTh"><code>dimensions:</code></th>
<th class="confluenceTh"><code>executionResult: { }</code></th>
</tr>
<tr>
<td class="confluenceTd"><span>1 measure<br>1 attribute (A)</span></td>
<td class="confluenceTd">[ 'A', 'measureGroup' ]</td>
<td class="confluenceTd"><pre><code>        A1    A2    A3     <span style="color: rgb(84,84,84)">← elements of attr A</span><br>data: [ ... , ... , ... ]&nbsp; ←&nbsp;values of the measure</code></pre></td>
</tr>
<tr>
<td class="confluenceTd" colspan="1">2 measures (M1, M2)</td>
<td colspan="1" class="confluenceTd">[ 'measureGroup' ]</td>
<td colspan="1" class="confluenceTd"><pre><code>        M1    M2     <span style="color: rgb(84,84,84)">← elements of measureGroup</span><br>data: [ ... , ... ]</code></pre></td>
</tr>
<tr>
<td class="confluenceTd">2 measures (M1, M2)<br>1 attribute (A)</td>
<td class="confluenceTd">[ 'A', 'measureGroup' ]</td>
<td class="confluenceTd"><pre><code>        A1-M1  A1-M2  A2-M1  A2-M2   <span style="color: rgb(84,84,84)">← cartesian product of</span><br>data: [ .... , .... , .... , .... ]    elements from A and <br>                                       measureGroup</code></pre></td>
</tr>
<tr>
<td class="confluenceTd">empty first dimension<br>+ the same as above</td>
<td class="confluenceTd">[ ],<br> [ 'A', 'measureGroup' ]</td>
<td class="confluenceTd"><pre><code>data: [<br>    A1    A2    A3    ← the same as above<br>  [ ... , ... , ... ]<br>]</code></pre></td>
</tr>
<tr>
<td colspan="1" class="confluenceTd"><span>2 measures (M1, M2)</span> <br> <span>1 attribute (A)<br> <br> </span>// typical for viewBy chart</td>
<td colspan="1" class="confluenceTd">[ 'A' ],<br>[ 'measureGroup' ]<br></td>
<td colspan="1" class="confluenceTd"><pre><code>data: [                  // it can be understood as data[A][M]<br>    M1    M2                aka first dimension = elems of A<br>  [ ... , ... ],  ← A1      and second = elems of measureGroup<br>  [ ... , ... ]   ← A2<br>] </code></pre></td>
</tr>
<tr>
<td colspan="1" class="confluenceTd">2 attributes (A, B)<br>1 measure (M1)<br> <br>// typical for stackBy chart</td>
<td colspan="1" class="confluenceTd">[ 'A' ],<br>[ 'B', 'measureGroup' ]</td>
<td colspan="1" class="confluenceTd"><pre><code>data: [                  // notice it doesn't matter in which<br>    B1-M1  B2-M1            dimension the measureGroup is<br>  [ .... , .... ],  ← A1    placed (as it has only one measure)<br>  [ .... , .... ]   ← A2<br>] </code></pre></td>
</tr>
</tbody>
</table>

## Find out more

For more examples, sign up to the Live Examples and watch the Network tab in your browser's Developer console. You may also experiment by sending your own `resultSpec`s: for example, use the [Postman application](https://www.getpostman.com/apps).
