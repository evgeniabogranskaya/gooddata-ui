---
id: create_custom_attribute_filter
title: Create a Custom Attribute Filter
sidebar_label: Create a Custom Attribute Filter
copyright: (C) 2007-2020 GoodData Corporation
---

The **Attribute Elements component** is a low-level counterpart to the [Attribute Filter component](10_vis__attribute_filter_component.md).

The Attribute Elements component lists attribute values and allows you to completely customize how they are rendered.

The child function receives the following arguments:

* An array of attribute values
* A callback to load more values
* Loading status
* An optional error object

### Example

For the latest up-to-date examples, see the [live examples](https://github.com/gooddata/gooddata-ui-sdk/blob/master/examples/sdk-examples/src/examples/attributeFilter/AttributeElementsExample.tsx) in the GoodData.UI repository.
