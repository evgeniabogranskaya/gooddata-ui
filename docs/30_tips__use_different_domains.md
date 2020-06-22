---
title: Access Multiple Domains and Projects
sidebar_label: Access Multiple Domains and Projects
copyright: (C) 2007-2018 GoodData Corporation
id: ht_render_visualization_from_different_domain
---

Sometimes, you need to render visualizations from different projects and different domains on one page. For example, you might be creating a dashboard with a sales overview for different products, or you want to use a product from a different domain to benchmark products in your current domain.

Every SDK instance is connected to a specific domain. To use multiple domains in the same application, create multiple SDK instances and pass them to your components. Every component imported from `@gooddata/react-components` contains an optional `sdk` property, by which you can specify the component that will fetch data.

## Example

```jsx
import React, { PureComponent } from 'react';
import bearFactory from '@gooddata/sdk-backend-bear';
import { InsightView } from '@gooddata/sdk-ui-ext';
 
 
export default class SampleVisualizations extends PureComponent {
    constructor(props) {
        super(props);
         
        const domain1 = 'my-custom-domain.com';
        this.backend1 = bearFactory({ hostname: domain1 });
     
        const domain2 = 'another-custom-domain.com';
        this.backend2 = bearFactory({ hostname: domain2 });
    }
 
    render() {
        return (
            <div>
                <div style={{ height: 400, width: 600 }}>
                    <InsightView
                        backend={this.backend1}
                        workspace="<projectId-from-backend1>"
                        identifier="<identifier-from-backend1>"
                    />
                </div>
                <div style={{ height: 400, width: 600 }}>
                    <InsightView
                        backend={this.backend2}
                        workspace="<projectId-from-backend2>"
                        identifier="<identifier-from-backend2>"
                    />
                </div>
            </div>
        );
    }
}
```
