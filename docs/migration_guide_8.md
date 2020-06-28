---
title: Migration from Version 7.x
sidebar_label: Migration from Version 7.x
copyright: (C) 2007-2019 GoodData Corporation
id: migration_guide_8
---

GoodData.UI Version 8.0 comes with a number of breaking changes. These changes allow us to better control the growth 
of GoodData.UI. While the list of breaking changes is long and may look daunting at first, the vast majority of the 
changes are of the structural nature.

Here are the steps that will let you smoothly upgrade your GoodData.UI to Version 8.0:

## Step 1. Change dependencies and imports.

Update your `package.json` to use the Version 8.0 packages. 

-  If you use `gooddata-js` directly in your application, make this change in your `package.json`:

   `"@gooddata/gooddata-js"` -> `"@gooddata/api-client-bear": "^8.0.0"`

-  If you use `typings` directly in your application, make this change in your `package.json` file:

   `"@gooddata/typings"` -> `"@gooddata/api-model-bear": "^8.0.0"`

-  If you use `react-components` directly in your application, make this change in your `package.json` file:

   `"@gooddata/react-components"` -> `"@gooddata/sdk-ui-all": "^8.0.0"`

## Step 2. Fix imports.

-  If you use `gooddata-js` directly in your application, find all instances of `"@gooddata/gooddata-js"` in your codebase 
   and replace them with `"@gooddata/api-client-bear"`. 

-  If you use `typings` directly in your application, find all instances of `"@gooddata/typings"` in your codebase and replace 
   them with `"@gooddata/api-model-bear"`.

-  If you use `react-components` directly in your application, find all instances of `"@gooddata/react-components"` in your 
   codebase and replace them with `"@gooddata/sdk-ui-all"`.

**NOTE**: We have moved some code between `api-client` and `api-model`. If you run into unresolved imports from `api-client`, 
you will find them in `api-model`.

## Step 3. Fix style imports.

Instead of importing stylesheets from the react-components package, switch to using the following:

* `@gooddata/sdk-ui-charts/styles/main.css`
* `@gooddata/sdk-ui-pivot/styles/main.css`
* `@gooddata/sdk-ui-filters/styles/main.css`
* `@gooddata/sdk-ui-ext/styles/main.css`

Apart from their location, the styles remain the same.

## Step 4. Initialize the analytical backend.

All GoodData.UI components now accept an instance of `IAnalyticalBackend` instead of directly using the `api-client` 
package (formerly known as `gooddata-js`).

If your application already uses `gooddata-js` to initialize integration with the GoodData platform, here is how you 
set up the analytical backend:

1.  Add an implementation of the analytical backend:

    `"@gooddata/sdk-backend-bear": "^8.0.0"`

    Keep the `api-client` initialization logic as-is, including all the code that you have for authentication.

2.  Create and set up an instance of the analytical backend to communicate with the GoodData platform:

    ```javascript
    import bearFactory, { ContextDeferredAuthProvider } 
    from "@gooddata/sdk-backend-bear";
    
    const backend = bearFactory({ hostname })
    .withAuthentication(new ContextDeferredAuthProvider());
    ```

    This creates a new instance of the analytical backend to communicate with the GoodData platform (codename “bear”) on the provided hostname. 

    The hostname semantics are the same as with the `api-client` package. You may omit the hostname the same way as with `api-client`. 

    The authentication is deferred fully to your application. The analytical backend expects your application code to take care of authentication on the GoodData platform (login/SSO). If the analytical backend finds that the context is not authenticated, the GoodDataSdkError with code UNAUTHORIZED will be sent through the onError callback.

3.  Wrap your React application with BackendProvider:

	```jsx
    <BackendProvider backend={backend}>
		<YourApplicationTree />
	</BackendProvider>
    ```

    If you were explicitly passing the `sdk` prop to the React components, remove the prop.

## Step 5. Adopt the breaking API changes.

If you are using TypeScript, you can rely on the compiler to report breaking changes. Here is a quick summary of breaking changes:

-  Components no longer have the `projectId` prop. It has been renamed to `workspace`.

    You can either manually rename all instances of the `projectId` prop to `workspace` or wrap your component tree using WorkspaceProvider:
    
    ```jsx
    <WorkspaceProvider workspace={“yourWorkspace”}>
        <YourApplication />
    </WorkspaceProvider>
    ```

-   The drilling API has changed. The onFiredDrillEvent has been renamed to "onDrill", and the payload of the drill events 
    has changed in a breaking way. The intersections now contain more detailed information, and the execution context has been 
    replaced with `dataView`.

    To adopt these changes, rename the prop and then update your drilling code to work with the events and the data view. In both cases, the events emitted by Version 8.0 events are a superset of the previous version events. All the information you relied on before is present, but is stored in different structures.

## Step 6. Adopt the breaking Model changes.

### Review the inputs created without the Model infrastructure.
    
If you created the objects manually, pay attention to the following breaking changes:
 
-  Previously, attribute objects had the `visualizationAttribute` root key. This root key has been renamed to just `attribute`.
-  Previously, positive and negative attribute filters could take the `“textFilter: true”` switch to indicate that contents of the `in` or `notIn` fields are text values of the attributes and not their URIs. 

   This has been changed so that the `in` and `notIn` properties now take one of the following objects:

   `{ uris: [...] }`
    or 
   `{ values: [...] }`

### Review the inputs created with the Model infrastructure.

The Model infrastructure was rebuilt. The API has changed significantly, and the Model namespace no longer exists. 
A superset of the Model functionality is now available through functions exported from `@gooddata/sdk-model`, which 
is included in `sdk-ui-all`.

For each type of an object, `sdk-model` contains a factory function that takes all the essential parameters.

| Before | After |
| :--- | :--- |
|`Model.measure`| `newMeasure` |
|`Model.arithmeticMeasure` | `newArithmeticMeasure` |
|`Model.popMeasure` | `newPopMeasure` |
|`Model.previousPeriodMeasure` | `newPreviousPeriodMeasure` |
|`Model.attribute` | `newAttribute` |
|`Model.positiveAttributeFilter` | `newPositiveAttributeFilter` |
|`Model.negativeAttributeFilter` | `newNegativeAttributeFilter` |
|`Model.absoluteDateFilter` | `newAbsoluteDateFilter` |
|`Model.relativeDateFilter` | `newRelativeDateFilter` |
|`Model.measureValueFilter` | `newMeasureValueFilter` |
|`Model.attributeSortItem` | `newAttributeSortItem` |
|`Model.measureSortItem` | `newMeasureSortItem` |

In contrast to the Model infrastructure, the factory functions now return the actual objects instead of the builder 
objects. Where applicable (for measures and attributes), the factory functions take a `modifications` function as the 
last and optional parameter. The modification function will be called with a single parameter: an instance of the 
builder that you can use to fine-tune the object before it is returned.

Therefore, instead of this: 

```javascript
Model.measure("id").alias("My Measure")
```

You now have to do this:

```javascript
newMeasure("id", (m) => m.alias("My Measure"))
```

**NOTE:** We have added layers of convenience to the `sdk-model` functions. One common convention is that if an object 
references another object by its local identifier (such as arithmetic measure operands), the factory function accepts 
either the **local identifier** or the **object itself**. In the latter case, it will extract the local identifier for you.

## Step 7. Adopt the changes in the Visualization component and the Execute component.

These two components significantly changed in both the API and implementation. Apart from changing the call sites, 
we recommend that you focus on testing the parts of your applications that use these components.

### Visualization Component

If you are using the Visualization component to embed insights created by Analytical Designer, make changes to use the 
InsightView component instead. 

The API of the new InsightView component has the same props as the old Visualization component. The only change is the name.

The implementation of the InsightView component is completely new. It now uses exactly the same code that 
Analytical Designer and KPI Dashboards use. The new implementation has been thoroughly tested and verified. 
It may, however, result in subtle changes in your application.

### Execute Component

The Execute component no longer allows you to specify AFM via the public interface. Instead, it offers abstraction of 
data series and data slices that can be optionally filtered and/or sorted. The component constructs AFM internally in 
the background using the following conventions:

-  If only `seriesBy` is provided, the `resultSpec` will contain a single dimension with attributes and measures in this dimension.
-  If both `seriesBy` and `slicesBy` are provided, the `resultSpec` will contain the `slicesBy` attributes in the first dimension, 
   and then the `seriesBy` attributes and measures in the second dimension.

If you can map your existing AFM and resultSpec to these conventions, then you have to only rearrange the props. 
The raw data that the Execute component was exposing is still available.

Otherwise, you have to either make larger modifications to how you read the data, or switch to using the RawExecute 
component where you can replicate the exact AFM execution and get the raw data in the exact format.

We strongly recommend sticking to the Execute component and migrating from using the raw data. Instead of raw 
data access, use the `dataSeries` and `dataSlices` data accessors that are available on DataViewFacade. 
These accessors greatly simplify working with the data.
