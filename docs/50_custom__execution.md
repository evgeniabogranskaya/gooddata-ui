---
title: Custom Executions
sidebar_label: Custom Executions
copyright: (C) 2007-2018 GoodData Corporation
id: afm
---

An **execution** is a combination of attributes, measures, and filters that describes what data you want to calculate. 

**NOTE:** A measure contains numeric data (for example, revenue). Measures can be sliced by selected attributes (for example, city, date in years, or both) and filtered by attribute values or date constraints. For more information, see the [main concepts](01_intro__platform_intro.md#main-concepts).

## End-to-end flow

You can use an instance of the Analytical Backend to conveniently construct and perform the executions using a fluent API.

```javascript
const backend = bearFactory();

const result = await backend
   .workspace("project_id")
   .execution()
   .forItems(measuresAndAttributes, filters)
   .withSorting(...sorting)
   .withDimensions(dimensions)
   .execute();

const firstPage = await result.readWindow([0, 0], [10, 10]);
const allData = await result.readAll();
```

In the above example, the code starts an execution in the workspace with `project_id`. It specifies execution for the `measuresAndAttributes` array filtered by the `filters` array. 

On top of that, it configures `sorting` of the result data and how to lay out the data into `dimensions`.

This article describes the different items and filters that you can input as execution items. For the description of sorting
and dimensionality setup, see [Specify the Result Structure](50_custom__result.md).

## Attribute

Each attribute is defined by its `displayForm` that will be used to slice the data. You can create an attribute 
definition using the following factory function:

```javascript
    const attribute = newAttribute('<attribute-displayForm-identifier>');
```

Each attribute requires a `localIdentifier` that you can use to reference the attribute in the scope of the execution (for instance, when specifying sorting). The factory function assigns a stable `localIdentifier` for you. 

You can optionally override the `localIdentifier` and also the title of the attribute in the factory function call:

```javascript
    const attribute = newAttribute('<attribute-displayForm-identifier>', m => m.localId('myLocalId').alias('My Attribute'));
```

All attributes are defined using their `displayForm` identifiers. This assures that you application can work on top of
different workspaces which have the same logical data model (LDM). For more information, see [Determine the Attribute Value ID](https://help.gooddata.com/display/doc/Determine+the+Attribute+Value+ID).

## Filter

You can limit the execution by providing one or more `filter`'s. Multiple filters are always interpreted as an intersection of all individual filters \(`f1 AND f2 AND f3...`).

The structure of individual filters is identical to the `filters` prop that is used to filter visual components. For more information, see [Filter Visual Components](30_tips__filter_visual_components.md).

## Measure

Measures in the scope of execution indicate what values the Analytical Backend must calculate and include in the result,
potentially sliced as indicated by the the different attributes.

You can construct measures of multiple types:

-  Measures created by aggregating facts in your LDM
-  Measures created by referencing an existing, potentially complex MAQL metric
-  Time-over-time comparison measures constructed by "shifting" the calculation in time
-  Arithmetic measures constructed by combining existing measures as operands of arithmetic operations

The [catalog-export](02_start__catalog_export.md) tool will automatically create measure definitions for all facts
and MAQL metrics in your workspace.

You can then use the [execution model functions](02_start__execution_model.md) to create [arithmetic measures](20_misc__arithmetic_measure.md)
and [time-over-time comparison measures](20_misc__time_over_time_comparison.md).

### Aggregation inside a measure

Each measure created from a fact can specify `aggregation` of data. Aggregation is represented by a string value that defines the aggregation type.

| Type | Description |
| :--- | :--- |
| `'sum'` | Returns a sum of all numbers in the set |
| `'count'` | Counts unique values of a selected attribute in a given dataset determined by the second attribute parameter  (ignores the measure's `format` value and uses the default value `#,##0` instead) |
| `'avg'` | Returns the average value of all numbers in the set; null values are ignored |
| `'min'` | Returns the minimum value of all numbers in the set |
| `'max'` | Returns the maximum value of all numbers in the set |
| `'median'` | Counts the statistical median - an order statistic that gives the "middle" value of a sample. If the "middle" falls between two values, the function returns average of the two middle values. Null values are ignored. |
| `'runsum'` | Returns a sum of numbers increased by the sum from the previous value \(accumulating a sum incrementally\) |

[catalog-export](02_start__catalog_export.md) generates measure definitions for all available aggregations for you.

### Filters in a measure definition

Each measure can be filtered by attribute filters. Filters are represented by an array of `IFilter` objects. 

Only one filter of the `DateFilter` type is allowed in the measure's filter definition.

* When both the measure filter of the `DateFilter` type and the global filter of the `DateFilter` type are set with 
  the **same** date dimension, the measure date filter overrides the global date filter for this measure 
  \(global date filters are still applied to other measures that do not have a measure date filter defined\).
* When the measure filter of the `DateFilter` type and the global filter of the `DateFilter` type are set 
  with **different** date dimensions, the filters are interpreted as an intersection of those filters (`f1 AND f2`).

### Show a measure as a percentage

When the execution runs on the Analytical Backend, the result measure data is, by default, returned as raw values \(numbers\).

If you want the measures data to be displayed as a percentage instead, you can use the `modifySimpleMeasure` function
of the execution model to turn on the `computeRatio` functionality:

```javascript
import { modifySimpleMeasure } from "@gooddata/sdk-model";
import { Ldm } from "./ldm";

// This will modify an existing simple measure, turn on the computeRatio functionality and associate a new, default localId
const ratioMeasure = modifySimpleMeasure(Ldm.$FranchiseFees, m => m.ratio().defaultLocalId());

// This will modify an existing simple measure, turn off the computeRatio functionality and associate a new, default localId
const noRatio = modifySimpleMeasure(ratioMeasure, m => m.noRatio().defaultLocalId());
```

When the property is enabled, the measure's `format` value is ignored. The default format `#,##0.00%` is used instead.

### Compare a measure over time

To compare a measure over time, add one of the supported measure types described 
in [Time Over Time Comparison](20_misc__time_over_time_comparison.md) to `afm.measures`.

### Calculated measures

To create arithmetic measures (for example, when you want to subtract a measure from another measure), 
add arithmetic measures described in [Arithmetic Measure](20_misc__arithmetic_measure.md) to the execution items. Then, add all the arithmetic measure operands to the execution itself.
