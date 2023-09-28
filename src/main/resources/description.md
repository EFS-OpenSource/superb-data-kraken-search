This service provides endpoints to enable searching in elasticsearch.

- ```/search/v1.0/index``` Gets available indexes (filtered by given pattern).
- ```/search/v1.0/criteria``` Gets possible filter-criteria within the given index (properties enabled in elasticsearch-mapping), including datatype and
  possible operators.
- ```/search/v1.0/resultproperties``` Gets possible result-properties.
- ```/search/v1.0``` Execute search.

---
**NOTE on Operators**

The following operators are supported:

- ```EQ``` look up for '&lt;value&gt;'
- ```LIKE``` look up for '\*&lt;value&gt;\*'
- ```NOT``` look up for values not matching '&lt;value&gt;'
- ```GT``` look up for values greater than '&lt;value&gt;'
- ```GTE``` look up for values greater than or equal to '&lt;value&gt;'
- ```LT``` look up for values less than '&lt;value&gt;'
- ```LTE``` look up for values less than or equal to '&lt;value&gt;'
- ```BETWEEN``` look up for values between '&lt;lowerBound&gt;' and '&lt;upperBound&gt;' (where the bounds are included)

If multiple Filters are provided, same-property-filters are combined by 'OR', varying properties are combined by 'AND'. Example:

```
  "filter": [
    {
      "operator": "EQ",
      "property": "metadata.project.projectId",
      "value": "sdk"
    },
    {
      "operator": "EQ",
      "property": "metadata.project.projectId",
      "value": "sdk2"
    },
    {
      "operator": "EQ",
      "property": "metadata.customer.customerId",
      "value": "efs"
    }
  ],
```

will be interpreted as

```
"metadata.customer.customerId:efs AND metadata.project.projectId:( sdk OR sdk2 )"
```