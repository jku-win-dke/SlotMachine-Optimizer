<!-- Generator: Widdershins v4.0.1 -->

<h1 id="api-documentation">Api Documentation v1.0</h1>

> Scroll down for code samples, example requests and responses.

<h1 id="api-documentation-initoptimizersession">initOptimizerSession</h1>

## initOptimizerSessionUsingPOST

<a id="opIdinitOptimizerSessionUsingPOST"></a>

`POST /sessions`

*Initialize heuristic optimization session.*

> Example responses

> 200 Response

```json
{
  "type": "object"
}
```

<h3 id="initoptimizersessionusingpost-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Success!|[Iterable](#schemaiterable)|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|None|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Not authorized!|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden!|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not found!|None|

<aside class="success">
This operation does not require authentication
</aside>

<h1 id="api-documentation-submitpreferences">submitPreferences</h1>

## submitPreferencesUsingPOST

<a id="opIdsubmitPreferencesUsingPOST"></a>

`POST /sessions/{sessionId}/preferences`

*Submit preferences for an optimization session*

> Body parameter

```json
{
  "type": "object",
  "properties": {
    "sessionId": {
      "type": "string"
    },
    "weightMap": {
      "type": "object",
      "additionalProperties": {
        "type": "array",
        "items": {
          "type": "integer",
          "format": "int32"
        }
      }
    }
  }
}
```

<h3 id="submitpreferencesusingpost-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|sessionId|path|string|true|sessionId|
|body|body|[SlotPreferencesDTO](#schemaslotpreferencesdto)|true|payload|

> Example responses

> 200 Response

```json
{
  "type": "object"
}
```

<h3 id="submitpreferencesusingpost-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Success!|[Iterable](#schemaiterable)|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|None|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Not authorized!|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden!|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not found!|None|

<aside class="success">
This operation does not require authentication
</aside>

<h1 id="api-documentation-getoptimizedflightorders">getOptimizedFlightOrders</h1>

## getOptimizedFlightSequenceUsingGET

<a id="opIdgetOptimizedFlightSequenceUsingGET"></a>

`GET /sessions/{sessionId}/result`

*Get the result of the optimization result, if already available*

<h3 id="getoptimizedflightsequenceusingget-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|sessionId|path|string|true|sessionId|

> Example responses

> 200 Response

```json
{
  "type": "object"
}
```

<h3 id="getoptimizedflightsequenceusingget-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Success!|[Iterable](#schemaiterable)|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Not authorized!|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden!|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not found!|None|

<aside class="success">
This operation does not require authentication
</aside>

<h1 id="api-documentation-startoptimizationsession">startOptimizationSession</h1>

## startOptimizationSessionUsingPUT

<a id="opIdstartOptimizationSessionUsingPUT"></a>

`PUT /sessions/{sessionId}/start`

*Start the optimization session*

> Body parameter

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
    "sessionId": {
      "type": "string"
    }
  }
}
```

<h3 id="startoptimizationsessionusingput-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|sessionId|path|string|true|sessionId|
|body|body|[FlightSequenceDTO](#schemaflightsequencedto)|true|flightSequence|

> Example responses

> 200 Response

```json
{
  "type": "object"
}
```

<h3 id="startoptimizationsessionusingput-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Success!|[Iterable](#schemaiterable)|
|201|[Created](https://tools.ietf.org/html/rfc7231#section-6.3.2)|Created|None|
|401|[Unauthorized](https://tools.ietf.org/html/rfc7235#section-3.1)|Not authorized!|None|
|403|[Forbidden](https://tools.ietf.org/html/rfc7231#section-6.5.3)|Forbidden!|None|
|404|[Not Found](https://tools.ietf.org/html/rfc7231#section-6.5.4)|Not found!|None|

<aside class="success">
This operation does not require authentication
</aside>

# Schemas

<h2 id="tocS_Iterable">Iterable</h2>
<!-- backwards compatibility -->
<a id="schemaiterable"></a>
<a id="schema_Iterable"></a>
<a id="tocSiterable"></a>
<a id="tocsiterable"></a>

```json
{
  "type": "object"
}

```

### Properties

*None*

<h2 id="tocS_FlightSequenceDTO">FlightSequenceDTO</h2>
<!-- backwards compatibility -->
<a id="schemaflightsequencedto"></a>
<a id="schema_FlightSequenceDTO"></a>
<a id="tocSflightsequencedto"></a>
<a id="tocsflightsequencedto"></a>

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
    "sessionId": {
      "type": "string"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|flightSequence|[string]|false|none|none|
|sessionId|string|false|none|none|

<h2 id="tocS_OptimizationSessionDTO">OptimizationSessionDTO</h2>
<!-- backwards compatibility -->
<a id="schemaoptimizationsessiondto"></a>
<a id="schema_OptimizationSessionDTO"></a>
<a id="tocSoptimizationsessiondto"></a>
<a id="tocsoptimizationsessiondto"></a>

```json
{
  "type": "object",
  "required": [
    "sessionId"
  ],
  "properties": {
    "sessionId": {
      "type": "string",
      "description": "test session id"
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|sessionId|string|true|none|test session id|

<h2 id="tocS_SlotPreferencesDTO">SlotPreferencesDTO</h2>
<!-- backwards compatibility -->
<a id="schemaslotpreferencesdto"></a>
<a id="schema_SlotPreferencesDTO"></a>
<a id="tocSslotpreferencesdto"></a>
<a id="tocsslotpreferencesdto"></a>

```json
{
  "type": "object",
  "properties": {
    "sessionId": {
      "type": "string"
    },
    "weightMap": {
      "type": "object",
      "additionalProperties": {
        "type": "array",
        "items": {
          "type": "integer",
          "format": "int32"
        }
      }
    }
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|sessionId|string|false|none|none|
|weightMap|object|false|none|none|
|» **additionalProperties**|[integer]|false|none|none|

