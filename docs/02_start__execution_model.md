---
id: model_helpers
title: Execution Model
sidebar_label: Execution Model
copyright: (C) 2018-2019 GoodData Corporation
---

GoodData.UI execution model allow you to easily create the different **data specification** inputs for the
visualization components.

The execution model and other smaller models are implemented in the `@gooddata/sdk-model` package. 

While it is possible to create the different inputs without the model functions we strongly recommend using the functionality
of the execution model - especially in conjunction with the [catalog-export](02_start__catalog_export.md) tool.

These two methods result in a very efficient developer experience, where you can spend time writing your application instead
of focusing on mundane tasks of simple object construction.

**NOTE**: GoodData.UI itself uses the execution model. We have evolved the execution model heavily based on the requirements
of the GoodData.UI and the applications which we build using the framework.

## Execution Model concepts

To simplify the creation of the various types of objects which specify what data to render, the model uses a combination
of two typical object creation patterns: factory function and builder. 


### Object Creation

The model provides a single factory function to create each type of object. There are several conventions:

-  The factory functions are always named as `new<ObjectType>`.
-  Factory functions can create only syntactically valid objects; all essential object parameters are required
   arguments to the function call
-  For simple objects, optional parameters will follow the required arguments
-  For complex objects - such as measures - the last parameter is a `modifications` function which you can implement.
   This function will receive an instance of builder with methods to customize the different parameters of the object
   under construction.

### Accessing Object Properties

The model provides accessor functions to access object properties. The naming convention for accessor function is always:

`<objectType>Property(object)` where the first and only parameter is the object to access.

For example: `attributeAlias`, `measureLocalId`, `measureFormat`, `filterAttributeElements` 

### Modifying Objects

The model provides specialized functions to allow modification of complex objects: measures and attributes. There
are several conventions:

-  The modification functions are always named `modify<ObjectType>`
-  The first parameter is the object to modify
-  The second parameter is the modification function. This function will receive an instance of builder with methods
   to customize the different parameters of the object.
-  The modification functions treat input as immutable, they will create a new objects instead of modifying the
   inputs 

### Local Identifiers and referencing objects

The `localIdentifier`, or `localId` for short, is a user-assigned identifier which you have to use when referencing 
attributes and measures in the scope of single visualization or execution. 

The execution model automatically generates stable `localIds` as it creates the attribute and measure objects. This is
very useful as you do not have to worry about this technicality. Furthermore, the execution model provides layers of
convenience so that you can pass the attribute and measure objects by value. 

There is, however, one limitation where this approach falls short: if you want to use same attribute or measure multiple
times in the same insight, you have to create a copy of the object and assign it a different localId yourself.

You can use the `modify<ObjectType>` functions to override the `localId`. The builders instances which your modification
function receives have functions to manipulate `localId`. The behavior of modify functions in regards to `localId` is as follows:

-  If you call the `m => m.defaultLocalId()`, then the default logic for `localId` generation will kick in _after_ all
   other object modifications are applied
   
-  If you call the `m => m.localId(customValue)`, then the modified object will have your custom localId

-  If you do not call `defaultLocalId` or `localId`, then the modification object will have same localId as the
   original object. 

## Attributes

The factory function to create an attribute is `newAttribute` and the modification function is `modifyAttribute`. The
usage is straightforward:

```javascript
import { newAttribute, modifyAttribute } from '@gooddata/sdk-model';

const attribute = newAttribute("displayFormIdentifier", m => m.alias("My Custom Name"));

// notice the call to defaultLocalId() - this ensures the new object will have different, generated localId
const sameAttributeDifferentName = modifyAttribute(attribute, m => m.alias("Corrected Name").defaultLocalId());
``` 

## Measures

The factory functions in the area are:

-  `newMeasure` creates a new measure from fact or MAQL Metric 
-  `newArithmeticMeasure` creates a new arithmetic measure
-  `newPopMeasure` creates a new over period comparison measure
-  `newPreviousPeriodMeasure` creates a previous period comparison measure

And the modification functions are:

-  `modifyMeasure` modifies measure-agnostic parameters (format, alias, localId) of any type of measure

**Example:**

```js harmony
import { newMeasure, newArithmeticMeasure, modifyMeasure } from '@gooddata/sdk-model';

const measureFromMaqlMetric = newMeasure('maqlMetricIdentifier');
const measureFromFact = newMeasure('factIdentifier', m => m.aggregation("avg").alias("Custom Name"));
const measureWithFilter = newMeasure('factIdentifier', m => m.filters(newPositiveAttributeFilter('displayFormId', ['value'])));

const arithmeticMeasure = newArithmeticMeasure(
                                [measureFromFact, measureFromMaqlMetric], 
                                "sum", 
                                m => m.alias("Custom Name For Arithmetic Measure").format("$#,#0.0")
                          );

// notice the call to defaultLocalId; this ensures that this new measure will have different localId - one that reflects
//  that the title and the format is different.
const modifiedArithmeticMeasure = modifyMeasure(arithmeticMeasure, 
                                    m => m.alias("Different Name For Arithmetic Measure").format("$#,#0").defaultLocalId()
                                  );
```

**NOTE**: We have a dedicated section for [time-over-time comparison](20_misc__time_over_time_comparison.md) measures where
you can find examples for the other factory functions together with detailed description of these special measures.

## Filters

The execution model provides several factory functions to create filter objects - one function for each type of
filter supported by GoodData.UI:

-  `newPositiveAttributeFilter`
-  `newNegativeAttributeFilter`
-  `newAbsoluteDateFilter`
-  `newRelativeDateFilter`
-  `newMeasureValueFilter`

We have a dedicate section for [filtering](30_tips__filter_visual_components.md) where you can find both in depth
explanation of the filters and examples using the factory functions.

## Sort Items

The execution model provides factory functions to create sort items and the respective locators. Use these
factory functions to create sort items:

- `newAttributeSortItem` to create a new attribute sort
- `newMeasureSortItem` to create a new measure value sort

For both of these you can specify attribute or measure either by `localId` or by passing the actual object. 

The second parameter is always the sort direction.

When sorting by measures that are scoped for particular attribute value - for example in pivot tables - you must
specify one or more attribute locators in order to pin-point the exact measure to sort by. You can conveniently
create attribute locators using the `newAttributeLocator` factory function.

**Example:**

```js harmony
import { newAttribute, newMeasure, newAttributeSort, newMeasureSort, newAttributeLocator } from '@gooddata/sdk-model';

const attribute = newAttribute('displayFormIdentifier', m => m.alias("Custom Dimension"));
const measure = newMeasure('maqlMetricIdentifier', m => m.alias("My Measure").format("#0"));

const attributeSort = newAttributeSort(attribute, "asc");

const measureSortWithoutAttributeLocator = newMeasureSort(measure, "asc"); 
const measureSortWithAttributeLocator = newMeasureSort(measure, "asc", [newAttributeLocator(attribute, "element-uri")])
```

### Attribute Area sort

You can also specify that the attribute sort should sort attribute values based on an aggregation function applied to 
all valid values belonging to each attribute value. This is extremely useful when sorting stacked visualizations such 
as stack bars or area charts.

Currently, only sorting by the `sum` function is supported.

The following example shows sorting a table with two measures and a 'Year' attribute. You can set sorting based on the Year attribute with:

```javascript
import { newAttribute, newAttributeAreaSort } from '@gooddata/sdk-model';

const attribute = newAttribute('displayFormIdentifier', m => m.alias("Custom Dimension"));

newAttributeAreaSort(attribute, "asc")
```

Consider the following original data:

| Year | 2006 | 2006 | 2007 | 2007 |
| :--- | :--- | :--- | :--- | :--- |
| Measures | M1 | M2 | M1 | M2 |
| Values | 1 | 2 | 3 | 4 |

The sorting function (`sum`) is applied to all attribute element values for each attribute element (2006 and 2007). 
Notice that the area sort is summing up values across different measures (M1 and M2):

| 2006 | 2007 |
| :--- | :--- |
| 1 + 2 = 3 | 3 + 4 = 7 |

Attribute values are then sorted by this computed value (3 and 7, respectivelly):

| Year | 2007 | 2007 | 2006 | 2006 |
| :--- | :--- | :--- | :--- | :--- |
| Measures | M1 | M2 | M1 | M2 |
| Values | 3 | 4 | 1 | 2 |
