---
id: attribute_filter_component
title: Attribute Filter
sidebar_label: Attribute Filter
copyright: (C) 2007-2018 GoodData Corporation
---

Attribute Filter component is a dropdown component that lists attribute values. You pass a callback function, which receives a list of the selected values when a user clicks **Apply**.

Optionally, you can define what attribute values should be selected in the filter by default.

![Attribute Filter Component](assets/attribute_filter.png "Attribute Filter Component")

### Example

In the following example, attribute values are listed and the ```onApply``` callback function is triggered when a user clicks **Apply** to confirm the selection.
The onApply callback receives new filter definition which you can use to filter charts.

```jsx
import React, { Component } from 'react';
import { AttributeFilter, Model } from '@gooddata/sdk-ui-filters';
import { newNegativeAttributeFilter } from '@gooddata/sdk-model';

import '@gooddata/sdk-ui-filters/styles/css/main.css';

import { Ldm } from "./ldm";

export class AttributeFilterExample extends Component {
    onApply(filter) {
        // eslint-disable-next-line no-console
        console.log('AttributeFilterExample onApply', filter);
    }

    render() {
        return (
            <div>
                <AttributeFilter
                    filter={newNegativeAttributeFilter(Ldm.EmployeeName.Default, [])}
                    onApply={this.onApply}
                />
            </div>
        );
    }
}
```
## Define the default selection of values in the filter

To define the attribute values that should be selected in the filter by default, include those attribute values in the ```filter``` property. For more details about filtering, see [Filter Visual Components](30_tips__filter_visual_components.md).

```jsx
    render() {
        return (
            <div>
                <AttributeFilter
                    filter={newPositiveAttributeFilter(Ldm.EmployeeName.Default, ["Abbie Adams"])}
                    onApply={this.onApply}
                />
            </div>
        );
    }
```


## Properties

| Name | Required? | Type | Description |
| :--- | :--- | :--- | :--- |
| onApply | false | Function | A callback when the selection is confirmed by a user |
| onApplyWithFilterDefinition | false | Function | A callback when the selection is confirmed by a user. The selection of attribute values is received already transformed into an attribute filter definition, which you can then send directly to a chart. |
| filter | false | [Filter](30_tips__filter_visual_components.md) | The attribute filter definition |
| locale | false | string | The localization of the component. Defaults to `en-US`. For other languages, see the [full list of available localizations](https://github.com/gooddata/gooddata-ui-sdk/blob/master/libs/sdk-ui/src/base/localization/Locale.ts). |
| fullscreenOnMobile | false | boolean | If `true`, adjusts the filter to be properly rendered on a mobile device |
| title | false | string | A custom label to show on the dropdown icon |
| FilterLoading | false | Component | A component to be rendered if attribute elements are loading |
| FilterError | false | Component | A component to be rendered if attribute elements loading fails |

**NOTE:** The ```uri``` property (the URI of the attribute displayForm used in the filter) and the ```identifier``` property (the identifier of the attribute displayForm used in the filter) are **deprecated**. Do not use them.
To define an attribute, use the ```filter``` property.
