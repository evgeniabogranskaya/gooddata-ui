---
id: model_helpers
title: Execution Model
sidebar_label: Execution Model
copyright: (C) 2018-2019 GoodData Corporation
---

The GoodData.UI execution model allows you to easily create different **data specification** inputs for the
visualization components.

The execution model and other smaller models are implemented in the `@gooddata/sdk-model` package. 

While you can create different inputs without the model functions, we strongly recommend using the functionality
of the execution model, especially in conjunction with the [catalog-export](02_start__catalog_export.md) tool.

These two methods result in a very efficient developer experience, where you can spend time writing your application instead
of focusing on constructing simple objects.

**NOTE:** GoodData.UI itself uses the execution model. We have evolved the execution model heavily based on the requirements
of GoodData.UI and the applications we build using the framework.

## Execution model concepts

To simplify the creation of various types of objects that specify what data to render, the model uses a combination
of two typical object creation patterns: factory function and builder. 

### Creating objects

The model provides a single factory function to create each type of objects. There are several conventions:

-  The factory functions are always named `new<ObjectType>`.
-  The factory functions can create only syntactically valid objects; all essential object parameters are required
   arguments to the function call.
-  For simple objects, optional parameters follow the required arguments.
-  For complex objects (such as measures), the last parameter is a `modifications` function which you can implement.
   This function will receive an instance of the builder with methods to customize different parameters of the object
   under construction.

### Accessing object properties

The model provides accessor functions to access object properties. The naming convention for accessor function is `<objectType>Property(object)`, where the first and only parameter is the object to access.

**Examples:** `attributeAlias`, `measureLocalId`, `measureFormat`, `filterAttributeElements`

### Modifying objects

The model provides specialized functions to allow modification of complex objects: measures and attributes. There
are several conventions:

-  The modification functions are always named `modify<ObjectType>`.
-  The first parameter is the object to modify.
-  The second parameter is the modification function. This function will receive an instance of the builder with methods
   to customize different parameters of the object.
-  The modification functions treat input as immutable. They will create new objects instead of modifying the
   inputs.

### Local identifiers and referencing objects

The `localIdentifier`, or `localId` for short, is a user-assigned identifier that you have to use when referencing 
attributes and measures in the scope of a single visualization or execution. 

The execution model automatically generates stable `localId`'s as it creates the attribute and measure objects. You can pass the attribute and measure objects by its value. However, if you want to use the same attribute or measure multiple times in the same visualization, you have to create a copy of the object and assign it a different `localId` yourself.

You can use the `modify<ObjectType>` functions to override the `localId` of an attribute or measure. The builder instances that your modification function receives have functions to manipulate `localId`. The behavior of the modification functions in regards to `localId` is as follows:

-  If you call the `m => m.defaultLocalId()`, the default logic for `localId` generation will kick in **after** all
   other object modifications are applied.
   
-  If you call the `m => m.localId(customValue)`, the modified object will have your custom `localId`.

-  If you do not call `defaultLocalId` or `localId`, the modification object will have the same `localId` as the
   original object.

## Attributes

The factory function that creates an attribute is `newAttribute`, and the modification function is `modifyAttribute`. The
usage is straightforward:

```javascript
import { newAttribute, modifyAttribute } from '@gooddata/sdk-model';

const attribute = newAttribute("displayFormIdentifier", m => m.alias("My Custom Name"));

// notice the call to defaultLocalId() - this ensures the new object will have a different, generated localId
const sameAttributeDifferentName = modifyAttribute(attribute, m => m.alias("Corrected Name").defaultLocalId());
``` 

## Measures

The factory functions are the following:

-  `newMeasure` creates a new measure from a fact or a MAQL metric.
-  `newArithmeticMeasure` creates a new arithmetic measure.
-  `newPopMeasure` creates a new period-over-period comparison measure.
-  `newPreviousPeriodMeasure` creates a new previous period comparison measure.

The modification function is `modifyMeasure`. It modifies measure-agnostic parameters (format, alias, localId) of any type of a measure.

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

// notice the call to defaultLocalId; this ensures that this new measure will have a different localId - one that reflects
// that the title and the format is different.
const modifiedArithmeticMeasure = modifyMeasure(arithmeticMeasure, 
                                    m => m.alias("Different Name For Arithmetic Measure").format("$#,#0").defaultLocalId()
                                  );
```

**NOTE:** You can find examples for the other factory functions together with a detailed description of time-over-time comparison measures in [Time-over-Time Comparison](20_misc__time_over_time_comparison.md).

## Filters

The execution model provides several factory functions to create filter objects, one function for each type of filters supported by GoodData.UI:

-  `newPositiveAttributeFilter`
-  `newNegativeAttributeFilter`
-  `newAbsoluteDateFilter`
-  `newRelativeDateFilter`
-  `newMeasureValueFilter`

**NOTE:** You can find in-depth explanation of the filters and examples using the factory functions in [Filter Visual Components](30_tips__filter_visual_components.md).

## Sort items

The execution model provides factory functions to create sort items and the respective locators:

- `newAttributeSortItem` creates a new attribute sort item.
- `newMeasureSortItem` creates a new measure value sort item.

For both of these, you can specify an attribute or measure either by `localId` or by passing the actual object. 

The second parameter is always the sort direction.

When sorting by measures that are scoped for a particular attribute value (for example, in pivot tables), you must specify one or more attribute locators to pinpoint the exact measure to sort by. You can conveniently create attribute locators using the `newAttributeLocator` factory function.

**Example:**

```js harmony
import { newAttribute, newMeasure, newAttributeSort, newMeasureSort, newAttributeLocator } from '@gooddata/sdk-model';

const attribute = newAttribute('displayFormIdentifier', m => m.alias("Custom Dimension"));
const measure = newMeasure('maqlMetricIdentifier', m => m.alias("My Measure").format("#0"));

const attributeSort = newAttributeSort(attribute, "asc");

const measureSortWithoutAttributeLocator = newMeasureSort(measure, "asc"); 
const measureSortWithAttributeLocator = newMeasureSort(measure, "asc", [newAttributeLocator(attribute, "element-uri")])
```

### Attribute area sorting

You can specify that the attribute sort should sort attribute values based on an aggregation function applied to 
all valid values belonging to each attribute value. This is extremely useful when sorting stacked visualizations such 
as stacked bar charts or area charts.

Currently, only sorting by the `sum` function is supported.

The following example shows sorting a table with two measures and a `Year` attribute. You can set sorting based on the `Year` attribute with:

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

The sorting function (`sum`) is applied to all attribute element values for each attribute element (`2006` and `2007`). 
Notice that the area sort is summing up values across different measures (M1 and M2):

| 2006 | 2007 |
| :--- | :--- |
| 1 + 2 = 3 | 3 + 4 = 7 |

Attribute values are then sorted by this computed value (3 and 7, respectivelly):

| Year | 2007 | 2007 | 2006 | 2006 |
| :--- | :--- | :--- | :--- | :--- |
| Measures | M1 | M2 | M1 | M2 |
| Values | 3 | 4 | 1 | 2 |
