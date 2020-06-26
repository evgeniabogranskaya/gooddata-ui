---
id: api_maturity
title: API Maturity
sidebar_label: API Maturity
copyright: (C) 2007-2018 GoodData Corporation
---

All APIs exported by the GoodData.UI packages come with documentation that also includes API Maturity annotations. These
annotations are important to follow: they indicate to you, the consumer, what the stability guarantees of the different
APIs are.

The API Maturity annotations are as follows:

-  **@alpha**: initial API; highly likely to change outside of the SemVer specification 
-  **@beta**: mostly stable API; details may change outside of the SemVer specification 
-  **@public**: stable API; follows the SemVer specification 
-  **@internal**: internal API; may change or disappear at any time 

All this documentation is included in the published packages so that you can conveniently access it in an IDE of your choice.

## Recommendation

Use only the exported APIs annotated as **@public**. Doing this guarantees that upgrades to latest minor
or patch versions work seamlessly.

On top of this, we strongly recommend that you use the same version of all GoodData.UI packages.