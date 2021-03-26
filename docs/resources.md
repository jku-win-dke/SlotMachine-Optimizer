<h1 id="api-documentation">Api Documentation v0.0.1</h1>

> Scroll down for code samples, example requests and responses. Select a language for code samples from the tabs above or the mobile navigation menu.

Api Documentation

<h1 id="api-documentation-optimization-resource">optimization-resource</h1>

Optimization Resource

## createAndInitializeOptimizationUsingPOST

<a id="opIdcreateAndInitializeOptimizationUsingPOST"></a>

`POST /optimizations`

*Create and initialize a heuristic optimization with flights and preferences.*

> Body parameter

```json
{
  "type": "object",
  "properties": {
    "flights": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "flightId": {
            "type": "string"
          },
          "scheduledTime": {
            "type": "string",
            "format": "date-time"
          },
          "weightMap": {
            "type": "array",
            "items": {
              "type": "integer",
              "format": "int32"
            }
          }
        }
      }
    },
    "initialFlightSequence": {
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "optId": {
      "type": "string"
    },
    "slots": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "time": {
            "type": "string",
            "format": "date-time"
          }
        }
      }
    }
  }
}
```

<h3 id="createandinitializeoptimizationusingpost-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|body|body|[OptimizationDTO](#schemaoptimizationdto)|true|optimization|

> Example responses

> 200 Response

<h3 id="createandinitializeoptimizationusingpost-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[OptimizationDTO](#schemaoptimizationdto)|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|[OptimizationDTO](#schemaoptimizationdto)|
|400|[Bad Request](https://tools.ietf.org/html/rfc7231#section-6.5.1)|Bad Request|None|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found|None|

<aside class="success">
This operation does not require authentication
</aside>

## getOptimizationUsingGET

<a id="opIdgetOptimizationUsingGET"></a>

`GET /optimizations/{optId}`

*Get the description of a specific optimization.*

<h3 id="getoptimizationusingget-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|optId|path|string|true|optId|

> Example responses

> 200 Response

```json
{
  "type": "object",
  "properties": {
    "duration": {
      "type": "object",
      "properties": {
        "nano": {
          "type": "integer",
          "format": "int32"
        },
        "negative": {
          "type": "boolean"
        },
        "seconds": {
          "type": "integer",
          "format": "int64"
        },
        "units": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "dateBased": {
                "type": "boolean"
              },
              "duration": "[Circular]",
              "durationEstimated": {
                "type": "boolean"
              },
              "timeBased": {
                "type": "boolean"
              }
            }
          }
        },
        "zero": {
          "type": "boolean"
        }
      }
    },
    "initialFitness": {
      "type": "number",
      "format": "double"
    },
    "iterations": {
      "type": "integer",
      "format": "int32"
    },
    "optId": {
      "type": "string"
    },
    "resultFitness": {
      "type": "number",
      "format": "double"
    },
    "status": {
      "type": "string",
      "enum": [
        "CREATED",
        "IN_PROGRESS",
        "FINISHED",
        "ABORTED"
      ]
    },
    "timeAborted": {
      "type": "string",
      "format": "date-time"
    },
    "timeCreated": {
      "type": "string",
      "format": "date-time"
    },
    "timeFinished": {
      "type": "string",
      "format": "date-time"
    },
    "timeStarted": {
      "type": "string",
      "format": "date-time"
    },
    "validTime": {
      "type": "string",
      "format": "date-time"
    }
  }
}
```

<h3 id="getoptimizationusingget-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[OptimizationStatisticsDTO](#schemaoptimizationstatisticsdto)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found|None|

<aside class="success">
This operation does not require authentication
</aside>

## abortOptimizationUsingPUT

<a id="opIdabortOptimizationUsingPUT"></a>

`PUT /optimizations/{optId}/abort`

*Abort a previously started optimization.*

<h3 id="abortoptimizationusingput-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|optId|path|string|true|optId|

<h3 id="abortoptimizationusingput-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|None|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|None|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found|None|
|409|[Conflict](https://tools.ietf.org/html/rfc7231#section-6.5.8)|Conflict|None|

<aside class="success">
This operation does not require authentication
</aside>

## getOptimizationResultUsingGET

<a id="opIdgetOptimizationResultUsingGET"></a>

`GET /optimizations/{optId}/result`

*Get the result of an optimization; returns intermediate result if not finished.*

<h3 id="getoptimizationresultusingget-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|optId|path|string|true|optId|

> Example responses

> 200 Response

```json
{
  "type": "object",
  "properties": {
    "flightSequence": {
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "optId": {
      "type": "string"
    }
  }
}
```

<h3 id="getoptimizationresultusingget-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[OptimizationResultDTO](#schemaoptimizationresultdto)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found|None|

<aside class="success">
This operation does not require authentication
</aside>

## startOptimizationUsingPUT

<a id="opIdstartOptimizationUsingPUT"></a>

`PUT /optimizations/{optId}/start`

*Start a specific optimization that was previously created and initialized.*

<h3 id="startoptimizationusingput-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|optId|path|string|true|optId|

<h3 id="startoptimizationusingput-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|None|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|None|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found|None|
|409|[Conflict](https://tools.ietf.org/html/rfc7231#section-6.5.8)|Conflict|None|

<aside class="success">
This operation does not require authentication
</aside>

## getOptimizationStatisticsUsingGET

<a id="opIdgetOptimizationStatisticsUsingGET"></a>

`GET /optimizations/{optId}/stats`

*Get current statistics for a specific optimization and its result.*

<h3 id="getoptimizationstatisticsusingget-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|optId|path|string|true|optId|

> Example responses

> 200 Response

```json
{
  "type": "object",
  "properties": {
    "duration": {
      "type": "object",
      "properties": {
        "nano": {
          "type": "integer",
          "format": "int32"
        },
        "negative": {
          "type": "boolean"
        },
        "seconds": {
          "type": "integer",
          "format": "int64"
        },
        "units": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "dateBased": {
                "type": "boolean"
              },
              "duration": "[Circular]",
              "durationEstimated": {
                "type": "boolean"
              },
              "timeBased": {
                "type": "boolean"
              }
            }
          }
        },
        "zero": {
          "type": "boolean"
        }
      }
    },
    "initialFitness": {
      "type": "number",
      "format": "double"
    },
    "iterations": {
      "type": "integer",
      "format": "int32"
    },
    "optId": {
      "type": "string"
    },
    "resultFitness": {
      "type": "number",
      "format": "double"
    },
    "status": {
      "type": "string",
      "enum": [
        "CREATED",
        "IN_PROGRESS",
        "FINISHED",
        "ABORTED"
      ]
    },
    "timeAborted": {
      "type": "string",
      "format": "date-time"
    },
    "timeCreated": {
      "type": "string",
      "format": "date-time"
    },
    "timeFinished": {
      "type": "string",
      "format": "date-time"
    },
    "timeStarted": {
      "type": "string",
      "format": "date-time"
    },
    "validTime": {
      "type": "string",
      "format": "date-time"
    }
  }
}
```

<h3 id="getoptimizationstatisticsusingget-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|OK|[OptimizationStatisticsDTO](#schemaoptimizationstatisticsdto)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Unauthorized|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not Found|None|

<aside class="success">
This operation does not require authentication
</aside>

# Schemas

<h2 id="tocS_SlotDTO">SlotDTO</h2>
<!-- backwards compatibility -->
<a id="schemaslotdto"></a>
<a id="schema_SlotDTO"></a>
<a id="tocSslotdto"></a>
<a id="tocsslotdto"></a>

```json
{
  "type": "object",
  "properties": {
    "time": {
      "type": "string",
      "format": "date-time"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|time|string(date-time)|false|none|none|

<h2 id="tocS_OptimizationDTO">OptimizationDTO</h2>
<!-- backwards compatibility -->
<a id="schemaoptimizationdto"></a>
<a id="schema_OptimizationDTO"></a>
<a id="tocSoptimizationdto"></a>
<a id="tocsoptimizationdto"></a>

```json
{
  "type": "object",
  "properties": {
    "flights": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "flightId": {
            "type": "string"
          },
          "scheduledTime": {
            "type": "string",
            "format": "date-time"
          },
          "weightMap": {
            "type": "array",
            "items": {
              "type": "integer",
              "format": "int32"
            }
          }
        }
      }
    },
    "initialFlightSequence": {
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "optId": {
      "type": "string"
    },
    "slots": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "time": {
            "type": "string",
            "format": "date-time"
          }
        }
      }
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|flights|[[FlightDTO](#schemaflightdto)]|false|none|none|
|initialFlightSequence|[string]|false|none|none|
|optId|string|false|none|none|
|slots|[[SlotDTO](#schemaslotdto)]|false|none|none|

<h2 id="tocS_OptimizationStatisticsDTO">OptimizationStatisticsDTO</h2>
<!-- backwards compatibility -->
<a id="schemaoptimizationstatisticsdto"></a>
<a id="schema_OptimizationStatisticsDTO"></a>
<a id="tocSoptimizationstatisticsdto"></a>
<a id="tocsoptimizationstatisticsdto"></a>

```json
{
  "type": "object",
  "properties": {
    "duration": {
      "type": "object",
      "properties": {
        "nano": {
          "type": "integer",
          "format": "int32"
        },
        "negative": {
          "type": "boolean"
        },
        "seconds": {
          "type": "integer",
          "format": "int64"
        },
        "units": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "dateBased": {
                "type": "boolean"
              },
              "duration": "[Circular]",
              "durationEstimated": {
                "type": "boolean"
              },
              "timeBased": {
                "type": "boolean"
              }
            }
          }
        },
        "zero": {
          "type": "boolean"
        }
      }
    },
    "initialFitness": {
      "type": "number",
      "format": "double"
    },
    "iterations": {
      "type": "integer",
      "format": "int32"
    },
    "optId": {
      "type": "string"
    },
    "resultFitness": {
      "type": "number",
      "format": "double"
    },
    "status": {
      "type": "string",
      "enum": [
        "CREATED",
        "IN_PROGRESS",
        "FINISHED",
        "ABORTED"
      ]
    },
    "timeAborted": {
      "type": "string",
      "format": "date-time"
    },
    "timeCreated": {
      "type": "string",
      "format": "date-time"
    },
    "timeFinished": {
      "type": "string",
      "format": "date-time"
    },
    "timeStarted": {
      "type": "string",
      "format": "date-time"
    },
    "validTime": {
      "type": "string",
      "format": "date-time"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|duration|[Duration](#schemaduration)|false|none|none|
|initialFitness|number(double)|false|none|none|
|iterations|integer(int32)|false|none|none|
|optId|string|false|none|none|
|resultFitness|number(double)|false|none|none|
|status|string|false|none|none|
|timeAborted|string(date-time)|false|none|none|
|timeCreated|string(date-time)|false|none|none|
|timeFinished|string(date-time)|false|none|none|
|timeStarted|string(date-time)|false|none|none|
|validTime|string(date-time)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|status|CREATED|
|status|IN_PROGRESS|
|status|FINISHED|
|status|ABORTED|

<h2 id="tocS_TemporalUnit">TemporalUnit</h2>
<!-- backwards compatibility -->
<a id="schematemporalunit"></a>
<a id="schema_TemporalUnit"></a>
<a id="tocStemporalunit"></a>
<a id="tocstemporalunit"></a>

```json
{
  "type": "object",
  "properties": {
    "dateBased": {
      "type": "boolean"
    },
    "duration": {
      "type": "object",
      "properties": {
        "nano": {
          "type": "integer",
          "format": "int32"
        },
        "negative": {
          "type": "boolean"
        },
        "seconds": {
          "type": "integer",
          "format": "int64"
        },
        "units": {
          "type": "array",
          "items": "[Circular]"
        },
        "zero": {
          "type": "boolean"
        }
      }
    },
    "durationEstimated": {
      "type": "boolean"
    },
    "timeBased": {
      "type": "boolean"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|dateBased|boolean|false|none|none|
|duration|[Duration](#schemaduration)|false|none|none|
|durationEstimated|boolean|false|none|none|
|timeBased|boolean|false|none|none|

<h2 id="tocS_FlightDTO">FlightDTO</h2>
<!-- backwards compatibility -->
<a id="schemaflightdto"></a>
<a id="schema_FlightDTO"></a>
<a id="tocSflightdto"></a>
<a id="tocsflightdto"></a>

```json
{
  "type": "object",
  "properties": {
    "flightId": {
      "type": "string"
    },
    "scheduledTime": {
      "type": "string",
      "format": "date-time"
    },
    "weightMap": {
      "type": "array",
      "items": {
        "type": "integer",
        "format": "int32"
      }
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|flightId|string|false|none|none|
|scheduledTime|string(date-time)|false|none|none|
|weightMap|[integer]|false|none|none|

<h2 id="tocS_Duration">Duration</h2>
<!-- backwards compatibility -->
<a id="schemaduration"></a>
<a id="schema_Duration"></a>
<a id="tocSduration"></a>
<a id="tocsduration"></a>

```json
{
  "type": "object",
  "properties": {
    "nano": {
      "type": "integer",
      "format": "int32"
    },
    "negative": {
      "type": "boolean"
    },
    "seconds": {
      "type": "integer",
      "format": "int64"
    },
    "units": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "dateBased": {
            "type": "boolean"
          },
          "duration": "[Circular]",
          "durationEstimated": {
            "type": "boolean"
          },
          "timeBased": {
            "type": "boolean"
          }
        }
      }
    },
    "zero": {
      "type": "boolean"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|nano|integer(int32)|false|none|none|
|negative|boolean|false|none|none|
|seconds|integer(int64)|false|none|none|
|units|[[TemporalUnit](#schematemporalunit)]|false|none|none|
|zero|boolean|false|none|none|

<h2 id="tocS_OptimizationResultDTO">OptimizationResultDTO</h2>
<!-- backwards compatibility -->
<a id="schemaoptimizationresultdto"></a>
<a id="schema_OptimizationResultDTO"></a>
<a id="tocSoptimizationresultdto"></a>
<a id="tocsoptimizationresultdto"></a>

```json
{
  "type": "object",
  "properties": {
    "flightSequence": {
      "type": "array",
      "items": {
        "type": "string"
      }
    },
    "optId": {
      "type": "string"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|flightSequence|[string]|false|none|none|
|optId|string|false|none|none|

