---
id: installation
title: Installation
sidebar_label: Installation
copyright: (C) 2007-2018 GoodData Corporation
---

This article describes how to install GoodData.UI  and a few steps to accomplish before you can start  creating your visualizations.

**NOTE:** Before you install GoodData.UI, make sure that you have a GoodData account (see [About GoodData.UI](01_intro__about_gooddataui.md#supported-technologies)).

## Installation

Run the following to install packages necessary to render charts:

```bash
yarn add @gooddata/sdk-backend-bear @gooddata/sdk-model @gooddata/sdk-ui @gooddata/sdk-ui-charts @gooddata/sdk-ui-pivot @gooddata/sdk-ui-geo @gooddata/sdk-ui-ext
```

```bash
$ npm install @gooddata/sdk-backend-bear @gooddata/sdk-model @gooddata/sdk-ui @gooddata/sdk-ui-charts @gooddata/sdk-ui-pivot @gooddata/sdk-ui-geo @gooddata/sdk-ui-ext
```

**NOTE:** Check out the [Architecture Overview](01_intro__framework_overview.md) to learn about additional packages which
you can include and render for instance Pivot Tables or Geo Charts.

### Important Notice

For smooth developer experience, please make sure that your application always uses same versions of all GoodData.UI packages. Do not
mix and match different versions. Always upgrade to same versions of all `@gooddata` packages.


## Before you start coding

1. Get your **project ID**. The project ID is a unique 32-character identifier of your project within the GoodData platform. You need your project ID to connect your project with the visualizations that you are going to create. To get your project ID, see [Find the Project ID](https://help.gooddata.com/display/doc/Find+the+Project+ID).
2. Obtain **object identifiers** using [gdc-catalog-export](02_start__catalog_export.md). The object identifier is a reference to a project object. You need the identifiers to display attributes, measures, visualizations, filters, and other objects.
    
    The identifier of an object is consistent across your domain regardless of the GoodData project it lives in. The object identifier is a text string. Example: `franchiseFeesAdRoyaltyIdentifier`
3. Pick a **visualization type** from the prepared React components (table, column chart, bar chart, and so on) and, optionally, one or more properties to use with the selected visualization. Alternatively, you can fetch raw data and create a fully custom visualization.
