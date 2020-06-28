---
title: What's New in Version 8.0
sidebar_label: What's New in Version 8.0
copyright: (C) 2007-2019 GoodData Corporation
id: whats_new_8
---

## New Repository, Repository Consolidation, and Versioning Unification

While GoodData.UI is primarily associated with React components and `@gooddata/react-components`, it is not just this 
single package that forms the GoodData.UI.

From day one, the `@gooddata/gooddata-js` and `@gooddata/typings packages` have been an integral part of GoodData.UI. 
With Version 8.0, we have fully acknowledged this and officially included these packages in GoodData.UI. 

We have created a single repository for all Gooddata.UI packages. We have merged code from multiple repositories 
into a single repository [gooddata-ui-sdk](https://github.com/gooddata/gooddata-ui-sdk). As a result, the existing repositories 
contain the previous major versions of the respective packages:

-  https://github.com/gooddata/gooddata-typings
-  https://github.com/gooddata/gooddata-js
-  https://github.com/gooddata/react-components

We have started the deprecation process of the previous major versions of these packages. All new features will be 
landing only on top of the new major version into the new [gooddata-ui-sdk](https://github.com/gooddata/gooddata-ui-sdk) repository.

As part of the consolidation, we renamed the `@gooddata/gooddata-js` and `@gooddata/typings` packages to 
`@gooddata/api-client-bear` and `@gooddata/api-model-bear`, correspondingly. 

All packages originating from the new gooddata-ui-sdk repository have common versioning. The first version 
of each new package is 8.0.0.

## More Granular Packaging

We have split the single large `@gooddata/react-components` package into a number of smaller packages, each with its 
own focus area. You can now pick only the packages that you really need and thus reduce build time and potentially 
also the size of your own applications. We also took this as an opportunity to revisit naming of the packages. 

Here are the most important new packages you should know about:

-  `@gooddata/sdk-ui` contains the React infrastructure and headless components such as the Execute component.
-  `@gooddata/sdk-ui-charts` contains the components for all charts.
-  `@gooddata/sdk-ui-pivot` contains the Pivot Table component.
-  `@gooddata/sdk-ui-filters` contains the filtering components.
-  `@gooddata/sdk-ui-ext` contains the components for embedding insights created in Analytical Designer.

To simplify migration, we have also created an umbrella package that exports the complete GoodData.UI 
functionality: `@gooddata/sdk-ui-all`.

## Architectural Support for Different Analytical Backends
We have significantly refactored GoodData.UI in order to draw a clean line between the different UI components and the 
contracts they have towards a backend server that calculates the data to render. GoodData.UI now defines a backend 
Service Provider Interface (SPI) that you can implement to meet your specific needs as long as the implementation 
matches the contracts. 

For now, the full documentation of the SPI is provided only at the code level. For more information, see 
the `@gooddata/sdk-backend-spi` package.

## Improved API Governance and Documentation
We admit that previous versions of GoodData.UI were lacking severely in this area. We have massively improved this 
in Version 8.0, and we have additional improvements planned for the future. For starters, we have diligently 
documented everything that various GoodData.UI packages export. On top of this, each exported element now comes 
with API maturity annotations:

-  `@alpha`: initial API; highly likely to change outside of the SemVer specification 
-  `@beta`: mostly stable API; details may change outside of the SemVer specification 
-  `@public`: stable API; follows the SemVer specification 
-  `@internal`: internal API; may change or disappear at any time 

All this documentation is included in the published packages so that you can conveniently access it in an IDE of your choice.

Additionally, all parts of GoodData.UI are now subject to automated API fingerprinting, which will greatly minimize a 
big group of possible breaking API changes.

## Data Model at Your Fingertips
We have greatly improved the Model infrastructure, which many of our customers use to specify what and 
how to visualize using our charts. There were so many changes and improvements that we decided to dedicate the 
new `@gooddata/sdk-model` package to this infrastructure exclusively. 

The `sdk-model` package contains a lightweight object-oriented model (types and functions) to simplify creation and 
modification of all objects that are normally used to construct input parameters for executions done by the backend or 
used as inputs to different visualization components.

To complete this story, we have enhanced the `catalog-export` tool so that it is now able to construct code constants 
representing the various logical data model (LDM) objects in your workspace. You can then use these constants directly in your application when defining visualizations. For instance, you can now do this:

```jsx
<AreaChart
	measures={[Ldm.$FranchiseFees]}
	viewBy={[Ldm.LocationCity]}
	stackBy={Ldm.LocationState}
/>
```

-- all without the need to write any boilerplate code. The [catalog-export](02_start__catalog_export.md) tool does all that for you.

## Improved Custom Executions
We have made significant changes and quality-of-life improvements when it comes to triggering executions on the 
backend and working with the results returned by the backend.

First off, we have realized that exposing the "raw" Execute AFM as a means to perform custom executions is not a 
good idea because it is tied to code too tightly with a particular implementation. Therefore to loosen up, we have 
created APIs and abstractions on top of the Execute AFM. The AFM concepts are still in place but now come in nicer 
packaging and streamlined into a fluent API.

For instance:

```javascript
backend
.workspace("myWorkspace")
.execution()
.forItems([Ldm.Won, Ldm.Region])
.withSorting(newAttributeSort(Ldm.Region))
.execute()
```

At the lowest level, this is how you can create and trigger executions. We have also made significant changes in 
regards to the structure of the results.For more information, see [Custom Executions](50_custom__execution.md).

## Improved Execute Component
We went above and beyond the execution API changes and came up with additional improvements and simplifications for 
the Executor React component. The Executor component no longer concerns itself with "low-level stuff" such as AFM. 
Instead, it provides a more curated yet powerful approach to get and process calculated results.

It does this by introducing data series and the slices view on the data. Imagine, for example, a table with rows per 
region and a number of columns for two metrics, `Won` and `Lost`. Such a table contains two data series, 
one for each metric. Each data series then contains a number of data points sliced by different regions.

The improved Executor component and the surrounding infrastructure allows you to work with the data in this way 
without worrying about AFM and ResultSpec. See the code backing our live examples for demonstration of these 
new capabilities.

There is way more in the data series and slices than the example shows. The important parts to mention are that you 
can iterate through the data in any direction and that all the essential metadata about each data point is 
readily available.

For more information, see [Create a Custom Visualization](50_custom__create_new_visualization.md).

## Improved React Integration
We have created a couple of React Context implementations that allow you to easily set a backend and workspace 
with which the different components work. Additionally, we have created `FilterContext` that you can use to set filters 
that the different components should use.

## Simplified Testing
We have created tools and infrastructure to simplify testing of your applications offline, without the real 
GoodData backend. You can now use the `@gooddata/mock-handling` tool to simplify creation and management of recorded or 
mock data. This recorded data can be made available to your application almost transparently, by using a recorded 
backend implementation included in `@gooddata/sdk-backend-mockingbird`.

We are successfully using this in our own testing project `@gooddata/reference-workspace` - check it out.
