---
id: create_custom_attribute_filter
title: Create a Custom Attribute Filter
sidebar_label: Create Custom Filter
copyright: (C) 2007-2020 GoodData Corporation
---

Attribute elements component is a low level counterpart to the [Attribute Filter component](10_vis__attribute_filter_component.md).

It lists attribute values and allows you to completely customize how they are rendered.

The child function receives the following arguments:

* an array of attribute values
* a callback to load more values
* loading status
* an optional error object

### Example

For latest up-to-date examples see the [live examples](https://github.com/gooddata/gooddata-ui-sdk/blob/master/examples/sdk-examples/src/examples/attributeFilter/AttributeElementsExample.tsx) in GoodData.UI repository.
