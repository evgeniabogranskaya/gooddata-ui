---
title: Connecting to an Analytical Backend
sidebar_label: Connecting to an Analytical Backend 
copyright: (C) 2007-2018 GoodData Corporation
id: analytical_backend_integration
---

GoodData.UI visual components render data stored in a workspace that exists on an Analytical Backend. 

Your application must initialize the integration with the Analytical Backend, select a workspace to work with and then 
use these in the React components.

All components accept the `backend` and `workspace` properties. You can use these explicitly or use the `BackendProvider` and `WorkspaceProvider` React providers.

## Initial setup

The following procedure implements the initial setup using the providers to pass down the configured backend and workspace:

1.  Create and set up an instance of the analytical backend to communicate with the GoodData platform:

    ```javascript
    import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
    
    const backend = bearFactory({ hostname })
                    .withAuthentication(new ContextDeferredAuthProvider());
    ```

    This creates a new instance of the analytical backend to communicate with the GoodData platform (codename `bear`) on 
    the provided hostname. You may omit the hostname when connecting to the same origin that serves the application. 

    The authentication is deferred fully to your application. The analytical backend expects your application code to take 
    care of authentication on the GoodData platform (login/SSO). If the analytical backend finds that the context is not 
    authenticated, the GoodDataSdkError with the code UNAUTHORIZED will be sent through the onError callback.

2.  Wrap your React application with BackendProvider and WorkspaceProvider:

    ```jsx
    <BackendProvider backend={backend}>
        <WorkspaceProvider workspace='your-project-id'>
            <YourApplicationTree />
        </WorkspaceProvider>
    </BackendProvider>
    ```

## Authentication options

TODO: more to come here