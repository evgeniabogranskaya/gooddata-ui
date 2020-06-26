---
title: InsightView
sidebar_label: InsightView
copyright: (C) 2007-2018 GoodData Corporation
id: visualization_component
---

The **InsightView component** is a generic component that renders insights created and saved by Analytical Designer.

## Structure

```jsx
import '@gooddata/sdk-ui-ext/styles/css/main.css';
import { InsightView } from '@gooddata/sdk-ui-ext';

<div style={{ height: 400, width: 600 }}>
    <InsightView
        identifier="<visualization-identifier>"
        config={<chart-config>}
    />
</div>
```

```jsx
import '@gooddata/sdk-ui-ext/styles/css/main.css';
import { InsightView } from '@gooddata/sdk-ui-ext';

<div style={{ height: 400, width: 600 }}>
    <InsightView
        uri="<visualization-uri>"
        config={<chart-config>}
        sdk={<sdk>}
    />
</div>
```
**NOTE**: A set of options in `config` is different for each type of visualization. The type expected on InsightView is the same as the type of the configuration expected on the respective React component.

## Example

```jsx
import '@gooddata/sdk-ui-ext/styles/css/main.css';
import { InsightView } from '@gooddata/sdk-ui-ext';

<div style={{ height: 400, width: 600 }}>
    <InsightView
        identifier="aoJqpe5Ib4mO"
        config={{
            colors: ['rgb(195, 49, 73)', 'rgb(168, 194, 86)'],
            legend: {
                enabled: true,
                position: 'bottom'
            }
        }}
    />
</div>
```

## Filters

For more information, see [Filter Visual Components](30_tips__filter_visual_components.md).

## Caching - TODO adapt

To properly render the referenced table or chart, the InsightView component needs additional information from the GoodData platform. This information is usually static. To minimize the number of redundant requests and reduce the rendering time, some static information (such as the list of visualization classes, the color palette, or feature flags for each project) is cached for all InsightView components in the same application.

The amount of the cached information does not impact performance in any way. However, you can manually clear the cache whenever needed (for example, after logging out, when switching projects or leaving a page with visualizations using the GoodData.UI components).

```javascript
import { clearSdkCache } from '@gooddata/react-components/dist/helpers/sdkCache';
...
clearSdkCache();
...
```

## Properties

| Name | Required? | Type | Description |
| :--- | :--- | :--- | :--- |
| identifier | false | string | The identifier of the visualization to be rendered. Can be omitted if the visualization URI is present. |
| locale | false | string | The localization of the visualization. Defaults to `en-US`. For other languages, see the [full list of available localizations](https://github.com/gooddata/gooddata-ui-sdk/blob/master/libs/sdk-ui/src/base/localization/Locale.ts). |
| config  | false | [IChartConfig](15_props__chart_config.md) | The chart configuration object |
| filters | false | [IFilter[]](30_tips__filter_visual_components.md) | An array of filter definitions |
| drillableItems | false | [IDrillableItem[]](15_props__drillable_item.md) | An array of points and attribute values to be drillable.|
| onDrill | false | [IDrillEventCallback](15_props__on_drill.md) | The drilling event catcher. Called when drilling happens. |
| onError | false | function | A custom error handler. Called with the argument containing the state and original error message, for example, `{ status:ErrorStates.BAD_REQUEST,error: {...} }`. See the [full list of error states](https://github.com/gooddata/gooddata-ui-sdk/blob/master/libs/sdk-ui/src/base/errors/GoodDataSdkError.ts). Defaults to `console.error`.|
| onExportReady | false | Function | A callback when the component is ready for exporting its data |
| onLoadingChanged | false | function | A custom loading handler. Called when a visualization changes to/from the loading state. Called with the argument denoting a valid state, for example, `{ isLoading:false}`. |
