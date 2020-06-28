---
title: Breaking Changes in Version 8.0
sidebar_label: Breaking Changes in Version 8.0
copyright: (C) 2007-2019 GoodData Corporation
id: breaking_changes_8
---

## Repackaging

The consolidation of all GoodData.UI packages under one repository, the desire to simplify versioning, and the major architectural changes we had to undertake for Version 8.0 have led us to reworking of the GoodData.UI packaging. 

Starting with Version 8.0, we have established new naming conventions. Therefore, the existing packages are reaching the end of their development line and will be switched to maintenance mode:

* `@gooddata/typings`
* `@gooddata/gooddata-js`
* `@gooddata/react-components`

New packages are taking their place, all starting at Version 8.0. Unless this documentation states otherwise, the contents of the old packages are available in the respective new packages. 

To adopt this change,  modify dependencies in your `package.json` file and update imports in your code. Some packages had additional breaking changes and will require further modification.

Here is mapping between the packages:

-  `@gooddata/typings` is renamed to `@gooddata/api-model-bear`.
-  `@gooddata/gooddata-js` is renamed to `@gooddata/api-client-bear`.
-  `@gooddata/react-components` is dissolved into the following packages:
    -  `@gooddata/sdk-model` contains the Model infrastructure that creates attributes, measures, and filters that serve as inputs to the various visualization components.
    -  `@gooddata/sdk-ui` is the core package of GoodData.UI. It contains fundamental type definitions, React integration and basic React components that integrate with the GoodData platform. This is where the Execute component lives.
    -  `@gooddata/sdk-ui-charts` contains components for all charts.
    -  `@gooddata/sdk-ui-geo` contains components for the Geo Pushpin Chart component.
    -  `@gooddata/sdk-ui-pivot` contains the Pivot Table component.
    -  `@gooddata/sdk-ui-filters` contain all filtering components.
    -  `@gooddata/sdk-ui-ext` contains components for embedding insights created by Analytical Designer. 

## Styling Changes
The only styling changes in Version 8.0 are those that were needed to reflect the repackaging as described in 
[Repackaging](#repackaging). The new packages now have their own primary stylesheets.

To adopt this change, import the stylesheets from the respective package:

-  `@gooddata/sdk-ui-charts/styles/main.css`
-  `@gooddata/sdk-ui-pivot/styles/main.css`
-  `@gooddata/sdk-ui-filters/styles/main.css`

    This imports styles for all filter components. If you are not using all the filter components in your application, 
    import only the styles for the components that you use: `attributeFilter.css`, `dateFilter.css`, or `measureValueFilter.css`.

Apart from these structural changes, the styles themselves remain the same and there are no breaking changes when it 
comes to classes.

## Removed Components

We have removed several components from Version 8.0. Each removed component has a fully functional replacement.

### DataLayer Infrastructure

This infrastructure was available in `gooddata-js`. Its role was to wrap and simplify performing executions and to 
work with data results. We have effectively superseded this infrastructure by the new Analytical Backend APIs. 

The Analytical Backend API section pertaining to workspaces has dedicated execution APIs. These APIs work with the 
same concepts that were used in the data layer, and the functionality available through them is a superset of what 
was available in DataLayer. They allow you to specify the custom execution and work with its results in an easier 
and more convenient way.

To adopt this change, switch to using the Analytical Backend APIs.

For more information, see [Custom Executions](50_custom__execution.md).

### AFM-driven Visualizations 

We have removed all AFM-driven charts from GoodData.UI. These components had been officially deprecated for more 
than a year and reached their end-of-life in September 2019. 

For each removed AFM-driven component, GoodData.UI comes with non-AFM alternatives. The non-AFM components have a 
more friendly interface using which you can specify what the chart should render.

To adopt this change, switch to the appropriate non-AFM components. For instance, if you were using the AfmBarChart 
component, switch to the BarChart component. To learn more about how to construct the visualizations, see the official 
documentation, live examples, or the code documentation.

### Legacy Table Component 
We have had the Pivot Table component since Version 7.0. Feature-wise, the Pivot Table component has all features of the legacy 
Table component and adds more on top of them. The legacy Table component has already been phased out from 
Analytical Designer and KPI Dashboards. In Version 8.0, we have removed the Table component from GoodData.UI.

To adopt this change, change all uses of the Table component to the Pivot Table component. To learn more about how to 
use the Pivot Table component, see [Pivot Table](10_vis__pivot_table_component.md).

### Visualization Component
We have redesigned the way insights created in Analytical Designer are embedded into applications. In the new design, 
the embedding uses the exact same components and code paths as Analytical Designer or KPI Dashboards. This was not the 
case before, which may have resulted in bugs and issues.

Additionally, we have recognized that the term "Visualization" is fairly overloaded within GoodData.UI and have 
opted to also rename this component to "InsightView".

To adopt this change, use the new InsightView component to embed insights created by Analytical Designer.

## Changes in Communication with the Backend

In Version 7.0, GoodData.UI used the `@gooddata/gooddata-js` package (mainly, its API client) to communicate with 
the backend. The instance of the API client, commonly named `sdk`, was set globally for the application or was passed 
directly via component props.

With Version 8.0, we have introduced a new implementation-agnostic layer to communicate with the backend and to no 
longer work directly with the API client. This has the following impact:

* The application initialization logic has changed and now looks like this:
    ```javascript
    import { bearFactory } from '@gooddata/sdk-backend-bear'

    const backend =
        bearFactory({ hostname: ""<yourDomain>"" })
        .withAuthentication(<authenticationProvider>);
    ```

* The props used to pass an instance of the object to use for communication with the backend have been renamed:

    -  Before: all charts had the `sdk` property.
    -  After: all charts have the `backend` property.

To adopt these changes, create the backend once and then do **one** of the following:

* Export the constant and use it in all visualizations:
    ```jsx<BarChart 
    backend={backend}
    workspace={“<yourWorkspace>”}
    measures={[Ldm.Won]}
    viewBy={[Ldm.Product]} />
    ```

* Use the newly available BackendProvider at the root of your application and remove use of the `sdk` property from 
your application:
    ```jsx
    <BackendProvider backend={backend}>
        <BarChart 
            workspace="<yourProjectId>"
            measures={[Ldm.Won]}
            viewBy={[Ldm.Product]} 
        />
    </BackendProvider>
    ```

To learn more details about initialization, see the <link to getting started>. For more information about configuring 
important aspects such as authentication, see <link>.

## Model Infrastructure and Execution Changes

### Model Infrastructure
We have overhauled the Model infrastructure. All its functionality is now in the `@gooddata/sdk-model` package. 
The APIs from Version 7.0 were reworked and changed significantly. Functionality-wise, the Model infrastructure in 
Version 8.0 is a superset of what we had in Version 7.0.

The main reason why we decided to make these changes is that we wanted GoodData.UI itself and all our applications 
to use this Model infrastructure. While the syntax changes are numerous, everything that was possible with the Model 
infrastructure in Version 7.0 is possible in Version 8.0 and with a fuller, more friendly and convenient API.

To adopt this change, use the `catalog-export` tool to generate the logical data model (LDM) object definitions for you. 
Unless your application works with arbitrary workspaces (where you do not know the LDM at compilation time), 
it should use the generated LDM.

For more information, see [Export Catalog](02_start__catalog_export.md).

### Execution
The updated LDM objects feed seamlessly into the updated infrastructure and APIs that trigger executions. We have 
significantly modified how GoodData.UI communicates with the backend. The Execute AFM component is no longer in use.

The API client package of the GoodData platform, `@gooddata/api-client-gd` (formerly known as `gooddata-js`), 
still exposes the same functionality as it did before - you can find the "raw" Execute AFM functionality there. 
However, we no longer use this directly in GoodData.UI, and we advise that you not do so either. Although the AFM 
functionality is going to remain in the `api-client-gd` package, the layers that we built on it provide additional 
features and are easier to use.

To adopt this change, migrate all custom executions so that they use the analytical backend SPI. The new entry 
point to execution is: 

`backend.workspace(<projectId>).execution()`

This is a gateway to the new execution APIs where you can set up custom execution with the same flexibility and 
more convenience than with the lower-level Execute AFM component. 

**NOTE:** For information about further levels of flexibility and performance that you can achieve with the new execution 
infrastructure, see Advanced Topic: Backend Decorators.

## Changes in Component Props
We have done additional, trivial breaking changes on the component level. They are mostly about renaming the props, 
getting rid of obsolete, defunct or deprecated props. Apart from these types of changes, we have modified the 
structure of drill events.

### Renamed Properties
The `projectId` prop on the different visualizations has been renamed to `workspace`. The type and content of the prop 
remain the same: when integrating with the GoodData platform, the value represents the ID of a project.

### Deleted Properties

We have determined that several props were included in the component APIs by mistake and were never used. Those props 
are `environment` and `onLoadingFinished` from the chart components. We deleted these props.

Additionally, we deleted the `columnMeasure`s prop and the `lineMeasures` prop from the Combo Chart component. These props 
were deprecated with the introduction of primary and secondary measure props.

## Changes in the Drilling Area

In Version 7.0, we introduced a couple of changes to drilling in a backward-compatible fashion, including the `onDrill` 
callback. The events sent through this callback have a slightly different payload: the indicators in the intersection 
are more precise.

In Version 8.0, we deleted the original drilling and kept only the new API. These are the most notable breaking changes:

-  The `onFiredDrillEvent` parameter has been deleted.
-  The `onDrill` event is now the only callback for drill events.
-  The `IDrillEvent` event and all events in its family have been deleted.
-  The `IDrillEventExtended` event has been renamed to `IDrillEvent`; the new events with extended header information 
   are included.
-  The `executionContext` property (containing AFM) of drill events has been deleted. As a replacement, we have added 
   the `dataView` property containing an instance of `IDataView`. 
-  The `HeaderPredicateFactory` property has been renamed to `HeaderPredicates`.

The new data view is a superset of `executionContext`. It contains everything you may need:
-  The execution definition (formerly known as AFM)
-  The execution result
-  The data that was visualized at the time the user triggered the drill event

On top of this, you can wrap an instance of the data view in `DataViewFacade`. `DataViewFacade` provides a host of added 
value functionality that you can use to work with the definition, data, or headers in a more convenient way.

## API Client Changes

We have consolidated the `@gooddata/gooddata-js` and `@gooddata/typings` packages under the new gooddata-ui-sdk repository. 
The `gooddata-js` package has been renamed to `@gooddata/api-client-bear`. The typings package has been renamed to 
`@gooddata/api-model-bear`. Both new packages start with Version 8.0.

On top of this, we have made several breaking changes to anchor the responsibilities of these packages to be low-level 
REST API wrappers.

Most breaking changes have been made in the `@gooddata/api-model-bear` package (formerly known as `@gooddata/typings`).

The following  types and namespace have been removed and replaced with  alternatives:

-  The `AFM` namespace has been removed. Use the `GdcExecuteAFM` namespace instead.
-  The `VisualizationInput` namespace has been removed. Use the contents of `@gooddata/sdk-model` instead.
-  Localization is no longer part of the package. Use `ILocale` from `@gooddata/sdk-ui` instead.

The following  namespaces and their contents have been renamed:

| Old name | New name | 
| :------- | :------- | 
| `ExecuteAFM` | `GdcExecuteAFM` |
| `Execution` | `GdcExecution` |
| `ExtendedDateFilters` | `GdcExtendedDateFilters` |
| `VisualizationObject` | `GdcVisualizationObject` |
| `IVisualizationAttribute` | `IAttribute` |
| `VisualizationObjectExtendedFilter` | `ExtendedFilter` |
| `VisualizationObjectFilter` | `Filter` |
| `VisualizationObjectDateFilter` | `DateFilter` |
| `VisualizationObjectAttributeFilter` | `AttributeFilter` |
| `IVisualizationObjectRelativeDateFilter` | `IRelativeDateFilter` |
| `IVisualizationObjectAbsoluteDateFilter` | `IAbsoluteDateFilter` |
| `IVisualizationObjectPositiveAttributeFilter` | `IPositiveAttributeFilter` |
| `IVisualizationObjectNegativeAttributeFilter` | `INegativeAttributeFilter` |
| `IVisualizationTotal` | `ITotal` |
| `isVisualizationAttribute` | `isAttribute` |
| `VisualizationClass` | `GdcVisualizationClass` |
| `DashboardExport` | `GdcDashboardExport` |

The following types exported from `DashboardLayout` are now located under the `GdcDashboardLayout` namespace:

-  `Layout`
-  `Widget`
-  `LayoutContent`
-  `IPersistedWidget`
-  `IFluidLayout`
-  `IFluidLayoutRow`
-  `IFluidLayoutColumn`
-  `IFluidLayoutColSize`
-  `IFluidLayoutSize`
-  `SectionHeader`
-  `ISectionHeader`
-  `ISectionDescription`
